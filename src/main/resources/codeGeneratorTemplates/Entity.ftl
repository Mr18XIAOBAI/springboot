package ${packageName}.entity.${entityName?lower_case};
import ${packageName}.entity.BasicEntity;
import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import lombok.Data;
/**
 * <${entityName}实体类>
 * @author ${author}
 * @date ${date}
 */
@Data
@Entity
@Table(name="${tableName}")
public class ${entityName} extends BasicEntity {

<#if entityColumnList?exists>
<#list entityColumnList as entityColumn>
<#if entityColumn.columnName!='id'&&entityColumn.columnName!='createTime'&&entityColumn.columnName!='updateTime'>
    <#if entityColumn.remark?exists>
    /**
     * ${entityColumn.remark}
     */
    </#if>
    <#if (entityColumn.typeName?exists)>
	@Column(name = "${entityColumn.changeColumnName}"<#if (entityColumn.typeName=='Boolean')>,columnDefinition="bit(1) DEFAULT false"</#if>)
    private ${entityColumn.typeName} <#if entityColumn.columnName=="package">${entityColumn.columnName}Str<#else>${entityColumn.columnName}</#if>;
    </#if>
</#if>
</#list>
</#if>
}