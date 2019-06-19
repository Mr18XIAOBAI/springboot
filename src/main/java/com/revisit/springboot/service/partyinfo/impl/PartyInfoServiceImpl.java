package com.revisit.springboot.service.partyinfo.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.partyinfo.PartyInfo;
import com.revisit.springboot.repository.partyinfo.PartyInfoRepository;
import com.revisit.springboot.service.partyinfo.PartyInfoService;
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
 * PartyInfo逻辑层接口类
 * @author Revisit-Zhang
 * @date 2019-06-19 11:54:16
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class PartyInfoServiceImpl implements PartyInfoService {

    @Autowired
    private PartyInfoRepository partyInfoRepository;

    private final static Logger logger = LoggerFactory.getLogger(PartyInfoServiceImpl.class);

    @Override
    public JSONObject addPartyInfo(PartyInfo partyInfo) {
        logger.info("新增PartyInfo");


		//保存此对象
		partyInfo = partyInfoRepository.save(partyInfo);

		// JSONObject partyInfoBean = (JSONObject)JSON.toJSON(partyInfo);

        if (StringUtils.isBlank(partyInfo.getId())){
            return Result.fail(110,"系统错误","新增PartyInfo失败,请联系管理员");
        }

        return Result.success(200,"新增PartyInfo成功",partyInfo);
    }

    @Override
    public JSONObject deletePartyInfoById(String id){

		logger.info("删除PartyInfo: " + id);

		List<String> ids = new ArrayList<>();
		if (StringUtils.contains(id,(","))){
			String[] split = StringUtils.split(id,",");
    		for (String s :split) {
				PartyInfo partyInfo = partyInfoRepository.findById(s).orElse(null);
				if(partyInfo!=null){
					ids.add(s);
    			}
			}
    	}else{
			PartyInfo partyInfo = partyInfoRepository.findById(id).orElse(null);
			if (partyInfo == null) {
				return Result.fail(102,"查询失败","PartyInfo对象不存在");
			}
    		ids.add(id);
		}

		if (ids==null || ids.isEmpty()){
			return Result.fail(102,"查询失败","PartyInfo对象不存在");
		}

		int partyInfoRows = partyInfoRepository.deleteByIds(ids);

    	return Result.success(200,"删除PartyInfo成功","批量删除PartyInfo成功,共删除PartyInfo: " + partyInfoRows + " 个");
	}

    @Override
    public JSONObject updatePartyInfoById(String id,PartyInfo newPartyInfo){
		logger.info("更新PartyInfo: " + id);

		PartyInfo oldPartyInfo = partyInfoRepository.findById(id).orElse(null);
		if (oldPartyInfo==null){
			return Result.fail(102,"查询失败","partyInfo对象不存在");
		}

		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "";

		//开始合并对象
		JavaBeanUtil.copyProperties(oldPartyInfo,newPartyInfo,ignoreProperties);

    	newPartyInfo = partyInfoRepository.save(newPartyInfo);

    	JSONObject partyInfoBean = (JSONObject)JSON.toJSON(newPartyInfo);

    	return Result.success(200,"更新成功",partyInfoBean);
    }

	@Override
    public JSONObject findPartyInfoById(String id){
    	logger.info("获取PartyInfo: " + id);

		PartyInfo partyInfo = partyInfoRepository.findById(id).orElse(null);

		if(partyInfo == null){
    		return Result.fail(102,"查询失败","partyInfo对象不存在");
    	}

    	JSONObject partyInfoBean = (JSONObject)JSON.toJSON(partyInfo);

		return Result.success(200,"查询成功",partyInfoBean);
    }

	@Override
    public JSONObject findPartyInfoByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取PartyInfo列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}
		Page partyInfoListPage = findPartyInfoList(keyword,orderBy,beginTime,endTime,page,rows);

		if(partyInfoListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}

    	JSONObject result = new JSONObject();
		result.put("rowsTotal",partyInfoListPage.getTotalElements());
		result.put("page",partyInfoListPage.getNumber()+1);
		result.put("rows",partyInfoListPage.getSize());
		result.put("partyInfoList",partyInfoListPage.getContent());
		return Result.success(200,"查询成功",result);
	}

	private Page findPartyInfoList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page partyInfoListPage = partyInfoRepository.findAll(new Specification<PartyInfo>() {
			@Override
			public Predicate toPredicate(Root<PartyInfo> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
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

        if (!partyInfoListPage.hasContent()){
        	return null;
        }

		return partyInfoListPage;
	}

	@Override
	public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page partyInfoListPage = findPartyInfoList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<PartyInfo> partyInfoList = new ArrayList<>();

		if (partyInfoListPage!=null){
			partyInfoList.addAll(partyInfoListPage.getContent());
		}

		if (partyInfoList!=null&&!partyInfoList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(partyInfoList, "partyInfoList列表", "partyInfoList列表",PartyInfo.class, "partyInfoList列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}

}