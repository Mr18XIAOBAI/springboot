package com.revisit.springboot.service.user.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.user.Authority;
import com.revisit.springboot.repository.user.AuthorityRepository;
import com.revisit.springboot.service.user.AuthorityService;
import com.revisit.springboot.utils.ExcelUtil;
import com.revisit.springboot.utils.PageableUtil;
import com.revisit.springboot.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Authority逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-04-19 14:13:48
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class AuthorityServiceImpl implements AuthorityService {

    @Autowired
    private AuthorityRepository authorityRepository;

    private final static Logger logger = LoggerFactory.getLogger(AuthorityServiceImpl.class);

    // @Override
    // public JSONObject addAuthority(Authority authority) {
     //    logger.info("新增Authority");
    //
    //
	// 	//保存此对象
	// 	authority = authorityRepository.save(authority);
    //
	// 	if (StringUtils.isBlank(authority.getId())){
     //        return Result.fail(110,"系统错误","新增Authority失败,请联系管理员");
     //    }
    //
	// 	JSONObject authorityBean = (JSONObject)JSON.toJSON(authority);
     //    return Result.success(200,"新增Authority成功",authorityBean);
    // }
    //
    // @Override
    // public JSONObject deleteAuthorityById(String id){
    //
	// 	logger.info("删除Authority: " + id);
    //
	// 	List<String> ids = new ArrayList<>();
	// 	if (StringUtils.contains(id,(","))){
	// 		String[] split = StringUtils.split(id,",");
    	// 	for (String s :split) {
	// 			Authority authority = authorityRepository.findById(s).orElse(null);
	// 			if(authority!=null){
	// 				ids.add(s);
    	// 		}
	// 		}
    	// }else{
	// 		Authority authority = authorityRepository.findById(id).orElse(null);
	// 		if (authority == null) {
	// 			return Result.fail(102,"查询失败","权限对象不存在");
	// 		}
    	// 	ids.add(id);
	// 	}
    //
	// 	if (ids==null || ids.isEmpty()){
	// 		return Result.fail(102,"查询失败","权限对象不存在");
	// 	}
    //
	// 	int authorityRows = authorityRepository.deleteByIds(ids);
    //
    	// return Result.success(200,"删除Authority成功","批量删除Authority成功,共删除Authority: " + authorityRows + " 个");
	// }

    // @Override
    // public JSONObject updateAuthorityById(String id,Authority newAuthority){
		// logger.info("更新Authority: " + id);
    //
		// Authority oldAuthority = authorityRepository.findById(id).orElse(null);
		// if (oldAuthority==null){
		// 	return Result.fail(102,"查询失败","权限对象不存在");
		// }
    //
		// //设置不更新字段,默认空值会被源对象替换
		// String ignoreProperties = "";
    //
		// //开始合并对象
		// JavaBeanUtil.copyProperties(oldAuthority,newAuthority,ignoreProperties);
    //
    // 	newAuthority = authorityRepository.save(newAuthority);
    //
    // 	JSONObject authorityBean = (JSONObject)JSON.toJSON(newAuthority);
    //
    // 	return Result.success(200,"更新成功",authorityBean);
    // }

	@Override
    public JSONObject findAuthorityById(String id){
    	logger.info("获取权限: " + id);

		Authority authority = authorityRepository.findById(id).orElse(null);

		if(authority == null){
    		return Result.fail(102,"查询失败","权限对象不存在");
    	}

    	JSONObject authorityBean = (JSONObject)JSON.toJSON(authority);

		return Result.success(200,"查询成功",authorityBean);
    }

	@Override
    public JSONObject findAuthorityByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取权限列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}
		Page authorityListPage = findAuthorityList(keyword,orderBy,beginTime,endTime,page,rows);

		if(authorityListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}

    	JSONObject result = new JSONObject();
		result.put("rowsTotal",authorityListPage.getTotalElements());
		result.put("page",authorityListPage.getNumber()+1);
		result.put("rows",authorityListPage.getSize());
		result.put("authorityList",authorityListPage.getContent());
		return Result.success(200,"查询成功",result);
	}

	private Page findAuthorityList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page authorityListPage = authorityRepository.findAll(new Specification<Authority>() {
			@Override
			public Predicate toPredicate(Root<Authority> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicateList = new ArrayList<>();
        		//指定查询对象
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

        if (!authorityListPage.hasContent()){
        	return null;
        }

		return authorityListPage;
	}

	@Override
	public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page authorityListPage = findAuthorityList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<Authority> authorityList = new ArrayList<>();

		if (authorityListPage!=null){
			authorityList.addAll(authorityListPage.getContent());
		}

		if (authorityList!=null&&!authorityList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(authorityList, "权限列表", "权限列表",Authority.class, "权限列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}

}