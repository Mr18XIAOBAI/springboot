/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: UserController
 * Author:   Revisit-Moon
 * Date:     2019/1/29 11:54 AM
 * Description: UserController
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/29 11:54 AM        1.0              描述
 */

package com.revisit.springboot.controller.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.component.ajaxhandler.CustomizeSessionContext;
import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.service.sms.SMSService;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.user.UserService;
import com.revisit.springboot.utils.AuthorityUtil;
import com.revisit.springboot.utils.CookiesUtil;
import com.revisit.springboot.utils.MoonUtil;
import com.revisit.springboot.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * 〈用户controller层〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private SMSService smsService;

    @Autowired
    private HttpServletRequest request;

    private final static Logger logger = LoggerFactory.getLogger(User.class);


    @PostMapping(value = "/register")
    public @ResponseBody JSONObject register(@RequestBody JSONObject param){
        String code = param.getString("verificationCode");
        param.remove("verificationCode");

        User user;

        try {
            user = JSONObject.toJavaObject(param,User.class);
            if (user==null){
                return Result.fail(102,"参数错误","参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","无法转换用户对象");
        }

        if (StringUtils.isBlank(user.getPassword())||user.getPassword().length()<6){
            return Result.fail(102,"参数错误","密码不能为空");
        }
        String sessionId = CookiesUtil.getJSessionId(request);
        if (StringUtils.isNotBlank(user.getMobile())){
            if (StringUtils.isNotBlank(code)) {
                if (!smsService.verifySMS(user.getMobile(), code,sessionId)){
                    return Result.fail(102,"参数错误","短信验证码错误");
                }
            }else{
                return Result.fail(102,"参数错误","手机注册必须提供短信验证码");
            }
        }

        return userService.addUser(user);
    }


    @PostMapping(value = "/binding/mobile")
    public @ResponseBody JSONObject bindingMobile(@RequestBody JSONObject param){
        String code = param.getString("verificationCode");
        String sessionId = CookiesUtil.getJSessionId(request);
        String authorization = request.getHeader("Authorization");
        AccessToken accessToken = accessTokenService.findAccessTokenByIdAndIsValid(authorization);
        if(accessToken==null){
            return Result.fail(102,"参数错误","您没有权限或token过期");
        }
        String userId = accessToken.getUserId();
        String mobile = param.getString("mobile");
        String realName = param.getString("realName");

        if (StringUtils.isBlank(code)
                ||StringUtils.isBlank(mobile)
                ||StringUtils.isBlank(realName)){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        if (!smsService.verifySMS(mobile, code,sessionId)){
            return Result.fail(102,"参数错误","短信验证码错误");
        }
        return userService.bindingMobile(userId,mobile,realName);
    }

    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject getUser(@PathVariable String id){
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization)){
            return Result.fail(102, "权限认证失败", "缺少必填参数");
        }
        if (accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.USER_READ)==null) {
            return Result.fail(102, "权限认证失败", "您没有权限或token过期");
        }
        return userService.findUserById(authorization,id);
    }

    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject deleteUser(@PathVariable String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        return userService.deleteUserById(id);
    }

    @PutMapping(value = "/{id}")
    public @ResponseBody JSONObject updateUser(@PathVariable String id,@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.USER_UPDATE)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        param.put("userId",id);
        if (accessTokenService.isSelf(authorization,param)==null){
            return Result.fail(108,"权限认证失败","您不是用户本人");
        }
        param.remove("userId");
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        User user;
        try {
            user = JSON.toJavaObject(param, User.class);
            if (user == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getMessage());
        }

        return userService.updateUserById(id,user);
    }

    @PostMapping(value = "/login")
    public synchronized @ResponseBody JSONObject login(@RequestBody JSONObject param){
        String userName = param.getString("userName");
        String mobile = param.getString("mobile");
        String email = param.getString("email");
        String smsCode = param.getString("smsCode");
        String password = param.getString("password");
        return userService.userLogin(userName,email,mobile,smsCode,password);
    }

    @PutMapping(value =  "/specifiedUserRole/{id}")
    public synchronized @ResponseBody JSONObject specifiedUserRole(@PathVariable String id,@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.MEMBER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (StringUtils.isBlank(id)){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        String roleName = param.getString("roleName");
        if (StringUtils.isBlank(roleName)){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return userService.specifiedUserRole(id,roleName);
    }

    @PostMapping(value = "/addSuperAdmin")
    public @ResponseBody JSONObject addAdmin(@RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String userName = param.getString("userName");
        String mobile = param.getString("mobile");
        String email = param.getString("email");
        String password = param.getString("password");
        if (StringUtils.isBlank(userName)
                &&StringUtils.isBlank(email)
                &&StringUtils.isBlank(mobile)){
            return Result.fail(102,"参数错误","请输入账号");
        }
        return userService.addAdmin(userName,email,mobile,password);
    }

    @PostMapping(value = "/addUserToSomeRole")
    public @ResponseBody JSONObject addUserToSomeRole(@RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String userName = param.getString("userName");
        String mobile = param.getString("mobile");
        String email = param.getString("email");
        String password = param.getString("password");
        String roleName = param.getString("roleName");
        if (StringUtils.isBlank(userName)
                &&StringUtils.isBlank(email)
                &&StringUtils.isBlank(mobile)){
            return Result.fail(102,"参数错误","请输入账号");
        }
        if (StringUtils.isBlank(roleName)){
            return Result.fail(102,"参数错误","角色名不能为空");
        }
        return userService.addUserToSomeRole(userName,email,mobile,password,roleName);
    }

    @PostMapping(value = "/list")
    public @ResponseBody JSONObject list(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String keyword = param.getString("keyword");
        String orderBy = param.getString("orderBy");

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            return Result.fail(102, "参数错误", "日期格式化异常");
        }

        Integer page = param.getInteger("page");
        Integer rows = param.getInteger("rows");

        return userService.findUserByList(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }


    @PostMapping(value="/wechatMiniProgram/login")
    public @ResponseBody JSONObject loginInWeChat(@RequestBody  JSONObject param){
        String session3rd = param.getString("session3rd");
        String rawData = param.getString("rawData");
        String signature = param.getString("signature");
        String referrerId = param.getString("referrerId");
        HttpSession session = CustomizeSessionContext.getSessionAttribute(session3rd);
        if (session==null) {
            return Result.fail(102,"非法操作","不存在的session");
        }
        String attribute = (String) session.getAttribute(session3rd);


        JSONObject sessionJson = JSONObject.parseObject(attribute);
        String sessionKey = sessionJson.getString("session_key");
        String openId = sessionJson.getString("openid");
        String encryptedData = param.getString("encryptedData");
        String iv = param.getString("iv");
        if (StringUtils.isBlank(openId)
                ||StringUtils.isBlank(sessionKey)
                ||StringUtils.isBlank(rawData)
                ||StringUtils.isBlank(signature)
                ||StringUtils.isBlank(encryptedData)
                ||StringUtils.isBlank(iv)){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        CustomizeSessionContext.deleteSession(session);
        return userService.loginInWeChatMiniProgram(openId, sessionKey, rawData, signature, encryptedData, iv,referrerId);
    }

    @GetMapping(value = "/export")
    public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){

        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出User表格时错误,错误原因: "+e.getMessage());
            }
            return;
        }
        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);
        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出UserExcel列表时出错");
                e.printStackTrace();
            }
            return;
        }

        userService.exportExcel(keyword, orderBy, timeRangeDate.get(0), timeRangeDate.get(1), response);
    }
}
