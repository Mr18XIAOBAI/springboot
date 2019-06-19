package com.revisit.springboot.service.addressmanage.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.addressmanage.AddressManage;
import com.revisit.springboot.repository.addressmanage.AddressManageRepository;
import com.revisit.springboot.service.addressmanage.AddressManageService;
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
 * AddressManage逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-04-15 21:11:34
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class AddressManageServiceImpl implements AddressManageService {

    @Autowired
    private AddressManageRepository addressManageRepository;

    private final static Logger logger = LoggerFactory.getLogger(AddressManageServiceImpl.class);

    @Override
    public JSONObject addAddressManage(AddressManage addressManage) {
        logger.info("新增地址");
		List<AddressManage> addressManageList = addressManageRepository.findByUserId(addressManage.getUserId());
		if (addressManageList!=null&&!addressManageList.isEmpty()&&addressManageList.size()>1) {
			if (addressManage.isDefault()) {
				addressManageRepository.fixAddressManageDefaultIsFalseByUserId(addressManage.getUserId());
			}else{
				int defaultIsALLFalse = addressManageRepository.findDefaultIsALLFalse(addressManage.getUserId());
				if (defaultIsALLFalse == 0) {
					addressManage.setDefault(true);
				}
			}
		}else{
			addressManage.setDefault(true);
		}
		//保存此对象
		addressManage = addressManageRepository.save(addressManage);

		if (StringUtils.isBlank(addressManage.getId())){
            return Result.fail(110,"系统错误","新增地址失败,请联系管理员");
        }

		JSONObject addressManageBean = (JSONObject)JSON.toJSON(addressManage);
        return Result.success(200,"新增地址成功",addressManageBean);
    }

    @Override
    public JSONObject deleteAddressManageById(String id,String userId){

		logger.info("删除地址: " + id);

		//需要修复默认地址
		boolean needFixDefault = false;

		List<String> ids = new ArrayList<>();
		if (StringUtils.contains(id,(","))){
			String[] split = StringUtils.split(id,",");
    		for (String s :split) {
				AddressManage addressManage = addressManageRepository.findByIdAndUserId(id,userId);
				if(addressManage!=null){
					if (addressManage.isDefault()){
						needFixDefault = true;
					}
					ids.add(s);
    			}
			}
    	}else{
			AddressManage addressManage = addressManageRepository.findByIdAndUserId(id,userId);
			if (addressManage == null) {
				return Result.fail(102,"查询失败","AddressManage对象不存在");
			}
			if (addressManage.isDefault()){
				needFixDefault = true;
			}
    		ids.add(id);
		}

		if (ids==null || ids.isEmpty()){
			return Result.fail(102,"查询失败","AddressManage对象不存在");
		}

		int addressManageRows = addressManageRepository.deleteByIds(ids);


		if (addressManageRows>0&&needFixDefault){
			String maxCreateTimeId = addressManageRepository.findAddressManagerByUserIdAndMaxCreateTime(userId);
			addressManageRepository.fixAddressManageDefaultIsTrueById(maxCreateTimeId);
		}
    	return Result.success(200,"删除地址成功","批量删除地址成功,共删除地址: " + addressManageRows + " 个");
	}

    @Override
    public JSONObject updateAddressManageById(String id,AddressManage newAddressManage){
		logger.info("更新地址: " + id);

		AddressManage oldAddressManage = addressManageRepository.findByIdAndUserId(id,newAddressManage.getUserId());
		if (oldAddressManage==null){
			return Result.fail(102,"查询失败","AddressManage对象不存在");
		}

		boolean defaultIsAllFalse = true;

		if(newAddressManage.isDefault()){
			addressManageRepository.fixAddressManageDefaultIsFalseByUserId(oldAddressManage.getUserId());
		}else{
			List<AddressManage> addressManageList = addressManageRepository.findByUserId(oldAddressManage.getUserId());
			//如果获取到的地址记录只剩余本地址则设置为默认地址
			if (addressManageList.size()==1){
				newAddressManage.setDefault(true);
			}
			//如果获取到的地址记录不是只剩余本地址
			if (addressManageList.size()!=1&&addressManageList.size()>0){
				//判断除了本地址外,其余地址是否都不为默认地址
				for (AddressManage addressManage :addressManageList) {
					if (!addressManage.getId().equals(id)){
						if (addressManage.isDefault()){
							defaultIsAllFalse = false;
						}
					}
				}
			}

		}

		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "";

		//开始合并对象
		JavaBeanUtil.copyProperties(oldAddressManage,newAddressManage,ignoreProperties);

    	newAddressManage = addressManageRepository.save(newAddressManage);

		//如果其余地址都不为默认地址,则将最后添加的地址设为默认地址
		if (defaultIsAllFalse&&!newAddressManage.isDefault()){
			String maxCreateTimeId = addressManageRepository.findAddressManagerByUserIdAndMaxCreateTime(newAddressManage.getUserId());
			addressManageRepository.fixAddressManageDefaultIsTrueById(maxCreateTimeId);
		}

		JSONObject addressManageBean = (JSONObject)JSON.toJSON(newAddressManage);

    	return Result.success(200,"更新成功",addressManageBean);
    }

	@Override
    public JSONObject findAddressManageByUserId(String userId){
    	logger.info("获取地址列表: " + userId);

		List<AddressManage> addressManageList = addressManageRepository.findByUserId(userId);

		if(addressManageList == null||addressManageList.isEmpty()){
    		return Result.fail(102,"查询失败","当前还没有地址,请先添加地址");
    	}

		return Result.success(200,"查询成功",addressManageList);
    }

	@Override
	public JSONObject findAddressManageById(String id,String userId){
		logger.info("获取地址: " + id);

		AddressManage addressManage = addressManageRepository.findById(id).orElse(null);

		if(addressManage == null){
			return Result.fail(102,"查询失败","地址对象不存在");
		}

		if (!addressManage.getUserId().equals(userId)){
			return Result.fail(102,"查询失败","非法操作");
		}

		return Result.success(200,"查询成功",addressManage);
	}

	@Override
    public JSONObject findAddressManageByList(String keyword,String userId,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取地址列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}
		Page addressManageListPage = findAddressManageList(keyword,userId,orderBy,beginTime,endTime,page,rows);

		if(addressManageListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}

    	JSONObject result = new JSONObject();
		result.put("rowsTotal",addressManageListPage.getTotalElements());
		result.put("page",addressManageListPage.getNumber()+1);
		result.put("rows",addressManageListPage.getSize());
		result.put("addressManageList",addressManageListPage.getContent());
		return Result.success(200,"查询成功",result);
	}

	private Page findAddressManageList(String keyword,String userId,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page addressManageListPage = addressManageRepository.findAll(new Specification<AddressManage>() {
			@Override
			public Predicate toPredicate(Root<AddressManage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicateList = new ArrayList<>();
        		//指定查询对象
        		if (StringUtils.isNotBlank(keyword)) {
        			predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("address"), "%" + keyword + "%")
        			, criteriaBuilder.like(root.get("consignee"), "%" + keyword + "%")
        			, criteriaBuilder.like(root.get("contactMobile"), "%" + keyword + "%")));
        		}

        		if (StringUtils.isNotBlank(userId)){
					predicateList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("userId"),userId)));
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

        if (!addressManageListPage.hasContent()){
        	return null;
        }

		return addressManageListPage;
	}

	@Override
	public void exportExcel(String keyword,String userId,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page addressManageListPage = findAddressManageList(keyword, userId,orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<AddressManage> addressManageList = new ArrayList<>();

		if (addressManageListPage!=null){
			addressManageList.addAll(addressManageListPage.getContent());
		}

		if (addressManageList!=null&&!addressManageList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(addressManageList, "地址列表", "地址列表",AddressManage.class, "地址列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}

}