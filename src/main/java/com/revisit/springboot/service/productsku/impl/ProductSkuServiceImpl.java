package com.revisit.springboot.service.productsku.impl;
import com.revisit.springboot.entity.product.Product;
import com.revisit.springboot.entity.productsku.ProductSku;
import com.revisit.springboot.repository.product.ProductRepository;
import com.revisit.springboot.repository.productsku.ProductSkuRepository;
import com.revisit.springboot.service.productsku.ProductSkuService;
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

import java.io.File;
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
 * ProductSku逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-03-03 15:28:24
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class ProductSkuServiceImpl implements ProductSkuService {

    @Autowired
    private ProductSkuRepository productSkuRepository;

    @Autowired
	private ProductRepository productRepository;

    private final static Logger logger = LoggerFactory.getLogger(ProductSkuServiceImpl.class);

    @Override
    public JSONObject addProductSku(String userId,ProductSku productSku) {
        logger.info("新增商品sku");

		String productId = productSku.getProductId();
		Product product = productRepository.findShopIdByProductIdAndUserId(productId,userId);
		if (product==null){
			return Result.fail(102,"更新失败","商品不存在");
		}

        ProductSku oldProductSku = productSkuRepository.findBySkuName(productSku.getSkuName());
		if (oldProductSku!=null){
            return Result.fail(102,"更新失败","商品sku名称已被使用");
        }

        if (productSku.getSkuSalePrice()==null
				||productSku.getSkuOrdinaryMemberPrice()==null
				||productSku.getSkuVipMemberPrice()==null
				||productSku.getSkuPartnerPrice()==null
				||productSku.getSkuCommunityPartnerPrice()==null){
			return Result.fail(102,"参数错误","sku价格不能为空");
		}

		Integer maxSkuSortNumber = productSkuRepository.findMaxSortNumberByProductId(product.getId());
		if (maxSkuSortNumber==null) {
			productSku.setSortNumber(0);
		}else{
			productSku.setSortNumber(maxSkuSortNumber + 1);
		}

		//保存此对象
		productSku = productSkuRepository.save(productSku);

        if (StringUtils.isBlank(productSku.getId())){
            return Result.fail(110,"系统错误","新增商品sku失败,请联系管理员");
        }
		JSONObject productSkuBean = (JSONObject)JSON.toJSON(productSku);
        return Result.success(200,"新增商品sku成功",productSkuBean);
    }

    @Override
    public JSONObject deleteProductSkuById(String userId,String id){

		logger.info("删除商品sku: " + id);

		List<String> ids = new ArrayList<>();
		if (StringUtils.contains(id,(","))){
			String[] split = StringUtils.split(id,",");
    		for (String s :split) {
				ProductSku productSku = productSkuRepository.findById(s).orElse(null);
				if (productSku!=null) {
					Product product = productRepository.findShopIdByProductIdAndUserId(productSku.getProductId(),userId);
					if (product==null){
						return Result.fail(102,"删除失败","该商品不属于你");
					}
					productSkuRepository.allSortNumberMinusOneBySortNumberAndRelationId(productSku.getSortNumber(),productSku.getProductId());
					List<String> oldProductSkuFileList = MoonUtil.getStringListByComma(productSku.getSkuAlbum());
					String skuIcon = productSku.getSkuIcon();
					if (StringUtils.isNotBlank(skuIcon)){
						oldProductSkuFileList.add(skuIcon);
					}
					String skuVideo = productSku.getSkuVideo();
					if (StringUtils.isNotBlank(skuVideo)){
						oldProductSkuFileList.add(skuVideo);
					}
					FileUtil.deleteFileList(oldProductSkuFileList);
					ids.add(s);
				}
			}
    	}else{
			ProductSku productSku = productSkuRepository.findById(id).orElse(null);
			if (productSku==null) {
				return Result.fail(102,"查询失败","商品sku对象不存在");
			}
			Product product = productRepository.findShopIdByProductIdAndUserId(productSku.getProductId(),userId);
			if (product==null){
				return Result.fail(102,"删除失败","该商品不属于你");
			}
			productSkuRepository.allSortNumberMinusOneBySortNumberAndRelationId(productSku.getSortNumber(),productSku.getProductId());
			List<String> oldProductSkuImageList = MoonUtil.getStringListByComma(productSku.getSkuAlbum());
			FileUtil.deleteFileList(oldProductSkuImageList);
			ids.add(id);
		}

		if (ids == null || ids.isEmpty()) {
			return Result.fail(102,"查询失败","商品sku对象不存在");
		}

		int productSkuRows = productSkuRepository.deleteByIds(ids);

		return Result.success(200,"删除商品sku成功","批量删除商品sku成功,共删除商品sku: " + productSkuRows + " 个");
	}

    @Override
    public JSONObject updateProductSkuById(String userId,String id,ProductSku newProductSku){
		logger.info("更新商品sku: " + id);

		ProductSku oldProductSku = productSkuRepository.findById(id).orElse(null);
		if (oldProductSku==null){
			return Result.fail(102,"查询失败","商品sku对象不存在");
		}

		Product product = productRepository.findShopIdByProductIdAndUserId(oldProductSku.getProductId(),userId);
		if (product==null){
			return Result.fail(102,"删除失败","该商品不属于你");
		}

		String newSkuAlbum = newProductSku.getSkuAlbum();
		String oldSkuAlbum = oldProductSku.getSkuAlbum();
		//删除旧相册
		if (StringUtils.isNotBlank(newSkuAlbum)) {
			if (StringUtils.isNotBlank(oldSkuAlbum)) {
				if (!newSkuAlbum.equals(oldSkuAlbum)) {
					FileUtil.deleteOldFileListByNewFileList(oldSkuAlbum,newSkuAlbum);

				}
			}
		}

		//删除旧的图标
		String oldSkuIcon = oldProductSku.getSkuIcon();
		String newSkuIcon = newProductSku.getSkuIcon();
		if (StringUtils.isNotBlank(newSkuIcon)) {
			if (StringUtils.isNotBlank(oldSkuIcon)) {
				if (!oldSkuIcon.equals(newSkuIcon)) {
					FileUtil.deleteOldFileListByNewFileList(oldSkuIcon,newSkuIcon);
				}
			}
		}

		//删除旧的视频
		String oldSkuVideo = oldProductSku.getSkuVideo();
		String newSkuVideo = newProductSku.getSkuVideo();
		if (StringUtils.isNotBlank(newSkuVideo)) {
			if (StringUtils.isNotBlank(oldSkuVideo)) {
				if (!oldSkuVideo.equals(newSkuVideo)) {
					FileUtil.deleteOldFileListByNewFileList(oldSkuVideo,newSkuVideo);
				}
			}
		}

		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "sortNumber";

		//开始合并对象
		JavaBeanUtil.copyProperties(oldProductSku,newProductSku,true,ignoreProperties);

		String skuDetail = newProductSku.getSkuDetail();
		if (StringUtils.isNotBlank(skuDetail)&&(skuDetail.contains("<img src="))){
			newProductSku.setSkuDetail(ImageUtil.fixBase64Edit(skuDetail));
		}

    	newProductSku = productSkuRepository.save(newProductSku);

    	JSONObject productSkuBean = (JSONObject)JSON.toJSON(newProductSku);

    	return Result.success(200,"更新成功",productSkuBean);
    }

	@Override
    public JSONObject findProductSkuById(String id){
    	logger.info("获取商品sku: " + id);

		ProductSku productSku = productSkuRepository.findById(id).orElse(null);

		if(productSku == null){
    		return Result.fail(102,"查询失败","商品sku对象不存在");
    	}
    	productSku.setSkuClickRate(productSku.getSkuClickRate()+1);
		productSku = productSkuRepository.save(productSku);
    	JSONObject productSkuBean = (JSONObject)JSON.toJSON(productSku);

		return Result.success(200,"查询成功",productSkuBean);
    }

	@Override
    public JSONObject findProductSkuByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取商品sku列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}
		Page productSkuListPage = findProductSkuList(keyword,orderBy,beginTime,endTime,page,rows);

		if(productSkuListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}

    	JSONObject result = new JSONObject();
		result.put("rowsTotal",productSkuListPage.getTotalElements());
		result.put("page",productSkuListPage.getNumber()+1);
		result.put("rows",productSkuListPage.getSize());
		result.put("productSkuList",productSkuListPage.getContent());
		return Result.success(200,"查询成功",result);
	}

	private Page findProductSkuList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page productSkuListPage = productSkuRepository.findAll(new Specification<ProductSku>() {
			@Override
			public Predicate toPredicate(Root<ProductSku> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
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

        if (!productSkuListPage.hasContent()){
        	return null;
        }

		return productSkuListPage;
	}

	@Override
	public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page productSkuListPage = findProductSkuList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<ProductSku> productSkuList = new ArrayList<>();

		if (productSkuListPage!=null){
			productSkuList.addAll(productSkuListPage.getContent());
		}

		if (productSkuList!=null&&!productSkuList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(productSkuList, "商品sku列表", "商品sku列表",ProductSku.class, "商品sku列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}


	@Override
	public JSONObject productSkuSortUpOrDown(String id, String upOrDown) {
		ProductSku productSku = productSkuRepository.findById(id).orElse(null);

		if (productSku==null){
			return Result.fail(102,"查询失败","该商品对象不存在");
		}
		Integer sortNumber = productSku.getSortNumber();
		if (sortNumber==0&&upOrDown.equals("上移")){
			return Result.fail(102,"排序失败","该商品当前已在同级别分类的最顶端");
		}

		Integer maxSortNumber = productSkuRepository.findMaxSortNumberByProductId(productSku.getProductId());

		if (sortNumber==maxSortNumber&&upOrDown.equals("下移")){
			return Result.fail(102,"排序失败","该商品当前已在同级别分类的最末端");
		}
		if (upOrDown.equals("上移")){
			sortNumber = sortNumber-1;
		}

		if (upOrDown.equals("下移")){
			sortNumber = sortNumber+1;
		}
		ProductSku brothersProductSku = productSkuRepository.findByProductIdAndSortNumber(productSku.getProductId(), sortNumber);
		if (brothersProductSku!=null){
			brothersProductSku.setSortNumber(productSku.getSortNumber());
			productSku.setSortNumber(sortNumber);
			productSkuRepository.save(brothersProductSku);
			productSku = productSkuRepository.save(productSku);
		}
		return Result.success(200,"排序成功",productSku);
	}

	/**
	 * 〈批量审核商品sku〉
	 *
	 * @param id,confirm
	 * @return:
	 * @since: 1.0.0
	 * @Author: Revisit-Moon
	 * @Date: 2019-06-17 16:45
	 */
    @Override
    public JSONObject confirmProductSku(String id, Boolean confirm) {
		logger.info("审核商品sku: "+ id);

		int productSkuRows = productSkuRepository.confirmProductSkuById(id, confirm);

		return Result.success(200,"审核成功","审核商品sku: "+productSkuRows+" 个");
    }

    /**
	 * 〈扣除商品库存〉
	 *
	 * @param skuIdAndNumber
	 * @return:
	 * @since: 1.0.0
	 * @Author: Revisit-Moon
	 * @Date: 2019/4/14 4:16 PM
	 */
	@Override
	public boolean deductStock(Map<String, Object> skuIdAndNumber) {
		if (skuIdAndNumber==null||skuIdAndNumber.isEmpty()) {
			return false;
		}
		Set<String> productIdSet = new HashSet<>();
		List<ProductSku> productSkuList = new ArrayList<>();
		for (Map.Entry entry :skuIdAndNumber.entrySet()) {
			String skuId = entry.getKey().toString();
			ProductSku sku = productSkuRepository.findById(skuId).orElse(null);
			if (sku==null){
				logger.info("扣除库存失败,sku不存在: "+skuId);
				continue;
			}
			int buyNumber = Integer.parseInt(entry.getValue().toString());

			//扣除库存
			sku.setSkuStock(sku.getSkuStock()-buyNumber);

			//增加销量
			sku.setSkuSales(sku.getSkuSales()+buyNumber);
			productSkuList.add(sku);
			productIdSet.add(sku.getProductId());
		}

		if (productSkuList.isEmpty()){
			return false;
		}
		productSkuList = productSkuRepository.saveAll(productSkuList);

		if (productSkuList!=null&&!productSkuList.isEmpty()){
			return true;
		}else {
			return false;
		}
	}


	//计算价格
	@Override
	public BigDecimal whatPriceByRoleNameAndSku(int level,ProductSku sku){
		switch (level){
			case 0:{
				return sku.getSkuSalePrice();
			}
			case 1:{
				return sku.getSkuOrdinaryMemberPrice();
			}
			case 2:{
				return sku.getSkuVipMemberPrice();
			}
			case 3:{
				return sku.getSkuPartnerPrice();
			}
			case 4:{
				return sku.getSkuCommunityPartnerPrice();
			}
			default:{
				return sku.getSkuSalePrice();
			}
		}
	}
}