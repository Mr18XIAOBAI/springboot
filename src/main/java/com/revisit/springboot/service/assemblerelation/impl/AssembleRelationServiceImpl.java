package com.revisit.springboot.service.assemblerelation.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;import com.revisit.springboot.entity.assemblerelation.AssembleRelation;
import com.revisit.springboot.repository.assemblerelation.AssembleRelationRepository;
import com.revisit.springboot.service.assemblerelation.AssembleRelationService;
import com.revisit.springboot.utils.ExcelUtil;
import com.revisit.springboot.utils.JavaBeanUtil;
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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 拼团关系逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-05-06 10:22:48
 */
@Service
public class AssembleRelationServiceImpl implements AssembleRelationService {

    @Autowired
    private AssembleRelationRepository assembleRelationRepository;

    private final static Logger logger = LoggerFactory.getLogger(AssembleRelationServiceImpl.class);

    @Override
    public JSONObject addAssembleRelation(AssembleRelation assembleRelation) {
        logger.info("新增AssembleRelation");


		//保存此对象
		assembleRelation = assembleRelationRepository.save(assembleRelation);

		if (StringUtils.isBlank(assembleRelation.getId())){
            return Result.fail(110,"系统错误","新增AssembleRelation失败,请联系管理员");
        }

		JSONObject assembleRelationBean = (JSONObject)JSON.toJSON(assembleRelation);
        return Result.success(200,"新增AssembleRelation成功",assembleRelationBean);
    }

    @Override
    public JSONObject deleteAssembleRelationById(String id){

		logger.info("删除AssembleRelation: " + id);

		List<String> ids = new ArrayList<>();
		if (StringUtils.contains(id,(","))){
			String[] split = StringUtils.split(id,",");
    		for (String s :split) {
				AssembleRelation assembleRelation = assembleRelationRepository.findById(s).orElse(null);
				if(assembleRelation!=null){
					ids.add(s);
    			}
			}
    	}else{
			AssembleRelation assembleRelation = assembleRelationRepository.findById(id).orElse(null);
			if (assembleRelation == null) {
				return Result.fail(102,"查询失败","AssembleRelation对象不存在");
			}
    		ids.add(id);
		}

		if (ids==null || ids.isEmpty()){
			return Result.fail(102,"查询失败","AssembleRelation对象不存在");
		}

		int assembleRelationRows = assembleRelationRepository.deleteByIds(ids);

    	return Result.success(200,"删除AssembleRelation成功","批量删除AssembleRelation成功,共删除AssembleRelation: " + assembleRelationRows + " 个");
	}

    @Override
    public JSONObject updateAssembleRelationById(String id,AssembleRelation newAssembleRelation){
		logger.info("更新AssembleRelation: " + id);

		AssembleRelation oldAssembleRelation = assembleRelationRepository.findById(id).orElse(null);
		if (oldAssembleRelation==null){
			return Result.fail(102,"查询失败","AssembleRelation对象不存在");
		}

		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "";

		//开始合并对象
		JavaBeanUtil.copyProperties(oldAssembleRelation,newAssembleRelation,ignoreProperties);

    	newAssembleRelation = assembleRelationRepository.save(newAssembleRelation);

    	JSONObject assembleRelationBean = (JSONObject)JSON.toJSON(newAssembleRelation);

    	return Result.success(200,"更新成功",assembleRelationBean);
    }

	@Override
    public JSONObject findAssembleRelationById(String id){
    	logger.info("获取AssembleRelation: " + id);

		AssembleRelation assembleRelation = assembleRelationRepository.findById(id).orElse(null);

		if(assembleRelation == null){
    		return Result.fail(102,"查询失败","AssembleRelation对象不存在");
    	}

    	JSONObject assembleRelationBean = (JSONObject)JSON.toJSON(assembleRelation);

		return Result.success(200,"查询成功",assembleRelationBean);
    }

	@Override
    public JSONObject findAssembleRelationByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取AssembleRelation列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}
		Page assembleRelationListPage = findAssembleRelationList(keyword,orderBy,beginTime,endTime,page,rows);

		if(assembleRelationListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}

    	JSONObject result = new JSONObject();
		result.put("rowsTotal",assembleRelationListPage.getTotalElements());
		result.put("page",assembleRelationListPage.getNumber()+1);
		result.put("rows",assembleRelationListPage.getSize());
		result.put("assembleRelationList",assembleRelationListPage.getContent());
		return Result.success(200,"查询成功",result);
	}

	private Page findAssembleRelationList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page assembleRelationListPage = assembleRelationRepository.findAll(new Specification<AssembleRelation>() {
			@Override
			public Predicate toPredicate(Root<AssembleRelation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
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

        if (!assembleRelationListPage.hasContent()){
        	return null;
        }

		return assembleRelationListPage;
	}

	@Override
	public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page assembleRelationListPage = findAssembleRelationList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<AssembleRelation> assembleRelationList = new ArrayList<>();

		if (assembleRelationListPage!=null){
			assembleRelationList.addAll(assembleRelationListPage.getContent());
		}

		if (assembleRelationList!=null&&!assembleRelationList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(assembleRelationList, "AssembleRelationList列表", "AssembleRelationList列表",AssembleRelation.class, "AssembleRelationList列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}

}