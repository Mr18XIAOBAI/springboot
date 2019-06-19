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
import com.revisit.springboot.component.uuid.CustomizeUUIDGenerate;
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
import com.revisit.springboot.service.user.UserService;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 〈〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */

@Transactional(rollbackFor = Exception.class)
@Service
public class UserServiceImpl implements UserService {

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

    @Autowired
    @PersistenceContext
    private EntityManager entityManager;


    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Override
    public JSONObject addUser(User user) {

        logger.info("新增用户: " + user.getRealName());

        if (StringUtils.isBlank(user.getUserName())
                &&StringUtils.isBlank(user.getMobile())
                &&StringUtils.isBlank(user.getEmail())){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }

        if (StringUtils.isNotBlank(user.getUserName())&&user.getUserName().length()<4){
            return Result.fail(102,"参数错误","用户名不可少于4位");
        }

        if (StringUtils.isNotBlank(user.getUserName())){
            User oldUser = userRepository.findByUserName(user.getUserName());
            if (oldUser!=null){
                return Result.fail(102,"参数错误","该用户名已被注册");
            }
        }

        if (StringUtils.isNotBlank(user.getEmail())){
            User oldUser = userRepository.findByEmail(user.getEmail());
            if (oldUser!=null){
                return Result.fail(102,"参数错误","该邮箱已被注册");
            }
        }

        if (StringUtils.isNotBlank(user.getMobile())){
            User oldUser = userRepository.findByMobile(user.getMobile());
            if (oldUser!=null){
                return Result.fail(102,"参数错误","该手机号码已被注册");
            }
        }

        //如果推广码不为空
        if (StringUtils.isNotBlank(user.getReferrerId())&&user.getReferrerId().length()==32) {
            //将输入的推广人UUID转成base64UUID
            String id = CustomizeUUIDGenerate.generateBase64UUID(user.getReferrerId());
            //搜索系统是否存在该用户
            User referrerUser = userRepository.findById(id).orElse(null);
            if (referrerUser==null) {
                return Result.fail(102, "参数错误", "推荐人不存在");
            }else{
                //如果搜索到,则将此注册用户的推广人ID设置成推广人的推荐码
                user.setReferrerId(referrerUser.getReferrerCode());
            }
        }

        //将此对象的密码进行加密
        // user.setPassword(DigestUtils.md5Hex(user.getPassword().trim()));
        if (StringUtils.isBlank(user.getRealName())){
            return Result.fail(102,"参数错误","姓名不能为空");
        }

        //数据库中是否存在游客角色
        Role role = roleRepository.findByRoleName(AuthorityUtil.TOURIST);
        if (role==null) {
            role = new Role();
            role.setRoleName(AuthorityUtil.TOURIST);
            role.setDescription("拥有基本权限");
            user.setRoles(setRoles(role));
        }

        //用户里是否已有角色
        Set<Role> roles = user.getRoles();
        if (roles==null||roles.isEmpty()){
            roles = new HashSet<>();
        }
        //如果此用户中不包含该角色,则添加
        if (!roles.contains(role)) {
            roles.add(role);
            user.setRoles(roles);
        }

        //保存此对象
        user = userRepository.save(user);

        if (StringUtils.isBlank(user.getId())){
            return Result.fail(110,"系统错误","新增用户失败,请联系管理员");
        }

       JSONObject userBean = (JSONObject)JSON.toJSON(user);
        return Result.success(200,"新增用户成功",userBean);
    }

    @Override
    public JSONObject bindingMobile(String userId, String mobile,String realName) {

        if (StringUtils.isBlank(realName)){
            return Result.fail(102,"参数错误","用户真实姓名不能为空");
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user==null){
            return Result.fail(102,"查询失败","用户对象不存在");
        }

        User oldUser = userRepository.findByMobile(mobile);
        if (oldUser!=null){
            return Result.fail(102,"参数错误","该手机号码已被注册");
        }

        user.setMobile(mobile);
        user.setRealName(realName);

        user = userRepository.saveAndFlush(user);

        return Result.success(200,"更新成功","用户: "+user.getRealName()+" 手机号码绑定成功");
    }

