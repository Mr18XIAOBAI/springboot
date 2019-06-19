package com.revisit.springboot.service.productclass.impl;
import com.revisit.springboot.entity.product.Product;
import com.revisit.springboot.entity.productclass.ProductClass;
import com.revisit.springboot.repository.product.ProductRepository;
import com.revisit.springboot.repository.productclass.ProductClassRepository;
import com.revisit.springboot.service.productclass.ProductClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.revisit.springboot.utils.Result;
import com.revisit.springboot.utils.*;
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
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletResponse;


/**
 * ProductClass逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-03-03 19:32:16
 */
@Service
public class ProductClassServiceImpl implements ProductClassService {

    @Autowired
    private ProductClassRepository productClassRepository;

    @Autowired
	private ProductRepository productRepository;

    private final static Logger logger = LoggerFactory.getLogger(ProductClassServiceImpl.class);

    @Override
    public JSONObject addProductClass(ProductClass productClass) {
        logger.info("新增分类");

		String fatherClassId = productClass.getFatherClassId();
		if (StringUtils.isNotBlank(fatherClassId)
				&&(!fatherClassId.equals("top")
				&&(fatherClassId.length()!=22))){
			return Result.fail(102,"参数错误","父分类不存在");
		}

		if (StringUtils.isBlank(fatherClassId)){
			fatherClassId = "top";
			productClass.setClassLevel(1);
		}

		if (!fatherClassId.equals("top")) {
			ProductClass fatherClass = productClassRepository.findById(fatherClassId).orElse(null);
			if (fatherClass==null){
				return Result.fail(102,"参数错误","父分类不存在");
			}
			List<Product> productList = productRepository.findByProductClassId(fatherClassId);
			if (productList!=null&&!productList.isEmpty()){
				return Result.fail(102,"参数错误","父分类下存在商品,不可新增子分类");
			}

			Integer classLevel = fatherClass.getClassLevel();
			if (!fatherClass.getHaveSonClass()){
				fatherClass.setHaveSonClass(true);
			}
			fatherClass.setSonClassCount(fatherClass.getSonClassCount()+1);
			productClass.setClassLevel(classLevel+1);
		}
		if (productClassRepository.findByClassName(productClass.getClassName())!=null){
			return Result.fail(102,"参数错误","分类已存在");
		}


		Integer sortNumber = productClassRepository.findByClassMaxSortNumber(fatherClassId);
		if (sortNumber==null){
			productClass.setSortNumber(0);
		}else{
			productClass.setSortNumber(sortNumber+1);
		}

		//保存此对象
		productClass = productClassRepository.save(productClass);

        if (StringUtils.isBlank(productClass.getId())){
            return Result.fail(110," 系统错误","新增分类失败,请联系管理员");
        }
		JSONObject productClassBean = (JSONObject)JSON.toJSON(productClass);
        return Result.success(200,"新增分类成功",productClassBean);
    }

    @Override
    public JSONObject deleteProductClassById(String id){

		logger.info("删除分类: " + id);
		List<ProductClass> productClassList = new ArrayList<>();
		if (StringUtils.contains(id,(","))){
			String[] split = StringUtils.split(id,",");
    		for (String s :split) {
				ProductClass productClass = productClassRepository.findById(s).orElse(null);
				if (productClass!=null) {
					productClassList.add(productClass);
				}
			}
    	}else {
			ProductClass productClass = productClassRepository.findById(id).orElse(null);
			if (productClass != null) {
				productClassList.add(productClass);
			}
		}

		if (productClassList!=null&&!productClassList.isEmpty()) {
			for (ProductClass productClass :productClassList) {
				if (productClass.getHaveSonClass()){
					return Result.fail(102,"参数错误","该分类下存在子分类,请先删除子分类");
				}
				String fatherClassId = productClass.getFatherClassId();
				deleteProductClassTree(productClass.getId());
				ProductClass fatherClass = null;
				productClassRepository.allSortNumberMinusOneByFatherId(productClass.getSortNumber(),productClass.getFatherClassId());
				if (StringUtils.isNotBlank(fatherClassId) && !fatherClassId.equals("top")) {
					fatherClass = productClassRepository.findById(fatherClassId).orElse(null);
				}
				if (fatherClass != null) {
					Integer sonClassCount = fatherClass.getSonClassCount();
					if (sonClassCount > 0) {
						fatherClass.setSonClassCount(fatherClass.getSonClassCount() - 1);
						if (fatherClass.getSonClassCount() == 0) {
							fatherClass.setHaveSonClass(false);
						}
					}
					if (sonClassCount == null || sonClassCount == 0) {
						fatherClass.setHaveSonClass(false);
					}
					productClassRepository.save(fatherClass);
				}
			}
		}else {
			return Result.fail(102,"查询失败","该分类对象不存在");
		}

		return Result.success(200,"删除成功","删除分类成功");
	}

