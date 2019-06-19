package ${packageName}.service.${entityName?lower_case};
import ${packageName}.entity.${entityName?lower_case}.${entityName};
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSONObject;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ${entityName}接口类
 * @author ${author}
 * @date ${date}
 */
public interface ${entityName}Service {

    //新增${entityName}
    JSONObject add${entityName}(${entityName} ${entityName?uncap_first});

    //根据ID删除${entityName}
    JSONObject delete${entityName}ById(String id);

    //根据新${entityName}更新id已存在的${entityName}
    JSONObject update${entityName}ById(String id,${entityName} new${entityName});

    //根据ID获取${entityName}
    JSONObject find${entityName}ById(String id);

    //根据ids集合批量获取${entityName}
    //JSONObject find${entityName}ListByIds(List<String> ids);

    //分页获取${entityName}列表
    JSONObject find${entityName}ByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows);
    <#if entityName=="User">
    //根据${entityName}和权限名进行权限验证
    //boolean containAuthority(String token, String authName);
    </#if>
    //导出excel表格
    void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response);
    <#list entityColumnList as entityColumn>
        <#if entityColumn.columnName=="sortNumber">
    //${entityName}上下移
    JSONObject ${entityName?uncap_first}SortUpOrDown(String id, String upOrDown);
        </#if>
    </#list>
}