package com.revisit.springboot.service.product.impl;
import com.revisit.springboot.entity.shop.Shop;
import com.revisit.springboot.entity.user.Role;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.repository.shop.ShopRepository;
import com.revisit.springboot.repository.shoppingcart.ShoppingCartItemRepository;
import com.revisit.springboot.repository.user.UserRepository;
import com.revisit.springboot.service.product.ProductService;
import com.revisit.springboot.service.productsku.ProductSkuService;
import com.revisit.springboot.entity.product.Product;
import com.revisit.springboot.entity.productclass.ProductClass;
import com.revisit.springboot.entity.productsku.ProductSku;
import com.revisit.springboot.repository.product.ProductRepository;
import com.revisit.springboot.repository.productclass.ProductClassRepository;
import com.revisit.springboot.repository.productsku.ProductSkuRepository;
import com.revisit.springboot.service.user.UserService;
import com.revisit.springboot.utils.*;
import org.apache.poi.ss.util.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletResponse;


/**
 * Product逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-03-01 10:00:53
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
	private ProductSkuRepository productSkuRepository;

  	@Autowired
	private ProductClassRepository productClassRepository;

  	@Autowired
	private ShoppingCartItemRepository shoppingCartItemRepository;

  	@Autowired
	private UserRepository userRepository;

  	@Autowired
	private ShopRepository shopRepository;


	private final static Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Override
    public JSONObject addProduct(String userId,Product product, List<ProductSku> skuList) {
        logger.info("新增商品");

		User user = userRepository.findById(userId).orElse(null);
		if (user==null){
			return Result.fail(102,"参数错误","用户不存在");
		}
		String productClassId = product.getProductClassId();
		String productName = product.getProductName();
		if (StringUtils.isBlank(productClassId)
				||productClassId.length()!=22
				||StringUtils.isBlank(productName)){
			return Result.fail(102,"参数错误","必填参数不能为空");
		}

		//调用获取店铺ID模块
		String shopId = getShopId(user,product.getShopId());
		if (StringUtils.isBlank(shopId)){
			return Result.fail(102,"参数错误","店铺不存在");
		}

		product.setShopId(shopId);

		if (productRepository.findByProductName(productName)!=null){
			return Result.fail(102,"新增失败","该商品已存在");
		}

		ProductClass productClass = productClassRepository.findById(productClassId).orElse(null);
		if (productClass == null){
			return Result.fail(102,"新增失败","目标分类不存在,请先新增分类");
		}

		Integer maxSortNumber = productRepository.findMaxSortNumberByProductClassId(productClass.getId());

		//设置为最大的排序号
		if (maxSortNumber==null) {
            product.setSortNumber(0);
		}else{
            product.setSortNumber(maxSortNumber + 1);
        }

		//保存此对象
		product = productRepository.save(product);
        if (StringUtils.isBlank(product.getId())) {
			return Result.fail(110, "系统错误", "新增商品失败,请联系管理员");
		}
        List<ProductSku> newSkuList = new ArrayList<>();
		for (ProductSku productSku : skuList) {
        	if (productSku.getSkuSalePrice()==null
					||productSku.getSkuOrdinaryMemberPrice()==null
					||productSku.getSkuVipMemberPrice()==null
					||productSku.getSkuPartnerPrice()==null
					||productSku.getSkuCommunityPartnerPrice()==null){
				return Result.fail(102,"参数错误","sku价格不能为空");
			}
			productSku.setProductId(product.getId());
			Integer maxSkuSortNumber = productSkuRepository.findMaxSortNumberByProductId(product.getId());
			if (maxSkuSortNumber==null) {
				productSku.setSortNumber(0);
			}else{
				productSku.setSortNumber(maxSkuSortNumber + 1);
			}

			String skuDetail = productSku.getSkuDetail();
			if (StringUtils.isNotBlank(skuDetail)&&(skuDetail.contains("<img src="))){
				productSku.setSkuDetail(ImageUtil.fixBase64Edit(skuDetail));
			}
			ProductSku sku = productSkuRepository.save(productSku);
			if (StringUtils.isBlank(sku.getId())){
				return Result.fail(110,"系统错误","新增商品SKU失败,请联系管理员");
			}
			newSkuList.add(sku);
		}

		JSONObject productBean = (JSONObject)JSON.toJSON(product);
		productBean.put("skuList",newSkuList);
        return Result.success(200,"新增商品成功",productBean);
    }

    @Override
    public JSONObject deleteProductById(String userId,String id){

		User user = userRepository.findById(userId).orElse(null);
		if (user==null){
			return Result.fail(102,"参数错误","用户不存在");
		}

		//调用获取店铺ID模块
		String shopId = getShopId(user,id);
		if (StringUtils.isBlank(shopId)){
			return Result.fail(102,"参数错误","店铺不存在");
		}

		logger.info("删除商品: " + id);

		List<String> ids = new ArrayList<>();
		if (StringUtils.contains(id,(","))){

			String[] split = StringUtils.split(id,",");

    		for (String s :split) {
				Product product = productRepository.findById(s).orElse(null);
				if (product != null&&product.getShopId().equals(shopId)) {
					productRepository.allSortNumberMinusOneBySortNumberAndRelationId(product.getSortNumber(), product.getProductClassId());
					deleteFile(product);
					ids.add(s);
				}
			}

		}else{
			Product product = productRepository.findById(id).orElse(null);
			if (product == null) {
				return Result.fail(102,"查询失败","商品对象不存在");
			}
			if (!product.getShopId().equals(shopId)){
				return Result.fail(102,"参数错误","不是商品拥有者");
			}
			productRepository.allSortNumberMinusOneBySortNumberAndRelationId(product.getSortNumber(),product.getProductClassId());
			deleteFile(product);
			ids.add(id);
		}

		if (ids==null||ids.isEmpty()){
			return Result.fail(102,"查询失败","商品对象不存在");
		}

		int shoppingCartRows = shoppingCartItemRepository.deleteByProductIds(ids);
		int productRows = productRepository.deleteByIds(ids);
		int productSkuRows = productSkuRepository.deleteByProductIds(ids);
		return Result.success(200,"删除商品成功","批量删除商品成功,共删除商品: " + productRows + " 个,商品sku: " + productSkuRows + " 个,购物车商品" + shoppingCartRows + "个");
	}

	private void deleteFile(Product product){
		if(product!=null){
			List<ProductSku> productSkuList = productSkuRepository.findByProductId(product.getId());
			if (productSkuList!=null&&!productSkuList.isEmpty()){
				for (ProductSku productSku :productSkuList) {
					productSkuRepository.allSortNumberMinusOneBySortNumberAndRelationId(productSku.getSortNumber(),productSku.getProductId());
					List<String> oldAssembleProductSkuAlbumList = MoonUtil.getStringListByComma(productSku.getSkuAlbum());
					FileUtil.deleteFileList(oldAssembleProductSkuAlbumList);
					List<String> oldAssembleProductSkuVideoList = MoonUtil.getStringListByComma(productSku.getSkuVideo());
					FileUtil.deleteFileList(oldAssembleProductSkuVideoList);
					List<String> oldAssembleProductSkuIconList = MoonUtil.getStringListByComma(productSku.getSkuIcon());
					FileUtil.deleteFileList(oldAssembleProductSkuIconList);
				}

			}
			List<String> oldProductIconList = MoonUtil.getStringListByComma(product.getProductMainIcon());
			List<String> oldProductVideoList = MoonUtil.getStringListByComma(product.getProductVideo());
			FileUtil.deleteFileList(oldProductIconList);
			FileUtil.deleteFileList(oldProductVideoList);
		}
	}

    @Override
    public JSONObject updateProductById(String userId,String id,Product newProduct){
		logger.info("更新商品: " + id);

		Product oldProduct = productRepository.findById(id).orElse(null);
		if (oldProduct==null){
			return Result.fail(102,"查询失败","商品对象不存在");
		}
		User user = userRepository.findById(userId).orElse(null);

		String shopId = getShopId(user,oldProduct.getShopId());
		if (!shopId.equals(oldProduct.getShopId())){
			return Result.fail(102,"参数错误","不是商品拥有者");
		}

		String oldProductName = oldProduct.getProductName();
		String newProductName = newProduct.getProductName();

		if (StringUtils.isNotBlank(newProductName)&&!oldProductName.equals(newProductName)){
			Product existsProduct = productRepository.findByProductName(newProductName);
			if (existsProduct!=null){
				return Result.fail(102,"更新失败","新商品名: "+newProductName+" 已存在");
			}
		}

		//删除旧的图标
		String oldIcon = oldProduct.getProductMainIcon();
		String newIcon = newProduct.getProductMainIcon();
		if (StringUtils.isNotBlank(newIcon)) {
			if (StringUtils.isNotBlank(oldIcon)) {
				if (!oldIcon.equals(newIcon)) {
					FileUtil.deleteOldFileListByNewFileList(oldIcon,newIcon);
				}
			}
		}

		//删除旧的视频
		String oldVideo = oldProduct.getProductVideo();
		String newVideo = newProduct.getProductVideo();
		if (StringUtils.isNotBlank(newVideo)) {
			if (StringUtils.isNotBlank(oldVideo)) {
				if (!oldVideo.equals(newVideo)) {
					FileUtil.deleteOldFileListByNewFileList(oldVideo,newVideo);
				}
			}
		}

		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "sortNumber";

		//开始合并对象
		JavaBeanUtil.copyProperties(oldProduct,newProduct,ignoreProperties);

    	newProduct = productRepository.save(newProduct);

    	JSONObject productBean = (JSONObject)JSON.toJSON(newProduct);

    	return Result.success(200,"更新成功",productBean);
    }

	@Override
    public JSONObject findProductById(String id,List<Integer> priceLevelList){
    	logger.info("获取商品: " + id);
		Product product = productRepository.findById(id).orElse(null);
		if(product == null){
			return Result.fail(102,"查询失败","商品对象不存在");
		}
		if (!isAdminFind(product.isOnline(),priceLevelList)) {
			return Result.fail(102, "查询失败", "商品对象不存在");
		}
		//增加商品点击率
		product.setProductClickRate(product.getProductClickRate()+1);
		product = productRepository.save(product);

		JSONObject productBean = (JSONObject)JSON.toJSON(product);
		//将sku列表加入商品列表JSON中
		List<ProductSku> skuList = productSkuRepository.findByProductId(product.getId());
		if (skuList!=null&&!skuList.isEmpty()){
			ProductSku productSku = skuList.get(0);
			productSku.setSkuSales(productSku.getSkuClickRate()+1);
			productSkuRepository.save(productSku);
			productBean = getFixPriceAfterSkuList(priceLevelList,skuList,productBean);
		}else{
			productBean.put("skuList",new JSONArray());
		}
		Shop shop = shopRepository.findById(product.getShopId()).orElse(null);
		JSONObject shopBean = new JSONObject();
		if (shop!=null){
			shopBean = (JSONObject)JSON.toJSON(shop);
			shopBean.remove("price");
		}
		productBean.put("shop",shopBean);
		return Result.success(200,"查询成功",productBean);
    }

	@Override
    public JSONObject findProductByList(List<Integer> priceLevelList,String keyword,String productClassId,String shopId,Boolean online,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取商品列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}
		productRepository.updateTotalViewAndSales();
		Page productListPage = findProductList(keyword,productClassId,shopId,online,orderBy,beginTime,endTime,page,rows);

		if(productListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}

		List<Product> productList= productListPage.getContent();
		JSONArray productArray = new JSONArray();
		if (productList!=null&&!productList.isEmpty()){
			for (Product product :productList) {
				if (!isAdminFind(product.isOnline(),priceLevelList)) {
					continue;
				}
				JSONObject productBean = (JSONObject)JSON.toJSON(product);
				List<ProductSku> productSkuList = productSkuRepository.findByProductId(product.getId());
				productBean = getFixPriceAfterSkuList(priceLevelList, productSkuList, productBean);
				Shop shop = shopRepository.findById(product.getShopId()).orElse(null);
				JSONObject shopBean = new JSONObject();
				if (shop!=null){
					shopBean = (JSONObject)JSON.toJSON(shop);
					shopBean.remove("price");
				}
				productBean.put("shop",shopBean);
				productArray.add(productBean);
			}
		}

		JSONObject result = new JSONObject();
		result.put("rowsTotal",productListPage.getTotalElements());
		result.put("page",productListPage.getNumber()+1);
		result.put("rows",productListPage.getSize());
		result.put("productList",productArray);
		return Result.success(200,"查询成功",result);
	}

	private Page findProductList(String keyword,String productClassId,String shopId,Boolean online,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page productListPage = productRepository.findAll(new Specification<Product>() {
			@Override
			public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicateList = new ArrayList<>();
        		//指定查询对象
        		if (StringUtils.isNotBlank(keyword)) {
        			predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("productName"), "%" + keyword + "%")
        			// , criteriaBuilder.like(root.get("productDetail"), "%" + keyword + "%")
        			, criteriaBuilder.like(root.get("id"), "%" + keyword + "%")));
        		}

        		if(StringUtils.isNotBlank(productClassId)){
					predicateList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("productClassId"), productClassId)));
				}
				if(StringUtils.isNotBlank(shopId)){
					predicateList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("shopId"), shopId)));
				}
				if (online!=null){
					predicateList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("online"), online)));
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

        if (!productListPage.hasContent()){
        	return null;
        }

		return productListPage;
	}

	@Override
	public void exportExcel(String keyword,String productClassId,String shopId,Boolean online,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page productListPage = findProductList(keyword,productClassId,shopId,online, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<Product> productList = new ArrayList<>();

		if (productListPage!=null){
			productList.addAll(productListPage.getContent());
		}

		if (productList!=null&&!productList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(productList, "商品列表", "商品列表",Product.class, "商品列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}

    @Override
    public JSONObject productSortUpOrDown(String id, String upOrDown) {
        Product product = productRepository.findById(id).orElse(null);

        if (product==null){
            return Result.fail(102,"查询失败","该商品对象不存在");
        }
        Integer sortNumber = product.getSortNumber();
        if (sortNumber==0&&upOrDown.equals("上移")){
            return Result.fail(102,"排序失败","该商品当前已在同级别分类的最顶端");
        }

        Integer maxSortNumber = productRepository.findMaxSortNumberByProductClassId(product.getProductClassId());

        if (sortNumber==maxSortNumber&&upOrDown.equals("下移")){
            return Result.fail(102,"排序失败","该商品当前已在同级别分类的最末端");
        }
        if (upOrDown.equals("上移")){
            sortNumber = sortNumber-1;
        }

        if (upOrDown.equals("下移")){
            sortNumber = sortNumber+1;
        }
        Product brothersProduct = productRepository.findByProductClassIdAndSortNumber(product.getProductClassId(), sortNumber);
        if (brothersProduct!=null){
            brothersProduct.setSortNumber(product.getSortNumber());
            product.setSortNumber(sortNumber);
            productRepository.save(brothersProduct);
            product = productRepository.save(product);
        }
        return Result.success(200,"排序成功",product);
    }

	@Override
	public JSONObject confirmProduct(String id, Boolean confirm) {
    	logger.info("审核商品: "+ id);
		List<String> productIds = new ArrayList<>();
    	if (id.contains(",")){
			productIds = MoonUtil.getStringListByComma(id);
		}else {
			productIds.add(id);
		}
		int productRows = 0;
		int productSkuRows = 0;
    	if (productIds!=null&&!productIds.isEmpty()){
			productRows = productRepository.confirmProductByIds(productIds, confirm);
			productSkuRows = productSkuRepository.confirmProductSkuByProductIds(productIds, confirm);
		}
		return Result.success(200,"审核成功","审核商品: "+productRows+" 个,商品sku: "+productSkuRows+" 个");
	}

	private Boolean isAdminFind(boolean isOnline, List<Integer> priceLevelList){
		if (priceLevelList==null||priceLevelList.isEmpty()){
			priceLevelList = new ArrayList<>();
			priceLevelList.add(0);
		}

		if (!isOnline) {
			if (priceLevelList!=null&&!priceLevelList.isEmpty()){
				for (int level :priceLevelList) {
					if (level==999||level==998) {
						return true;
					}
				}
				return false;
			}else {
				return false;
			}
		}
		return true;
	}

	private  JSONObject getFixPriceAfterSkuList(List<Integer> priceLevelList,List<ProductSku> productSkuList,JSONObject productBean){
    	boolean isAdmin = false;

		int priceLevel = 0;

		if (priceLevelList==null||priceLevelList.isEmpty()){
			priceLevelList = new ArrayList<>();
			priceLevelList.add(0);
		}

    	for (Integer level :priceLevelList) {
			if (priceLevel == 0){
				priceLevel = level;
			}
			if(level==999||level==998){
				isAdmin = true;
			}
		}
		JSONArray productSkuArray = new JSONArray();

		for (int i = 0; i <productSkuList.size() ; i++) {
			JSONObject skuBean = (JSONObject) JSON.toJSON(productSkuList.get(i));
			if (isAdmin) {
				productSkuArray.add(skuBean);
			} else {
				if (productSkuList.get(i).isOnline()) {
					skuBean = whatPriceToShow(priceLevel, skuBean);
					productSkuArray.add(skuBean);
				}else{
					productSkuArray.add(new JSONObject());
				}
			}
			productBean.put("skuList", productSkuArray);
		}
		return productBean;
	}

	private JSONObject whatPriceToShow(int priceLevel,JSONObject skuBean){
    	// switch (priceLevel){
		// 	case 0:{
		// 		skuBean.put("skuPrice",skuBean.getBigDecimal("skuSalesPrice"));
		// 		break;
		// 	}
		// 	case 1:{
		// 		skuBean.put("skuPrice",skuBean.getBigDecimal("skuOrdinaryMemberPrice"));
		// 		break;
		// 	}
		// 	case 2:{
		// 		// skuBean.put("skuPrice",MoonUtil.mathematical(skuBean.getBigDecimal("skuSalesPrice"),"*",0.6,2));
		// 		skuBean.put("skuPrice",skuBean.getBigDecimal("skuVipMemberPrice"));
		// 		break;
		// 	}
		// 	case 3:{
		// 		skuBean.put("skuPrice",skuBean.getBigDecimal("skuPartnerPrice"));
		// 		break;
		// 	}
		// 	case 4:{
		// 		skuBean.put("skuPrice",skuBean.getBigDecimal("skuCommunityPartnerPrice"));
		// 		break;
		// 	}
		// 	default:{
		// 		skuBean.put("skuPrice",skuBean.getBigDecimal("skuSalesPrice"));
		// 		break;
		// 	}
		// }
		if (priceLevel<4){
			skuBean.remove("skuCommunityPartnerPrice");
		}
		if (priceLevel<3){
			skuBean.remove("skuCommunityPartnerPrice");
			skuBean.remove("skuPartnerPrice");
		}
		if (priceLevel<2){
			skuBean.remove("skuCommunityPartnerPrice");
			skuBean.remove("skuPartnerPrice");
			skuBean.remove("skuVipMemberPrice");
		}
		if (priceLevel<1){
			skuBean.remove("skuCommunityPartnerPrice");
			skuBean.remove("skuPartnerPrice");
			skuBean.remove("skuVipMemberPrice");
			skuBean.remove("skuOrdinaryMemberPrice");
		}
		return skuBean;
	}

	private String getShopId(User user,String userSendShopId){
		String shopId = "";
		Set<Role> roles = user.getRoles();
		for (Role role :roles) {
			if (role.getLevel()==4||role.getLevel()==997){
				Shop shop = shopRepository.findByUserId(user.getId());
				if (shop==null){
					return null;
				}
				shopId = shop.getId();
			}

			if (role.getLevel()>997){
				Shop shop = shopRepository.findByUserId(userSendShopId);
				if (shop==null){
					return null;
				}
				shopId = shop.getId();
			}
		}
		return shopId;
	}
}