    @Override
    public JSONObject userLogin(String userName, String email, String mobile, String smsCode, String password) {
        if (StringUtils.isNotBlank(userName)
                &&StringUtils.isNotBlank(email)
                &&StringUtils.isNotBlank(mobile)){
            return Result.fail(102,"参数错误","只能选择一种登录方式");
        }

        if (StringUtils.isBlank(password)||password.length()<6){
            return Result.fail(102,"参数错误","密码错误");
        }
        //MD5加密
        // password = DigestUtils.md5Hex(password);
        //获取用户
        User user = userRepository.findBySomeOneAccountAndLogin(userName,email,mobile,password);

        if (user==null){
            return Result.fail(102,"参数错误","账号或密码不正确");
        }

        AccessToken accessToken = createAccessToken(user.getId());

        return Result.tokenSuccess(200,"登录成功",user,accessToken.getId());
    }

    @Override
    public JSONObject deleteUserById(String id) {
        logger.info("删除用户: " + id);
        User user = userRepository.findById(id).orElse(null);
        if (user==null){
            return Result.fail(102,"查询失败","用户对象不存在");
        }
        userRepository.deleteById(id);
        return Result.success(200,"删除成功","");
    }

    @Override
    public JSONObject updateUserById(String id, User user) {
        logger.info("更新用户: " + id);
        User oldUser = userRepository.findById(id).orElse(null);
        if (oldUser==null){
            return Result.fail(102,"查询失败","用户对象不存在");
        }
        //设置不更新字段
        String ignoreProperties = "roles,mobile,password,userName,email";
        JavaBeanUtil.copyProperties(oldUser,user,ignoreProperties);
        user = userRepository.save(user);
        return Result.success(200,"更新成功",user);
    }

    @Override
    public JSONObject findUserById(String authorization,String id) {
        logger.info("获取用户: " + id);

        User user = userRepository.findById(id).orElse(null);

        if (user==null){
            return Result.fail(102,"查询失败","用户对象不存在");
        }

        if (!isSelf(authorization,user.getId())){
            return Result.fail(102,"查询失败","用户不是自己且不是超级管理员");
        }

        return Result.success(200,"查询成功",user);
    }

    @Override
    public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        Page userListPage = findUserList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

