package com.revisit.springboot.controller.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.user.AuthorityService;
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
* Authority访问控制层
* @author Revisit-Moon
* @date 2019-04-19 14:13:48
*/
@RestController
@RequestMapping("/api/authority")
public class AuthorityController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private AuthorityService authorityService;

    private final static Logger logger = LoggerFactory.getLogger(AuthorityController.class);

    // /**
    //  * 新增Authority
    //  * @param param
    //  */
    // @PostMapping(value = "/add")
    // public @ResponseBody JSONObject addAuthority(@RequestBody JSONObject param){
    //     String authorization = request.getHeader("Authorization");
    //     if (!accessTokenService.isValid(authorization)){
    //        return Result.fail(108,"权限认证失败","您没有权限或token过期");
    //     }
    //
    //     Authority authority;
    //
    //     try {
    //         authority = JSON.toJavaObject(param, Authority.class);
    //         if (authority == null){
    //             return Result.fail(102,"参数错误","必填参数不能为空");
    //         }
    //     }catch (Exception e){
    //         return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
    //     }
    //
    //     return authorityService.addAuthority(authority);
    // }
    //
    // /**
    //  * 根据ID删除Authority
    //  * @param id
    //  */
    // @DeleteMapping(value = "/{id}")
    // public @ResponseBody JSONObject deleteAuthority(@PathVariable String id){
    //     String authorization = request.getHeader("Authorization");
    //     if (!accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)){
    //         return Result.fail(108,"权限认证失败","您没有权限或token过期");
    //     }
    //     if (id==null||StringUtils.isBlank(id)||id.length()<22){
    //         return Result.fail(102,"参数错误","必填参数不能为空");
    //     }
    //     return authorityService.deleteAuthorityById(id);
    // }
    //
    // /**
    //  * 根据新Authority更新id已存在的Authority
    //  * @param id,newAuthority
    //  */
    // @PutMapping(value = "/{id}")
    // @ResponseBody
    // public JSONObject updateAuthority(@PathVariable("id") String id,@RequestBody JSONObject param){
    //     String authorization = request.getHeader("Authorization");
    //     if (!accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.AUTHORITY_UPDATE)){
    //         return Result.fail(108,"权限认证失败","您没有权限或token过期");
    //     }
    //     if (id==null||StringUtils.isBlank(id)||id.length()!=22){
    //         return Result.fail(102,"参数错误","必填参数不能为空");
    //     }
    //     Authority newAuthority;
    //     try {
    //         newAuthority = JSON.toJavaObject(param, Authority.class);
    //         if (newAuthority == null){
    //             return Result.fail(102,"参数错误","必填参数不能为空");
    //         }
    //     }catch (Exception e){
    //         return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
    //     }
    //     return authorityService.updateAuthorityById(id,newAuthority);
    // }

    /**
     * 根据id获取Authority
     * @param id
     */
    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject getAuthorityById(@PathVariable("id") String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return authorityService.findAuthorityById(id);
    }

    /**
     * 分页获取Authority列表
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody JSONObject findAuthorityByPager(@RequestBody JSONObject param){
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

        return authorityService.findAuthorityByList(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 导出AuthorityExcel表格
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){
        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出Authority列表时出错,错误原因: "+e.getCause());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);

        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出AuthorityExcel列表时出错" + e.getCause());
                e.printStackTrace();
            }
            return;
        }
        authorityService.exportExcel(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    }

}