	private void deleteProductClassTree(String id){
		List<ProductClass> productClassList = productClassRepository.findByFatherClassId(id);
		if (productClassList!=null&&!productClassList.isEmpty()){
			for (ProductClass productClass :productClassList) {
				if (productClass.getHaveSonClass()){
					deleteProductClassTree(productClass.getId());
				}
			}
		}
		productClassRepository.deleteById(id);
		productRepository.deleteByProductClassId(id);
	}

    @Override
    public JSONObject updateProductClassById(String id,ProductClass newProductClass){
		logger.info("更新分类: " + id);

		ProductClass oldProductClass = productClassRepository.findById(id).orElse(null);
		if (oldProductClass==null){
			return Result.fail(102,"查询失败","该分类对象不存在");
		}

		String className = newProductClass.getClassName();
		if (StringUtils.isNotBlank(className)&&className.equals(oldProductClass.getClassName())){
			if (productClassRepository.findByClassName(newProductClass.getClassName())!=null){
				return Result.fail(102,"参数错误","分类已存在");
			}
		}

		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "sortNumber,classLevel,haveSonClass,sonClassCount,fatherClassId";

		//开始合并对象
		JavaBeanUtil.copyProperties(oldProductClass,newProductClass,ignoreProperties);

    	newProductClass = productClassRepository.save(newProductClass);

    	JSONObject productClassBean = (JSONObject)JSON.toJSON(newProductClass);

    	return Result.success(200,"更新成功",productClassBean);
    }

	@Override
    public JSONObject findProductClassById(String id){
    	logger.info("获取分类: " + id);

		ProductClass productClass = productClassRepository.findById(id).orElse(null);

		if(productClass == null){
    		return Result.fail(102,"查询失败","该分类对象不存在");
    	}

    	JSONObject productClassBean = (JSONObject)JSON.toJSON(productClass);

		return Result.success(200,"查询成功",productClassBean);
    }

