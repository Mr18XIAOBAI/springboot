/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: UserService
 * Author:   Revisit-Moon
 * Date:     2019/1/29 9:51 AM
 * Description: service.UserService
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/29 9:51 AM        1.0              描述
 */

package com.revisit.springboot.service.user;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.user.User;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 〈service.UserService〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */

public interface UserService {

    //用户登录
    JSONObject userLogin(String userName,String email,String mobile,String smsCode,String password);

    //用户在小程序登录
    JSONObject loginInWeChatMiniProgram(String openId,String sessionKey,String rawData,String signature,String encryptedData,String iv,String referrerId);

    //用户绑定手机
    JSONObject bindingMobile(String userId,String mobile,String realName);

    //新增用户
    JSONObject addUser(User user);

    //新增超级管理员
    JSONObject addAdmin(String userName,String email,String mobile,String password);

    //根据ID删除用户
    JSONObject deleteUserById(String id);

    //根据ID更新用户
    JSONObject updateUserById(String id,User user);

    //根据ID搜索用户
    JSONObject findUserById(String authorization,String id);

    //根据条件搜索用户
    JSONObject findUserByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page,Integer rows);

    //导出excel表格
    void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response);

    //指定用户成为某个角色
    JSONObject specifiedUserRole(String id, String roleName);

    //新增用户且成为某个角色
    JSONObject addUserToSomeRole(String userName, String email, String mobile, String password, String roleName);

    //判断是否管理员
    boolean isAdmin(User user);

}
