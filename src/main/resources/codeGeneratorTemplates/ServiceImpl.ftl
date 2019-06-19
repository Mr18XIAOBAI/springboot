package ${packageName}.service.${entityName?lower_case}.impl;
import ${packageName}.entity.${entityName?lower_case}.${entityName};
import ${packageName}.repository.${entityName?lower_case}.${entityName}Repository;
import ${packageName}.service.${entityName?lower_case}.${entityName}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ${packageName}.utils.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletResponse;


/**
 * ${entityName}逻辑层接口类
 * @author ${author}
 * @date ${date}
 */
@Service
public class ${entityName}ServiceImpl implements ${entityName}Service {

    @Autowired
    private ${entityName}Repository ${entityName?uncap_first}Repository;

    private final static Logger logger = LoggerFactory.getLogger(${entityName}ServiceImpl.class);

    @Override
    public JSONObject add${entityName}(${entityName} ${entityName?uncap_first}) {
        logger.info("新增${entityName}");

		<#list entityColumnList as entityColumn>
			<#if entityColumn.columnName=="sortNumber">
		Integer maxSortNumber = ${entityName?uncap_first}Repository.findMaxSortNumber();
		//设置为最大的排序号
		if(maxSortNumber==null){
			${entityName?uncap_first}.setSortNumber(0);
		}else{
			${entityName?uncap_first}.setSortNumber(maxSortNumber+1);
		}
			</#if>
		</#list>

		//保存此对象
		${entityName?uncap_first} = ${entityName?uncap_first}Repository.save(${entityName?uncap_first});

		// JSONObject ${entityName?uncap_first}Bean = (JSONObject)JSON.toJSON(${entityName?uncap_first});

        if (StringUtils.isBlank(${entityName?uncap_first}.getId())){
            return Result.fail(110,"系统错误","新增${entityName}失败,请联系管理员");
        }

        return Result.success(200,"新增${entityName}成功",${entityName?uncap_first});
    }

    @Override
    public JSONObject delete${entityName}ById(String id){

		logger.info("删除${entityName}: " + id);

		List<String> ids = new ArrayList<>();
		if (StringUtils.contains(id,(","))){
			String[] split = StringUtils.split(id,",");
    		for (String s :split) {
				${entityName} ${entityName?uncap_first} = ${entityName?uncap_first}Repository.findById(s).orElse(null);
				if(${entityName?uncap_first}!=null){
				<#list entityColumnList as entityColumn>
					<#if entityColumn.columnName=="sortNumber">
					//将比要删除的对象的排序号大的全部-1
					${entityName?uncap_first}Repository.allSortNumberMinusOneBySortNumber(${entityName?uncap_first}.getSortNumber());
					</#if>
				</#list>
					ids.add(s);
    			}
			}
    	}else{
			${entityName} ${entityName?uncap_first} = ${entityName?uncap_first}Repository.findById(id).orElse(null);
			if (${entityName?uncap_first} == null) {
				return Result.fail(102,"查询失败","${entityName}对象不存在");
			}
			<#list entityColumnList as entityColumn>
				<#if entityColumn.columnName=="sortNumber">
    		//将比要删除的对象的排序号大的全部-1
			${entityName?uncap_first}Repository.allSortNumberMinusOneBySortNumber(${entityName?uncap_first}.getSortNumber());
				</#if>
			</#list>
    		ids.add(id);
		}

		if (ids==null || ids.isEmpty()){
			return Result.fail(102,"查询失败","${entityName}对象不存在");
		}

		int ${entityName?uncap_first}Rows = ${entityName?uncap_first}Repository.deleteByIds(ids);

    	return Result.success(200,"删除${entityName}成功","批量删除${entityName}成功,共删除${entityName}: " + ${entityName?uncap_first}Rows + " 个");
	}

    @Override
    public JSONObject update${entityName}ById(String id,${entityName} new${entityName}){
		logger.info("更新${entityName}: " + id);

		${entityName} old${entityName} = ${entityName?uncap_first}Repository.findById(id).orElse(null);
		if (old${entityName}==null){
			return Result.fail(102,"查询失败","${entityName?uncap_first}对象不存在");
		}

		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "<#list entityColumnList as entityColumn><#if entityColumn.columnName=="sortNumber">sortNumber</#if></#list>";

		//开始合并对象
		JavaBeanUtil.copyProperties(old${entityName},new${entityName},ignoreProperties);

    	new${entityName} = ${entityName?uncap_first}Repository.save(new${entityName});

    	JSONObject ${entityName?uncap_first}Bean = (JSONObject)JSON.toJSON(new${entityName});

    	return Result.success(200,"更新成功",${entityName?uncap_first}Bean);
    }

	@Override
    public JSONObject find${entityName}ById(String id){
    	logger.info("获取${entityName}: " + id);

		${entityName} ${entityName?uncap_first} = ${entityName?uncap_first}Repository.findById(id).orElse(null);

		if(${entityName?uncap_first} == null){
    		return Result.fail(102,"查询失败","${entityName?uncap_first}对象不存在");
    	}

    	JSONObject ${entityName?uncap_first}Bean = (JSONObject)JSON.toJSON(${entityName?uncap_first});

		return Result.success(200,"查询成功",${entityName?uncap_first}Bean);
    }