	@Override
    public JSONObject findProductClassByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取分类列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}

		if (StringUtils.isBlank(orderBy)){
			orderBy = "sortNumber_ASC";
		}

		Page productClassListPage = findProductClassList(keyword,orderBy,beginTime,endTime,page,rows);

		if(productClassListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}

		JSONObject result = new JSONObject();
		result.put("rowsTotal",productClassListPage.getTotalElements());
		result.put("page",productClassListPage.getNumber()+1);
		result.put("rows",productClassListPage.getSize());
		result.put("productClassList",productClassListPage.getContent());
		return Result.success(200,"查询成功",result);
	}

	private Page findProductClassList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page productClassListPage = productClassRepository.findAll(new Specification<ProductClass>() {
			@Override
			public Predicate toPredicate(Root<ProductClass> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicateList = new ArrayList<>();
        		//指定查询对象
        		if (StringUtils.isNotBlank(keyword)) {
        			predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("className"), "%" + keyword + "%")
        			, criteriaBuilder.like(root.get("sortNumber"), "%" + keyword + "%")));
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

        if (!productClassListPage.hasContent()){
        	return null;
        }

		return productClassListPage;
	}

	@Override
	public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page productClassListPage = findProductClassList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<ProductClass> productClassList = new ArrayList<>();

		if (productClassListPage!=null){
			productClassList.addAll(productClassListPage.getContent());
		}

		if (productClassList!=null&&!productClassList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(productClassList, "分类列表", "分类列表",ProductClass.class, "分类列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}

	@Override
	public JSONObject findProductTreeList(String productClassId,String productClassName) {
		JSONArray productClassList = new JSONArray();
		//如果是获取全部分类的树形结构
    	if (StringUtils.isBlank(productClassId)&&StringUtils.isBlank(productClassName)){
			List<ProductClass> topClassList = productClassRepository.findByClassLevel(1);
			if (topClassList!=null&&!topClassList.isEmpty()){
				for (ProductClass  productClass: topClassList) {
					JSONObject productClassBean = (JSONObject) JSON.toJSON(productClass);
					while (productClass.getHaveSonClass()){
						productClassBean.put("sonClassList",findSonClassList(productClass));
						productClassList.add(productClassBean);
						break;
					}
					if (!productClass.getHaveSonClass()) {
						productClassList.add(productClassBean);
					}
				}
			}else{
				return Result.fail(102,"查询失败","获取不到相关数据");
			}
		}
		ProductClass fatherClass;
		if (StringUtils.isNotBlank(productClassId)||StringUtils.isNotBlank(productClassName)){
			fatherClass = productClassRepository.findById(productClassId).orElse(null);
			if (fatherClass == null){
				fatherClass = productClassRepository.findByClassName(productClassName);
			}
			if (fatherClass!=null){
				if (!fatherClass.getHaveSonClass()) {
					productClassList.add(fatherClass);
				}else {
					JSONObject productClassBean = (JSONObject) JSON.toJSON(fatherClass);
					productClassBean.put("sonClassList", findSonClassList(fatherClass));
					productClassList.add(productClassBean);
				}
			}else{
				return Result.fail(102,"查询失败","获取不到相关数据");
			}
		}
		return Result.success(102,"查询成功",productClassList);

	}

	private JSONArray findSonClassList(ProductClass productClass){
		JSONArray sonClassArray = new JSONArray();
		List<ProductClass> sonList = productClassRepository.findByFatherClassId(productClass.getId());
		if (sonList!=null&&!sonList.isEmpty()) {
			for (ProductClass sonClass :sonList) {
				JSONObject productClassBean = (JSONObject)JSON.toJSON(sonClass);
				while (sonClass.getHaveSonClass()){
					productClassBean.put("sonClassList",findSonClassList(sonClass));
					break;
				}
				if (!sonClass.getHaveSonClass()){
					productClassBean.put("sonClassList",new JSONArray());
				}
				sonClassArray.add(productClassBean);
			}
		}
		return sonClassArray;
	}

	@Override
	public JSONObject productClassSortUpOrDown(String id, String upOrDown) {

		ProductClass productClass = productClassRepository.findById(id).orElse(null);

		if (productClass==null){
			return Result.fail(102,"查询失败","该分类对象不存在");
		}
		Integer sortNumber = productClass.getSortNumber();
		if (sortNumber==0&&upOrDown.equals("上移")){
			return Result.fail(102,"排序失败","该分类当前已在同级别分类的最顶端");
		}

		int maxSortNumber = productClassRepository.findByClassMaxSortNumber(productClass.getFatherClassId());

		if (sortNumber==maxSortNumber&&upOrDown.equals("下移")){
			return Result.fail(102,"排序失败","该分类当前已在同级别分类的最末端");
		}
		if (upOrDown.equals("上移")){
			sortNumber = sortNumber-1;
		}

		if (upOrDown.equals("下移")){
			sortNumber = sortNumber+1;
		}
		ProductClass brothersProductClass = productClassRepository.findByFatherClassIdAndSortNumber(productClass.getFatherClassId(), sortNumber);
		if (brothersProductClass!=null){
			brothersProductClass.setSortNumber(productClass.getSortNumber());
			productClass.setSortNumber(sortNumber);
			productClassRepository.save(brothersProductClass);
			productClass = productClassRepository.save(productClass);
		}
		return Result.success(200,"排序成功",productClass);
	}
}