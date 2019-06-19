package ${packageName}.controller.${entityName?lower_case};

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import ${packageName}.utils.Result;
import ${packageName}.utils.MoonUtil;
import ${packageName}.utils.AuthorityUtil;
import ${packageName}.service.${entityName?lower_case}.${entityName}Service;
import ${packageName}.entity.${entityName?lower_case}.${entityName};
<#if entityName!="AccessToken">
import ${packageName}.service.accesstoken.AccessTokenService;
</#if>
<#if entityName!="User">
import ${packageName}.service.user.UserService;
</#if>
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* ${entityName}访问控制层
* @author ${author}
* @date ${date}
*/
@RestController
@RequestMapping("/api/${entityName?uncap_first}")
public class ${entityName}Controller {

<#if entityName!="AccessToken">
    @Autowired
    private AccessTokenService accessTokenService;
</#if>

<#if entityName!="User">
    @Autowired
    private UserService userService;
</#if>

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private ${entityName}Service ${entityName?uncap_first}Service;

    private final static Logger logger = LoggerFactory.getLogger(${entityName}Controller.class);

    /**
     * 新增${entityName}
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody JSONObject add${entityName}(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValid(authorization)==null){
           return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        ${entityName} ${entityName?uncap_first};

        try {
            ${entityName?uncap_first} = JSON.toJavaObject(param, ${entityName}.class);
            if (${entityName?uncap_first} == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getMessage());
        }

        return ${entityName?uncap_first}Service.add${entityName}(${entityName?uncap_first});
    }

    /**
     * 根据ID删除${entityName}
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject delete${entityName}(@PathVariable String id){
        if (id==null||StringUtils.isBlank(id)||id.length()<22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }

        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        return ${entityName?uncap_first}Service.delete${entityName}ById(id);
    }

    /**
     * 根据新${entityName}更新id已存在的${entityName}
     * @param id,new${entityName}
     */
    @PutMapping(value = "/{id}")
    public @ResponseBody JSONObject update${entityName}(@PathVariable("id") String id,@RequestBody JSONObject param){
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        String authorization = request.getHeader("Authorization");

        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.${entityName?upper_case}_UPDATE)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        ${entityName} new${entityName};
        try {
            new${entityName} = JSON.toJavaObject(param, ${entityName}.class);
            if (new${entityName} == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getMessage());
        }
        return ${entityName?uncap_first}Service.update${entityName}ById(id,new${entityName});
    }

    /**
     * 根据id获取${entityName}
     * @param id
     */
    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject get${entityName}ById(@PathVariable("id") String id){
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.${entityName?upper_case}_READ)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        return ${entityName?uncap_first}Service.find${entityName}ById(id);
    }

    /**
     * 分页获取${entityName}列表
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody JSONObject find${entityName}ByPager(@RequestBody JSONObject param){
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
        return ${entityName?uncap_first}Service.find${entityName}ByList(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 导出${entityName}Excel表格
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){
        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出${entityName}列表时出错,错误原因: "+e.getMessage());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);

        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出${entityName}Excel列表时出错" + e.getMessage());
                e.printStackTrace();
            }
            return;
        }
        ${entityName?uncap_first}Service.exportExcel(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    }

    <#list entityColumnList as entityColumn>
        <#if entityColumn.columnName=="sortNumber">
    /**
    * ${entityName}上下移
    *
    * @param id,param
    */
    @PutMapping(value = "/sort/{id}")
    public @ResponseBody JSONObject ${entityName?uncap_first}SortUpOrDown(@PathVariable String id,@RequestBody JSONObject param) {
        String upOrDown = param.getString("upOrDown");
        if (StringUtils.isBlank(id)||id.length()!=22||StringUtils.isBlank(upOrDown)||(!upOrDown.equals("上移")&&!upOrDown.equals("下移"))){
            return Result.fail(102,"参数错误","必填参数不能为空,或不正确的参数");
        }
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null) {
            return Result.fail(102, "权限认证失败", "您没有权限或token过期");
        }
        return ${entityName?uncap_first}Service.${entityName?uncap_first}SortUpOrDown(id, upOrDown);
    }
        </#if>
    </#list>
}