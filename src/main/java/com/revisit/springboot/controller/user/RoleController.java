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
import com.revisit.springboot.entity.user.Role;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.sms.SMSService;
import com.revisit.springboot.service.user.RoleService;
import com.revisit.springboot.utils.AuthorityUtil;
import com.revisit.springboot.utils.MoonUtil;
import com.revisit.springboot.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * 〈角色controller层〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private SMSService smsService;

    @Autowired
    private HttpServletRequest request;

    private final static Logger logger = LoggerFactory.getLogger(RoleController.class);


    @PostMapping(value = "/add")
    public @ResponseBody JSONObject addRole(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String authorityIds = param.getString("authorityIds");
        param.remove("authorityIds");
        Role role;
        try {
            role = JSONObject.toJavaObject(param,Role.class);
            if (role==null){
                return Result.fail(102,"参数错误","参数不能为空");
            }
            String roleName = role.getRoleName();

            if (StringUtils.isBlank(roleName)){
                return Result.fail(102,"参数错误","角色名不能空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换角色对象出错");
        }

        if (StringUtils.isBlank(role.getRoleName())){
            return Result.fail(102,"参数错误","角色名不能为空");
        }

        return roleService.addRole(role,authorityIds);
    }

    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject deleteRoleById(@PathVariable String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        return roleService.deleteRoleById(id);
    }

    @PutMapping(value = "/{id}")
    public @ResponseBody JSONObject updateRole(@PathVariable String id,@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.USER_UPDATE)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        String authorityIds = param.getString("authorityIds");
        param.remove("authorityIds");
        Role role;
        try {
            role = JSON.toJavaObject(param, Role.class);
            if (role == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }

        return roleService.updateRoleById(id,role,authorityIds);
    }

    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject getRole(@PathVariable String id){
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization)){
            return Result.fail(108, "权限认证失败", "缺少必填参数");
        }
        if (accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.USER_READ)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        return roleService.findRoleById(id);
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

        return roleService.findRoleByList(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    @GetMapping(value = "/export")
    public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){
        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出User表格时错误,错误原因: "+e.getCause());
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

        roleService.exportExcel(keyword, orderBy, timeRangeDate.get(0), timeRangeDate.get(1), response);

    }
}
