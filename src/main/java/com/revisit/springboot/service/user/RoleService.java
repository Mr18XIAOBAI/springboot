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
import com.revisit.springboot.entity.user.Role;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 〈service.RoleService〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */

public interface RoleService {

    //用户登录
    // JSONObject userLogin(String userName, String email, String mobile, String smsCode, String password);

    //用户在小程序登录
    // JSONObject loginInWeChatMiniProgram(String openId, String sessionKey, String rawData, String signature, String encryptedData, String iv, String referrerId);

    //用户绑定手机
    // JSONObject bindingMobile(String userId, String mobile, String realName, Date birthday, String address);

    //新增用户
    JSONObject addRole(Role role, String authorityIds);

    //新增超级管理员
    // JSONObject addAdmin(String userName, String email, String mobile, String password);

    //根据ID删除角色
    JSONObject deleteRoleById(String id);

    //根据ID更新角色
    JSONObject updateRoleById(String id, Role newRole, String authorityIds);

    //根据ID搜索角色
    JSONObject findRoleById(String id);

    //根据条件搜索角色
    JSONObject findRoleByList(String keyword, String orderBy, Date beginTime, Date endTime, Integer page, Integer rows);

    //导出excel表格
    void exportExcel(String keyword, String orderBy, Date beginTime, Date endTime, HttpServletResponse response);

    // JSONObject rechargeMember(String userId, String rechargeName);
}
