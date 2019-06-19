package com.revisit.springboot.controller.assemblerelation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.assemblerelation.AssembleRelation;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.assemblerelation.AssembleRelationService;
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
* 拼团关系访问控制层
* @author Revisit-Moon
* @date 2019-05-06 10:22:48
*/
@RestController
@RequestMapping("/api/assembleRelation")
public class AssembleRelationController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private AssembleRelationService assembleRelationService;

    private final static Logger logger = LoggerFactory.getLogger(AssembleRelationController.class);

    /**
     * 新增AssembleRelation
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody JSONObject addAssembleRelation(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        AssembleRelation assembleRelation;

        try {
            assembleRelation = JSON.toJavaObject(param, AssembleRelation.class);
            if (assembleRelation == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }

        return assembleRelationService.addAssembleRelation(assembleRelation);
    }

    /**
     * 根据ID删除AssembleRelation
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject deleteAssembleRelation(@PathVariable String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()<22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return assembleRelationService.deleteAssembleRelationById(id);
    }

    /**
     * 根据新AssembleRelation更新id已存在的AssembleRelation
     * @param id,newAssembleRelation
     */
    @PutMapping(value = "/{id}")
    @ResponseBody
    public JSONObject updateAssembleRelation(@PathVariable("id") String id,@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        AssembleRelation newAssembleRelation;
        try {
            newAssembleRelation = JSON.toJavaObject(param, AssembleRelation.class);
            if (newAssembleRelation == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }
        return assembleRelationService.updateAssembleRelationById(id,newAssembleRelation);
    }

    /**
     * 根据id获取AssembleRelation
     * @param id
     */
    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject getAssembleRelationById(@PathVariable("id") String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return assembleRelationService.findAssembleRelationById(id);
    }

    /**
     * 分页获取AssembleRelation列表
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody JSONObject findAssembleRelationByPager(@RequestBody JSONObject param){
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

        return assembleRelationService.findAssembleRelationByList(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 导出AssembleRelationExcel表格
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){
        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出AssembleRelation列表时出错,错误原因: "+e.getCause());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);

        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出AssembleRelationExcel列表时出错" + e.getCause());
                e.printStackTrace();
            }
            return;
        }
        assembleRelationService.exportExcel(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    }

}