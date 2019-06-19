/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: AccessTokenServiceImpl
 * Author:   Revisit-Moon
 * Date:     2019/1/31 12:10 PM
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/31 12:10 PM        1.0              描述
 */

package com.revisit.springboot.service.accesstoken.impl;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.entity.user.Authority;
import com.revisit.springboot.entity.user.Role;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.repository.system.SystemSettingRepository;
import com.revisit.springboot.repository.token.AccessTokenRepository;
import com.revisit.springboot.repository.user.AuthorityRepository;
import com.revisit.springboot.repository.user.RoleRepository;
import com.revisit.springboot.repository.user.UserRepository;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.utils.AuthorityUtil;
import com.revisit.springboot.utils.MoonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 〈〉
 *
 * @author Revisit-Moon
 * @create 2019/1/31
 * @since 1.0.0
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class AccessTokenServiceImpl implements AccessTokenService {

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public AccessToken findAccessTokenByIdAndIsValid(String id) {
        return checkTokenIsExpired(accessTokenRepository.findById(id).orElse(null));
    }



    @Override
    public AccessToken isValid(String authorization) {
        return checkTokenIsExpired(accessTokenRepository.findById(authorization).orElse(null));
    }

    @Override
    public AccessToken isValidTokenAndRoles(String authorization,String ...roleNameArray) {
        String roleNames = "系统管理员,超级管理员,";
        if (roleNameArray.length>0) {
            for (String s : roleNameArray) {
                roleNames += s + ",";
            }
        }

        roleNames = roleNames.substring(0,roleNames.length()-1);

        return checkTokenIsExpired(accessTokenRepository.isValidTokenAndRoles(authorization,roleNames));
    }

    @Override
    public AccessToken isValidTokenAndAuthorities(String authorization,String ...authoritiesArray) {
        AccessToken token = isValidTokenAndRoles(authorization, "");
        if (authoritiesArray.length>0) {
            if (token != null) {
                return token;
            }
        }
        String authorities = "";
        for (String s :authoritiesArray) {
            authorities = s+",";
        }

        authorities = authorities.substring(0,authorities.length()-1);

        return checkTokenIsExpired(accessTokenRepository.isValidTokenAndAuthorities(authorization,authorities));
    }

    private AccessToken checkTokenIsExpired(AccessToken accessToken){

        if (accessToken==null){
            return null;
        }
        Date nowTime = new Date();
        if (nowTime == MoonUtil.contrastTime(nowTime,new Date(accessToken.getTtl()+accessToken.getCreateTime().getTime()))){
            //已过期
            accessTokenRepository.delete(accessToken);
            return null;
        }
        return accessToken;
    }

    @Override
    public AccessToken isValidTokenAndValidAuthority(String authorization, String authorityName) {
        AccessToken accessToken = findAccessTokenByIdAndIsValid(authorization);
        if (accessToken==null){
            return null;
        }

        User user = userRepository.findById(accessToken.getUserId()).orElse(null);
        if (user == null){
            return null;
        }

        Authority authority = authorityRepository.findByAuthorityNameOrDescription(authorityName);
        if (authority==null){
            if (user.getRoles().contains(getSystemAdmin())){
                return accessToken;
            }
            return null;
        }

        Set<Role> roles = user.getRoles();

        if (roles!=null&&!roles.isEmpty()) {
            if (roles.contains(getSystemAdmin())||roles.contains(getSuperAdmin())){
                return accessToken;
            }
            for (Role role : roles) {
                Set<Authority> authorities = role.getAuthorities();
                if (authorities.contains(authority)) {
                    return accessToken;
                }else{
                    if (authorityName.contains("SELF_")){
                        String removeAfterAuthorityName = StringUtils.remove(authorityName, "SELF_");
                        if (authorities.contains(authorityRepository.findByAuthorityNameOrDescription(removeAfterAuthorityName))) {
                            return accessToken;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public AccessToken isValidTokenAndRole(String authorization, String roleName) {
        AccessToken accessToken = findAccessTokenByIdAndIsValid(authorization);
        if(StringUtils.isBlank(authorization)){
            return null;
        }
        if (accessToken==null){
            return null;
        }
        User user = userRepository.findById(accessToken.getUserId()).orElse(null);
        if (user == null){
            return null;
        }
        Role role = roleRepository.findByRoleName(roleName);

        if (role==null){
            if (user.getRoles().contains(getSuperAdmin())||user.getRoles().contains(getSystemAdmin())){
                return accessToken;
            }
            return null;
        }

        Set<Role> roles = user.getRoles();
        if (roles.contains(role)||roles.contains(getSuperAdmin())||roles.contains(getSystemAdmin())){
            return accessToken;
        }
        return null;
    }

    private Role getSystemAdmin(){
        Role systemAdmin = roleRepository.findSystemAdmin();
        return systemAdmin;
    }
    private Role getSuperAdmin(){
        Role superAdmin = roleRepository.findSuperAdmin();
        return superAdmin;
    }

    @Override
    public AccessToken isSelf(String id, JSONObject target) {
        AccessToken accessToken = accessTokenRepository.findById(id).orElse(null);
        String userId = target.getString("userId");
        if (userId == null){
            accessToken = isValidTokenAndRole(accessToken.getId(), AuthorityUtil.SUPER_ADMIN);
            if (accessToken!=null){
                return accessToken;
            }else {
                return null;
            }
        }
        if (!accessToken.getUserId().equals(userId)) {
            accessToken = isValidTokenAndRole(accessToken.getId(),AuthorityUtil.SUPER_ADMIN);
            if (accessToken!=null){
                return accessToken;
            }
            return null;
        }else{
            return accessToken;
        }
    }

    @Override
    public List<Integer> findPriceLevelByAccessTokenAndIsValid(String id) {
        AccessToken accessToken = findAccessTokenByIdAndIsValid(id);
        if (accessToken == null){
            return null;
        }
        User user = userRepository.findById(accessToken.getUserId()).orElse(null);
        if (user == null){
            return null;
        }
        Set<Role> roles = user.getRoles();
        if (roles==null||roles.isEmpty()){
            return null;
        }
        List<Integer> priceLevelList = new ArrayList<>();
        for (Role role :roles) {
            priceLevelList.add(role.getLevel());
        }
        return priceLevelList;
    }
}