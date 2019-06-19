/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: UserServiceImpl
 * Author:   Revisit-Moon
 * Date:     2019/1/29 9:52 AM
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/29 9:52 AM        1.0              描述
 */

package com.revisit.springboot.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.system.SystemSetting;
import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.entity.user.Authority;
import com.revisit.springboot.entity.user.Role;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.repository.system.SystemSettingRepository;
import com.revisit.springboot.repository.token.AccessTokenRepository;
import com.revisit.springboot.repository.user.AuthorityRepository;
import com.revisit.springboot.repository.user.RoleRepository;
import com.revisit.springboot.repository.user.UserRepository;
import com.revisit.springboot.service.user.RoleService;
import com.revisit.springboot.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 〈角色逻辑层〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    private final static Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Override
    public JSONObject addRole(Role role,String authorityIds) {

        logger.info("新增角色: " + role.getRoleName());

        if (StringUtils.isNotBlank(role.getRoleName())){
            Role oldRole = roleRepository.findByRoleName(role.getRoleName());
            if (oldRole!=null){
                return Result.fail(102,"参数错误","该角色已存在");
            }
        }

        int level = role.getLevel();
        // int priceLevel = role.getLevel();

        Role oldRole = roleRepository.findByLevel(level);
        if (oldRole!=null){
            roleRepository.allRoleLevelAddOneByLevel(level);
        }

        // List<Role> priceLevelList = roleRepository.findByPriceLevel(priceLevel);
        // if (priceLevelList==null||priceLevelList.isEmpty()){
        //     if (!(priceLevel>=0&&priceLevel<=6)) {
        //         return Result.fail(102, "参数错误", "角色显示价格等级不存在");
        //     }
        // }

        if (level == 0&&!role.getRoleName().equals(AuthorityUtil.ORDINARY_MEMBER)){
            return Result.fail(102,"参数错误","会员等级为0时名字必需为非会员");
        }

        Set<Authority> authorityList = new HashSet<>();
        if (authorityIds.contains(",")){
            String[] split = StringUtils.split(authorityIds,",");
            for (String s : split) {
                Authority authority = authorityRepository.findById(s).orElse(null);
                if (authority!=null){
                    authorityList.add(authority);
                }
            }
        }else{
            Authority authority = authorityRepository.findById(authorityIds).orElse(null);
            if (authority!=null){
                authorityList.add(authority);
            }
        }

        if (authorityList.isEmpty()){
            role.setAuthorities(null);
        }else{
            role.setAuthorities(authorityList);
        }

        role.setInherent(false);

        //保存此对象
        role = roleRepository.save(role);
        if (StringUtils.isBlank(role.getId())){
            return Result.fail(110,"更新失败","请联系管理员");
        }

       JSONObject userBean = (JSONObject)JSON.toJSON(role);
        return Result.success(200,"新增角色成功",userBean);
    }

    @Override
    public JSONObject deleteRoleById(String id) {
        logger.info("删除角色: " + id);
        Role role = roleRepository.findById(id).orElse(null);
        if (role==null){
            return Result.fail(102,"查询失败","角色对象不存在");
        }
        // if (role.isInherent()){
        //     return Result.fail(102,"删除失败","系统内置角色无法删除");
        // }
        roleRepository.allRoleLevelMinusOneByLevel(role.getLevel());
        roleRepository.delete(role);
        return Result.success(200,"删除成功","");
    }

    @Override
    public JSONObject updateRoleById(String id, Role role,String authorityIds) {
        logger.info("更新角色: " + id);
        Role oldRole = roleRepository.findById(id).orElse(null);

        if (oldRole==null){
            return Result.fail(102,"查询失败","角色对象不存在");
        }

        if (oldRole.isInherent()){
            return Result.fail(102,"更新失败","系统内置角色无法更新");
        }
        // List<Role> priceLevelList = roleRepository.findByPriceLevel(role.getPriceLevel());
        // if (priceLevelList==null||priceLevelList.isEmpty()){
        //     if (!(role.getPriceLevel()>=0&&role.getPriceLevel()<=6)) {
        //         return Result.fail(102, "参数错误", "角色显示价格等级不存在");
        //     }
        // }

        // RechargeItem oldRechargeItem = rechargeItemRepository.findByName(oldRole.getRoleName());

        String needFixRechargeItemName = "";
        if (!oldRole.getRoleName().equals(role.getRoleName())){
            Role oldRoleByName = roleRepository.findByRoleName(role.getRoleName());
            if (oldRoleByName!=null){
                return Result.fail(102,"查询失败","要修改的角色名已存在");
            }
            needFixRechargeItemName = role.getRoleName();
        }

        int level = role.getLevel();

        if (level == 0&&!role.getRoleName().equals(AuthorityUtil.ORDINARY_MEMBER)){
            return Result.fail(102,"参数错误","会员等级为0时名字必需为非会员");
        }

        Role oldRoleLevel = roleRepository.findByLevel(level);
        if (oldRoleLevel!=null&&level!=oldRole.getLevel()){
            if (oldRoleLevel.isInherent()){
                return Result.fail(102,"更新失败","系统内置角色无法更新");
            }
            roleRepository.allRoleLevelMinusOneByLevel(oldRole.getLevel());
            roleRepository.allRoleLevelAddOneByLevel(oldRoleLevel.getLevel());
        }

        Set<Authority> authorityList = new HashSet<>();
        if (authorityIds.contains(",")){
            String[] split = StringUtils.split(authorityIds,",");
            for (String s : split) {
                Authority authority = authorityRepository.findById(s).orElse(null);
                if (authority!=null){
                    authorityList.add(authority);
                }
            }
        }else{
            Authority authority = authorityRepository.findById(authorityIds).orElse(null);
            if (authority!=null){
                authorityList.add(authority);
            }
        }

        if (authorityList.isEmpty()){
            role.setAuthorities(null);
        }else{
            role.setAuthorities(authorityList);
        }

        //设置不更新字段
        String ignoreProperties = "isInherent";
        JavaBeanUtil.copyProperties(oldRole,role,ignoreProperties);

        Set<Authority> authorities = role.getAuthorities();
        Set<Authority> oldAuthorities = new HashSet<>();
        if (authorities!=null&&!authorities.isEmpty()) {
            for (Authority authority : authorities) {
                String authorityName = authority.getAuthorityName();
                if (StringUtils.isBlank(authorityName)){
                    return Result.fail(102,"参数错误","权限名不可为空");
                }
                Authority oldAuthority = authorityRepository.findByAuthorityNameOrDescription(authorityName);
                if (oldAuthorities==null) {
                    return Result.fail(102,"参数错误",authorityName+"该权限名不存在");
                }
                oldAuthorities.add(oldAuthority);
            }
        }

        //替换权限集合
        role.setAuthorities(oldAuthorities);
        role.setInherent(false);
        role = roleRepository.save(role);

        if (StringUtils.isBlank(role.getId())){
            return Result.fail(110,"更新失败","请联系管理员");
        }
        // if (oldRechargeItem != null && StringUtils.isNotBlank(needFixRechargeItemName)) {
        //     oldRechargeItem.setName(needFixRechargeItemName);
        //     rechargeItemRepository.saveAndFlush(oldRechargeItem);
        // }
        return Result.success(200,"更新成功",role);
    }

    @Override
    public JSONObject findRoleById(String id) {
        logger.info("获取角色: " + id);

        Role role = roleRepository.findById(id).orElse(null);

        if (role==null){
            return Result.fail(102,"查询失败","角色对象不存在");
        }

        return Result.success(200,"查询成功",role);
    }

    @Override
    public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        Page roleListPage = findRoleList(keyword, orderBy, 1, Integer.MAX_VALUE);

        List<User> roleList = new ArrayList<>();
        if (roleListPage!=null){
            roleList.addAll(roleListPage.getContent());
        }
        //从数据库获取需要导出的数据
        if (roleList!=null&&!roleList.isEmpty()) {
            //导出操作
            ExcelUtil.exportExcel(roleList, "角色列表", "角色列表",Role.class, "角色列表.xls", response);
        }else {
            try {
                response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public JSONObject findRoleByList(String keyword, String orderBy, Date beginTime ,Date endTime, Integer page, Integer rows) {

        //如果当前页数是空,则默认第一页
        if (page==null) {
            page = 1;
        }
        //如果需要查询条数为空,则默认查询10条
        if (rows==null){
            rows=10;
        }

        Page roleListPage = findRoleList(keyword,orderBy,page,rows);

        if (roleListPage==null){
            return Result.fail(102,"参数有误","获取不到相关数据");
        }
        JSONObject result = new JSONObject();
        result.put("rowsTotal",roleListPage.getTotalElements());
        result.put("page",roleListPage.getNumber()+1);
        result.put("rows",roleListPage.getSize());
        result.put("roleList",roleListPage.getContent());
        return Result.success(200,"查询成功",result);
    }

    private Page findRoleList(String keyword, String orderBy,Integer page, Integer rows){
        //分页插件
        PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
        Pageable pageable = pageableUtil.getPageable();
        //jpa查询构造器
        Page roleListPage = roleRepository.findAll(new Specification<Role>() {
            @Override
            public Predicate toPredicate(Root<Role> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                // predicateList.add(criteriaBuilder.isNotNull(root.get("id")));
                //指定查询对象
                // query.from(User.class);
                if (StringUtils.isNotBlank(keyword)) {
                    predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("roleName"), "%" + keyword + "%")
                            , criteriaBuilder.like(root.get("description"), "%" + keyword + "%")
                            , criteriaBuilder.like(root.get("id"), "%" + keyword + "%")));
                }

                // if (beginTime != null) {
                //     predicateList.add(criteriaBuilder.and(criteriaBuilder.greaterThan(root.get("createTime"), beginTime)));
                // }
                //
                // if (endTime != null) {
                //     predicateList.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get("createTime"), endTime)));
                // }
                return query.where(predicateList.toArray(new Predicate[predicateList.size()])).getRestriction();
            }
        }, pageable);

        if (!roleListPage.hasContent()){
            return null;
        }
        return roleListPage;
    }

    private boolean isSelf(String authorization, String id) {
        User requestUser = userRepository.findById(accessTokenRepository.findById(authorization).orElse(null).getUserId()).orElse(null);
        if (!requestUser.getId().equals(id)){
            Set<Role> roles = requestUser.getRoles();
            for (Role role :roles) {
                if (role.getRoleName().equals(AuthorityUtil.SUPER_ADMIN)||role.getRoleName().equals(AuthorityUtil.SYSTEM_ADMIN)){
                    return true;
                }else{
                    return false;
                }
            }

        }
        return true;
    }

    private Set<Role> setRoles(Role role){
        Set<Role> roles = new HashSet<>();
        String roleName = role.getRoleName();
        switch (roleName){
            case AuthorityUtil.SUPER_ADMIN:{
                Set<Authority> authorities = setAuthority(AuthorityUtil.generateSuperAdminAuthority());
                role.setAuthorities(authorities);
                roles.add(role);
                break;
            }
            case AuthorityUtil.ORDINARY_MEMBER:{
                Set<Authority> authorities = setAuthority(AuthorityUtil.generateOrdinaryMemberAuthority());
                role.setAuthorities(authorities);
                roles.add(role);
                break;
            }
            // case AuthorityUtil.YEAR_MEMBER:{
            //     Set<Authority> authorities = setAuthority(AuthorityUtil.generateMemberAuthority());
            //     role.setAuthorities(authorities);
            //     roles.add(role);
            //     break;
            // }
            // case AuthorityUtil.PRIVILEGE_MEMBER:{
            //     Set<Authority> authorities = setAuthority(AuthorityUtil.generateMemberAuthority());
            //     role.setAuthorities(authorities);
            //     roles.add(role);
            //     break;
            // }
            // case AuthorityUtil.TEMPORARY_MEMBER:{
            //     Set<Authority> authorities = setAuthority(AuthorityUtil.generateMemberAuthority());
            //     role.setAuthorities(authorities);
            //     roles.add(role);
            //     break;
            // }
            default:{
                Set<Authority> authorities = setAuthority(AuthorityUtil.generateTouristAuthority());
                role.setAuthorities(authorities);
                roles.add(role);
                break;
            }
        }
        return roles;
    }

    private Set<Authority> setAuthority(Set<Authority> authorities){
        Set<Authority> authoritiesSet = new HashSet<>();
        if (authorities!=null&&!authorities.isEmpty()) {
            for (Authority authority :authorities) {
                Authority auth = authorityRepository.findByAuthorityNameOrDescription(authority.getAuthorityName());
                if (auth==null){
                    auth = authorityRepository.save(authority);
                    authoritiesSet.add(auth);
                }else{
                    authoritiesSet.add(auth);
                }
            }
        }
        return authoritiesSet;
    }

    private AccessToken createAccessToken(String userId){
        AccessToken accessToken = findAccessTokenByUserIdAndIsValid(userId);
        SystemSetting systemSetting = systemSettingRepository.findSystemSetting();
        if (accessToken == null){
            accessToken = new AccessToken();
            accessToken.setTtl(systemSetting.getTokenValid());
            accessToken.setUserId(userId);
            accessToken = accessTokenRepository.save(accessToken);
        }else{
            accessTokenRepository.delete(accessToken);
            accessToken = new AccessToken();
            accessToken.setTtl(systemSetting.getTokenValid());
            accessToken.setUserId(userId);
            accessToken = accessTokenRepository.save(accessToken);
        }
        return accessToken;
    }

    private AccessToken findAccessTokenByUserIdAndIsValid(String userId){
        AccessToken accessToken = accessTokenRepository.findByUserId(userId);
        if (accessToken==null){
            return null;
        }else {
            Date nowTime = new Date();
            if (nowTime == MoonUtil.contrastTime(nowTime,new Date(accessToken.getTtl()+accessToken.getCreateTime().getTime()))){
                accessTokenRepository.delete(accessToken);
                //已过期
                return null;
            }
        }
        return accessToken;
    }
}
