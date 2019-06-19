package ${packageName}.repository.${entityName?lower_case};
import ${packageName}.entity.${entityName?lower_case}.${entityName};

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
/**
* ${entityName}持久层接口类
* @author ${author}
* @date ${date}
*/
@Transactional
public interface ${entityName}Repository extends JpaRepository<${entityName},String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from ${entityName} ${entityName?uncap_first} where ${entityName?uncap_first}.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);
<#list entityColumnList as entityColumn>
    <#if entityColumn.columnName=="sortNumber">

    //获取最大的排序号,如果为空则返回0
    @Query("SELECT MAX(sortNumber) FROM ${entityName}")
    Integer findMaxSortNumber();

    //根据排序号获取对象
    ${entityName} findBySortNumber(int sortNumber);

    //根据排序号和关联ID把比排序号大的所有列的排序号-1
    @Modifying
    @Query(value = "UPDATE ${tableName} SET sort_number = sort_number-1 WHERE sort_number > :sortNumber ",nativeQuery = true)
    int allSortNumberMinusOneBySortNumber(@Param("sortNumber") int sortNumber);

    </#if>
</#list>
}