        List<User> userList = new ArrayList<>();
        if (userListPage!=null){
            userList.addAll(userListPage.getContent());
        }
        //从数据库获取需要导出的数据
        if (userList!=null&&!userList.isEmpty()) {
            //导出操作
            ExcelUtil.exportExcel(userList, "用户列表", "用户列表",User.class, "用户列表.xls", response);
        }else {
            try {
                response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public JSONObject specifiedUserRole(String id, String roleName) {
        logger.info("指定某个用户拥有某些角色");
        User user = userRepository.findById(id).orElse(null);
        if (user==null){
            return Result.fail(102,"参数错误","用户不存在");
        }
        Set<String> roleNameList = new HashSet<>();

        if (roleName.contains(",")){
            roleNameList=MoonUtil.getStringSetByComma(roleName);
        }else {
            roleNameList.add(roleName);
        }

        if (roleNameList.isEmpty()){
            return Result.fail(102,"参数错误","参数不正确");
        }

        Set<Role> oldRoles = user.getRoles();
        for (Role oldRole :oldRoles) {
            if (roleName.equals(oldRole.getRoleName())){
                return Result.fail(102,"参数错误","该用户已拥有某个角色");
            }
        }

        Set<Role> newRoles = new HashSet<>();
        for (String roleNameStr :roleNameList) {
            Role role = roleRepository.findByRoleName(roleNameStr);
            if (role==null){
                if (AuthorityUtil.existOrdinaryRole(roleNameStr)){
                    role = new Role();
                    role.setRoleName(roleName);
                    Set<Role> roles = setRoles(role);
                    newRoles.addAll(roles);
                }
            }else{
                newRoles.add(role);
            }
        }
        //替换角色
        user.setRoles(newRoles);

        user = userRepository.save(user);
        if (StringUtils.isBlank(user.getId())) {
            return Result.fail(110,"系统错误","更新用户时出错");
        }


        return Result.success(200,"更新成功", user);
    }

    @Override
    public JSONObject addUserToSomeRole(String userName, String email, String mobile, String password, String roleName) {
        if (StringUtils.isNotBlank(userName)&&userName.length()<4){
            return Result.fail(102,"参数错误","用户名不可少于4位");
        }

        if (StringUtils.isNotBlank(userName)){
            User oldUser = userRepository.findByUserName(userName);
            if (oldUser!=null){
                return Result.fail(102,"参数错误","该用户名已被注册");
            }
        }

        if (StringUtils.isNotBlank(email)){
            User oldUser = userRepository.findByEmail(email);
            if (oldUser!=null){
                return Result.fail(102,"参数错误","该邮箱已被注册");
            }
        }

        if (StringUtils.isNotBlank(mobile)){
            User oldUser = userRepository.findByMobile(mobile);
            if (oldUser!=null){
                return Result.fail(102,"参数错误","该手机号码已被注册");
            }
        }
        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setPassword(password);

        Set<String> roleNameList = new HashSet<>();

        if (roleName.contains(",")){
            roleNameList=MoonUtil.getStringSetByComma(roleName);
        }else {
            roleNameList.add(roleName);
        }

        if (roleNameList.isEmpty()){
            return Result.fail(102,"参数错误","参数不正确");
        }

        Set<Role> newRoles = new HashSet<>();
        for (String roleNameStr :roleNameList) {
            Role role = roleRepository.findByRoleName(roleNameStr);
            if (role==null){
                if (AuthorityUtil.existOrdinaryRole(roleNameStr)){
                    role = new Role();
                    role.setRoleName(roleName);
                    Set<Role> roles = setRoles(role);
                    newRoles.addAll(roles);
                }else{
                    return Result.fail(102,"查询失败","角色不存在");
                }
            }else{
                newRoles.add(role);
            }
        }
        //替换角色
        user.setRoles(newRoles);

        user = userRepository.save(user);
        if (StringUtils.isBlank(user.getId())) {
            return Result.fail(110,"系统错误","新增用户时出错");
        }
        return Result.success(200,"新增成功", user);
    }

    @Override
    public JSONObject findUserByList(String keyword, String orderBy, Date beginTime ,Date endTime, Integer page, Integer rows) {

        //如果当前页数是空,则默认第一页
        if (page==null) {
            page = 1;
        }
        //如果需要查询条数为空,则默认查询10条
        if (rows==null){
            rows=10;
        }

        Page userListPage = findUserList(keyword,orderBy,beginTime,endTime,page,rows);

        if (userListPage==null){
            return Result.fail(102,"参数有误","获取不到相关数据");
        }
        JSONObject result = new JSONObject();
        result.put("rowsTotal",userListPage.getTotalElements());
        result.put("page",userListPage.getNumber()+1);
        result.put("rows",userListPage.getSize());
        result.put("userList",userListPage.getContent());
        return Result.success(200,"查询成功",result);
    }

    private Page findUserList(String keyword, String orderBy, Date beginTime ,Date endTime, Integer page, Integer rows){
        //分页插件
        PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
        Pageable pageable = pageableUtil.getPageable();
        //jpa查询构造器
        Page userListPage = userRepository.findAll(new Specification<User>() {
            @Override
            public Predicate toPredicate(Root<User> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = new ArrayList<>();
                // predicateList.add(criteriaBuilder.isNotNull(root.get("id")));
                //指定查询对象
                // query.from(User.class);
                if (StringUtils.isNotBlank(keyword)) {
                    predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("userName"), "%" + keyword + "%")
                            , criteriaBuilder.like(root.get("realName"), "%" + keyword + "%")
                            , criteriaBuilder.like(root.get("mobile"), "%" + keyword + "%")
                            , criteriaBuilder.like(root.get("weChatName"), "%" + keyword + "%")));
                }

                if (beginTime != null) {
                    predicateList.add(criteriaBuilder.and(criteriaBuilder.greaterThan(root.get("createTime"), beginTime)));
                }

                if (endTime != null) {
                    predicateList.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get("createTime"), endTime)));
                }
                return query.where(predicateList.toArray(new Predicate[predicateList.size()])).getRestriction();
            }
        }, pageable);

        if (!userListPage.hasContent()){
            return null;
        }
        return userListPage;
    }

    @Override
    public JSONObject loginInWeChatMiniProgram(String openId,String sessionKey,String rawData,String signature,String encryptedData,String iv,String referrerId) {

        String decryptStr = WeChatUtil.decryptWeChatMiniProgramSessionKey(encryptedData, iv, sessionKey);

        if (StringUtils.isBlank(decryptStr)){
            return Result.fail(102,"登录失败","请重新授权");
        }

        if (!WeChatUtil.signatureIsValid(rawData,sessionKey,signature)){
            return Result.fail(102,"非法操作","数据签名校验失败");
        }

        JSONObject userInfo = JSONObject.parseObject(decryptStr);
        if (userInfo!=null){
            if (!openId.equals(userInfo.getString("openId"))){
                return Result.fail(102,"非法操作","openId不正确");
            }
            JSONObject watermark = userInfo.getJSONObject("watermark");
            if (!WeChatUtil.isSystemMiniProgram(watermark.getString("appid"))){
                return Result.fail(102,"非法操作","appid不正确");
            }
        }
        User weChatUser = userRepository.findByWeChatOpenId(userInfo.getString("openId"));
        if (weChatUser == null) {
            weChatUser = new User();
            weChatUser.setGender(userInfo.getInteger("gender"));
            weChatUser.setWeChatOpenId(userInfo.getString("openId"));
            weChatUser.setWeChatName(userInfo.getString("nickName"));
            weChatUser.setWeChatAvatar(userInfo.getString("avatarUrl"));
            weChatUser.setWeChatCity(userInfo.getString("city"));
            //数据库中是否存在游客角色
            Role role = roleRepository.findByRoleName(AuthorityUtil.TOURIST);
            if (role == null) {
                role = new Role();
                role.setRoleName(AuthorityUtil.TOURIST);
                role.setDescription("拥有基本读取权限");
                weChatUser.setRoles(setRoles(role));
            }
            //用户里是否已有角色
            Set<Role> roles = weChatUser.getRoles();
            if (roles == null || roles.isEmpty()) {
                roles = new HashSet<>();
            }
            //如果此用户中不包含该角色,则添加
            if (!roles.contains(role)) {
                roles.add(role);
                weChatUser.setRoles(roles);
            }
            //如果有推广人Id
            if (StringUtils.isNotBlank(referrerId)&&referrerId.length()==32){
                //将输入的推广人UUID转成base64UUID
                String id = CustomizeUUIDGenerate.generateBase64UUID(referrerId);
                //搜索系统是否存在该用户
                User referrerUser = userRepository.findById(id).orElse(null);
                if (referrerUser!=null) {
                    //如果搜索到,则将此注册用户的推广人ID设置成推广人的推荐码
                    weChatUser.setReferrerId(referrerUser.getReferrerCode());
                }
            }
            weChatUser = userRepository.save(weChatUser);
            if (StringUtils.isBlank(weChatUser.getId())) {
                return Result.fail(100, "新增微信用户失败", "系统错误");
            }
        }else{
            //否则更新最新的微信信息
            weChatUser.setGender(userInfo.getInteger("gender"));
            weChatUser.setWeChatOpenId(userInfo.getString("openId"));
            weChatUser.setWeChatName(userInfo.getString("nickName"));
            weChatUser.setWeChatAvatar(userInfo.getString("avatarUrl"));
            weChatUser.setWeChatCity(userInfo.getString("city"));
            weChatUser = userRepository.save(weChatUser);
        }
        AccessToken accessToken = createAccessToken(weChatUser.getId());
        return Result.tokenSuccess(200,"登录成功",weChatUser,accessToken.getId());
    }

    @Override
    public JSONObject addAdmin(String userName, String email, String mobile, String password) {
        if (StringUtils.isBlank(password)){
            return Result.fail(102,"新增超级管理员失败","请输入密码");
        }
        // password = DigestUtils.md5Hex(password.trim());
        User user = userRepository.findBySomeOneAccount(userName, email, mobile);
        if (user!=null){
            return Result.fail(102,"新增超级管理员失败","该用户名或邮箱或手机已被注册");
        }

        user = new User();
        if (StringUtils.isNotBlank(userName)){
            user.setUserName(userName);
        }
        if (StringUtils.isNotBlank(email)){
            user.setEmail(userName);
        }
        if (StringUtils.isNotBlank(mobile)){
            user.setMobile(mobile);
        }

        user.setPassword(password);

        //数据库中是否存在超级管理员角色
        Role role = roleRepository.findByRoleName(AuthorityUtil.SUPER_ADMIN);
        if (role==null) {
            role = new Role();
            role.setRoleName(AuthorityUtil.SUPER_ADMIN);
            role.setDescription("拥有所有权限");
            user.setRoles(setRoles(role));
        }
        //用户里是否已有角色
        Set<Role> roles = user.getRoles();
        if (roles==null||roles.isEmpty()){
            roles = new HashSet<>();
        }
        //如果此用户中不包含该角色,则添加
        if (!roles.contains(role)) {
            roles.add(role);
            user.setRoles(roles);
        }

        user = userRepository.save(user);
        if (StringUtils.isBlank(user.getId())){
            return Result.fail(100,"新增超级管理员失败","系统错误");
        }
        return Result.success(200,"新增超级管理员成功",user);
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
                role.setInherent(true);
                role.setLevel(998);
                roles.add(role);
                break;
            }
            case AuthorityUtil.SHOP_ADMIN:{
                Set<Authority> authorities = setAuthority(AuthorityUtil.generateShopAdminAuthority());
                role.setAuthorities(authorities);
                role.setInherent(true);
                role.setLevel(997);
                roles.add(role);
                break;
            }
            case AuthorityUtil.MEMBER_ADMIN:{
                Set<Authority> authorities = setAuthority(AuthorityUtil.generateMemberAdminAuthority());
                role.setAuthorities(authorities);
                role.setInherent(true);
                role.setLevel(996);
                roles.add(role);
                break;
            }
            case AuthorityUtil.TOURIST:{
                Set<Authority> authorities = setAuthority(AuthorityUtil.generateTouristAuthority());
                role.setAuthorities(authorities);
                role.setInherent(true);
                role.setLevel(0);
                roles.add(role);
                break;
            }
            case AuthorityUtil.ORDINARY_MEMBER:{
                Set<Authority> authorities = setAuthority(AuthorityUtil.generateOrdinaryMemberAuthority());
                role.setAuthorities(authorities);
                role.setInherent(true);
                role.setLevel(1);
                roles.add(role);
                break;
            }
            case AuthorityUtil.VIP_MEMBER:{
                Set<Authority> authorities = setAuthority(AuthorityUtil.generateVipMenberAuthority());
                role.setAuthorities(authorities);
                role.setInherent(true);
                role.setLevel(2);
                roles.add(role);
                break;
            }
            case AuthorityUtil.PARTNER:{
                Set<Authority> authorities = setAuthority(AuthorityUtil.generatePartnerAuthority());
                role.setAuthorities(authorities);
                role.setInherent(true);
                role.setLevel(3);
                roles.add(role);
                break;
            }
            case AuthorityUtil.COMMUNITY_PARTNER:{
                Set<Authority> authorities = setAuthority(AuthorityUtil.generateCommunityPartnerAuthority());
                role.setAuthorities(authorities);
                role.setInherent(true);
                role.setLevel(4);
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

    @Override
    public boolean isAdmin(User user) {
        Set<Role> roles = user.getRoles();
        if(roles!=null&&!roles.isEmpty()) {
            for (Role role : roles) {
                if (role.getRoleName().equals(AuthorityUtil.SUPER_ADMIN)
                        || role.getRoleName().equals(AuthorityUtil.SYSTEM_ADMIN)){
                    return true;
                }
            }
        }
        return false;
    }

}
