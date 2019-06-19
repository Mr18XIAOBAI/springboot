package com.revisit.springboot.controller.partyinfo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.partyinfo.PartyInfo;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.partyinfo.PartyInfoService;
import com.revisit.springboot.service.user.UserService;
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
* PartyInfo访问控制层
* @author Revisit-Zhang
* @date 2019-06-19 11:54:16
*/
@RestController
@RequestMapping("/api/partyInfo")
public class PartyInfoController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private PartyInfoService partyInfoService;

    private final static Logger logger = LoggerFactory.getLogger(PartyInfoController.class);

    /**
     * 新增PartyInfo
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody JSONObject addPartyInfo(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValid(authorization)==null){
           return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        PartyInfo partyInfo;

        try {
            partyInfo = JSON.toJavaObject(param, PartyInfo.class);
            if (partyInfo == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getMessage());
        }

        return partyInfoService.addPartyInfo(partyInfo);
    }

    /**
     * 根据ID删除PartyInfo
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject deletePartyInfo(@PathVariable String id){
        if (id==null||StringUtils.isBlank(id)||id.length()<22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }

        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        return partyInfoService.deletePartyInfoById(id);
    }

    /**
     * 根据新PartyInfo更新id已存在的PartyInfo
     * @param id,newPartyInfo
     */
    @PutMapping(value = "/{id}")
    public @ResponseBody JSONObject updatePartyInfo(@PathVariable("id") String id,@RequestBody JSONObject param){
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        String authorization = request.getHeader("Authorization");

        if (accessTokenService.isValidTokenAndAuthorities(AuthorityUtil.RESOURCES_READ)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        PartyInfo newPartyInfo;
        try {
            newPartyInfo = JSON.toJavaObject(param, PartyInfo.class);
            if (newPartyInfo == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getMessage());
        }
        return partyInfoService.updatePartyInfoById(id,newPartyInfo);
    }

    /**
     * 根据id获取PartyInfo
     * @param id
     */
    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject getPartyInfoById(@PathVariable("id") String id){
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndAuthorities(AuthorityUtil.RESOURCES_READ)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        return partyInfoService.findPartyInfoById(id);
    }

    /**
     * 分页获取PartyInfo列表
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody JSONObject findPartyInfoByPager(@RequestBody JSONObject param){
        String keyword = param.getString("keyword");
        String orderBy = param.getString("orderBy");

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            return Result.fail(102, "参数错误", "日期格式化异常");
        }

        Integer page = param.getInteger("page");
        Integer rows = param.getInteger("rows");
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        return partyInfoService.findPartyInfoByList(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 导出PartyInfoExcel表格
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){
        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出PartyInfo列表时出错,错误原因: "+e.getMessage());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);

        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出PartyInfoExcel列表时出错" + e.getMessage());
                e.printStackTrace();
            }
            return;
        }
        partyInfoService.exportExcel(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    }

}