	@Override
    public JSONObject find${entityName}ByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取${entityName}列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}
		Page ${entityName?uncap_first}ListPage = find${entityName}List(keyword,orderBy,beginTime,endTime,page,rows);

		if(${entityName?uncap_first}ListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}

    	JSONObject result = new JSONObject();
		result.put("rowsTotal",${entityName?uncap_first}ListPage.getTotalElements());
		result.put("page",${entityName?uncap_first}ListPage.getNumber()+1);
		result.put("rows",${entityName?uncap_first}ListPage.getSize());
		result.put("${entityName?uncap_first}List",${entityName?uncap_first}ListPage.getContent());
		return Result.success(200,"查询成功",result);
	}

	private Page find${entityName}List(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page ${entityName?uncap_first}ListPage = ${entityName?uncap_first}Repository.findAll(new Specification<${entityName}>() {
			@Override
			public Predicate toPredicate(Root<${entityName}> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicateList = new ArrayList<>();
        		//指定查询参数
        		if (StringUtils.isNotBlank(keyword)) {
        			predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("id"), "%" + keyword + "%")
        			, criteriaBuilder.like(root.get("id"), "%" + keyword + "%")
        			, criteriaBuilder.like(root.get("id"), "%" + keyword + "%")));
        		}

        		if (beginTime != null) {
        			predicateList.add(criteriaBuilder.and(criteriaBuilder.greaterThan(root.get("createTime"), beginTime)));
        		}

				if (endTime != null) {
        			predicateList.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get("createTime"), endTime)));
				}
        		return query.where(predicateList.toArray(new Predicate[predicateList.size()])).getRestriction();
        	}
        }, pageable);

        if (!${entityName?uncap_first}ListPage.hasContent()){
        	return null;
        }

		return ${entityName?uncap_first}ListPage;
	}

	@Override
	public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page ${entityName?uncap_first}ListPage = find${entityName}List(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<${entityName}> ${entityName?uncap_first}List = new ArrayList<>();

		if (${entityName?uncap_first}ListPage!=null){
			${entityName?uncap_first}List.addAll(${entityName?uncap_first}ListPage.getContent());
		}

		if (${entityName?uncap_first}List!=null&&!${entityName?uncap_first}List.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(${entityName?uncap_first}List, "${entityName?uncap_first}List列表", "${entityName?uncap_first}List列表",${entityName}.class, "${entityName?uncap_first}List列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}
	<#list entityColumnList as entityColumn>
			<#if entityColumn.columnName=="sortNumber">
	@Override
	public JSONObject ${entityName?uncap_first}SortUpOrDown(String id, String upOrDown) {
		${entityName} ${entityName?uncap_first} = ${entityName?uncap_first}Repository.findById(id).orElse(null);
		if (${entityName?uncap_first}==null){
			return Result.fail(102,"查询失败","该${entityName?uncap_first}对象不存在");
		}
		Integer sortNumber = ${entityName?uncap_first}.getSortNumber();
		if (sortNumber==0&&upOrDown.equals("上移")){
			return Result.fail(102,"排序失败","该${entityName?uncap_first}当前已在最顶端");
		}
		int maxSortNumber = ${entityName?uncap_first}Repository.findMaxSortNumber();

		if (sortNumber==maxSortNumber&&upOrDown.equals("下移")){
			return Result.fail(102,"排序失败","该${entityName?uncap_first}当前已在最末端");
		}
		if (upOrDown.equals("上移")){
			sortNumber = sortNumber-1;
		}

		if (upOrDown.equals("下移")){
			sortNumber = sortNumber+1;
		}
		${entityName} brothers${entityName} = ${entityName?uncap_first}Repository.findBySortNumber(sortNumber);

		if (brothers${entityName}!=null){
			brothers${entityName}.setSortNumber(${entityName?uncap_first}.getSortNumber());
			${entityName?uncap_first}.setSortNumber(sortNumber);
			${entityName?uncap_first}Repository.save(brothers${entityName});
			${entityName?uncap_first} = ${entityName?uncap_first}Repository.save(${entityName?uncap_first});
		}

		return Result.success(200,"排序成功",${entityName?uncap_first});
	}
			</#if>
	</#list>

	<#if entityName=="User">
	@Override
	public boolean containAuthority(String token, String authName){

		/**
		 * 获取用户对象
		 */
		AccessToken accessToken = accessTokenDao.get(token);
		if(accessToken==null){
			LOGGER.info("非法操作,token为空");
			return false;
		}
		String userId = accessToken.getUserId();
		${entityName} ${entityName?uncap_first} = ${entityName?uncap_first}Dao.get(userId);
		if(${entityName?uncap_first} == null){
			LOGGER.info("获取不到对应的用户对象");
			return false;
		}

		${entityName}Authority auth = ${entityName?uncap_first}AuthorityDao.getByName(authName);
		if(auth == null){
			LOGGER.info("获取不了该权限对象：" + authName);
			return false;
		}

		Set<${entityName}Role> roles = ${entityName?uncap_first}.getRoles();
    	for(${entityName}Role role : roles){
    		Set<${entityName}Authority> roleBean = role.getUserAuthorities();
        	if(roleBean.contains(auth)){
        		return true;
        	}
        }
		return false;
	}
	</#if>
}