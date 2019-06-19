package com.revisit.springboot.service.shoppingcart.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.product.Product;
import com.revisit.springboot.entity.productsku.ProductSku;
import com.revisit.springboot.entity.shop.Shop;
import com.revisit.springboot.entity.shoppingcart.ShoppingCart;
import com.revisit.springboot.entity.shoppingcart.ShoppingCartItem;
import com.revisit.springboot.entity.user.Role;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.repository.product.ProductRepository;
import com.revisit.springboot.repository.productsku.ProductSkuRepository;
import com.revisit.springboot.repository.shop.ShopRepository;
import com.revisit.springboot.repository.shoppingcart.ShoppingCartItemRepository;
import com.revisit.springboot.repository.shoppingcart.ShoppingCartRepository;
import com.revisit.springboot.repository.user.UserRepository;
import com.revisit.springboot.service.productsku.ProductSkuService;
import com.revisit.springboot.service.shoppingcart.ShoppingCartService;
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
import java.math.BigDecimal;
import java.util.*;


/**
 * ShoppingCart逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-04-15 11:05:45
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ShoppingCartItemRepository shoppingCartItemRepository;

    @Autowired
	private ProductSkuRepository productSkuRepository;

    @Autowired
	private ProductRepository productRepository;

    @Autowired
	private ShopRepository shopRepository;

    @Autowired
	private UserRepository userRepository;

    @Autowired
	private ProductSkuService productSkuService;

    private final static Logger logger = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);

    @Override
    public JSONObject addShoppingCart(String userId, ShoppingCartItem shoppingCartItem) {
        logger.info("新增购物车内商品");

		User user = userRepository.findById(userId).orElse(null);
		if (user==null){
			return Result.fail(110,"参数错误","用户不存在");
		}
		Set<Role> roles = user.getRoles();
		int priceLevel = 0;
		for (Role role :roles) {
			priceLevel = role.getLevel();
			break;
		}

		ShoppingCart newShoppingCart = shoppingCartRepository.findByUserId(userId);
		if (newShoppingCart==null){
			newShoppingCart = new ShoppingCart();
			newShoppingCart.setUserId(userId);
			newShoppingCart = shoppingCartRepository.save(newShoppingCart);
		}

		ProductSku productSku = productSkuRepository.findById(shoppingCartItem.getProductSkuId()).orElse(null);

		Product product = productRepository.findByProductSkuId(productSku.getId());

		if (product == null){
			return Result.fail(110,"参数错误","商品不存在");
		}

		if (productSku == null){
			return Result.fail(110,"参数错误","商品sku不存在");
		}

		BigDecimal unitPrice = productSkuService.whatPriceByRoleNameAndSku(priceLevel,productSku);

		int totalQuantity = 0;

		ShoppingCartItem oldShoppingCartItem = shoppingCartItemRepository.findByShoppingCartIdAndProductSkuId(newShoppingCart.getId(), productSku.getId());
		if (oldShoppingCartItem==null){
			oldShoppingCartItem = new ShoppingCartItem();
		}else {
			oldShoppingCartItem.setQuantity(oldShoppingCartItem.getQuantity()+shoppingCartItem.getQuantity());
		}

		oldShoppingCartItem.setProductId(product.getId());
		oldShoppingCartItem.setShopId(product.getShopId());
		oldShoppingCartItem.setProductSkuName(productSku.getSkuName());
		oldShoppingCartItem.setProductSkuAlbum(productSku.getSkuAlbum());
		oldShoppingCartItem.setProductSkuIcon(productSku.getSkuIcon());
		oldShoppingCartItem.setProductSkuVideo(productSku.getSkuVideo());
		oldShoppingCartItem.setProductSkuOnline(productSku.isOnline());
		oldShoppingCartItem.setPrice(unitPrice);
		totalQuantity += shoppingCartItem.getQuantity();

        newShoppingCart.setUserId(userId);
        newShoppingCart.setTotalQuantity(totalQuantity);

        //保存此对象
        newShoppingCart = shoppingCartRepository.save(newShoppingCart);
        if (StringUtils.isBlank(newShoppingCart.getId())){
            return Result.fail(110,"系统错误","新增购物车失败,请联系管理员");
        }
        JSONObject shoppingCart = (JSONObject) JSON.toJSON(oldShoppingCartItem);
        shoppingCartItem.setShoppingCartId(oldShoppingCartItem.getId());
		oldShoppingCartItem = shoppingCartItemRepository.save(oldShoppingCartItem);
        if (StringUtils.isBlank(oldShoppingCartItem.getId())){
            return Result.fail(110,"系统错误","新增购物车内商品失败,请联系管理员");
        }

        return Result.success(200,"新增成功","新增购物车成功");

	}

    @Override
    public JSONObject deleteShoppingCartItemByProductSkuId(String userId,String productSkuId){

		logger.info("删除购物车内商品: " + productSkuId);

        ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId);

        List<String> ids = new ArrayList<>();
		if (StringUtils.contains(productSkuId,(","))){
			String[] split = StringUtils.split(productSkuId,",");
    		for (String s :split) {
				ShoppingCartItem shoppingCartItem = shoppingCartItemRepository.findByShoppingCartIdAndProductSkuId(shoppingCart.getId(),productSkuId);
				if(shoppingCartItem!=null){
                    shoppingCart.setTotalQuantity(shoppingCart.getTotalQuantity()-shoppingCartItem.getQuantity());
					ids.add(shoppingCartItem.getId());
    			}
			}
    	}else{
            ShoppingCartItem shoppingCartItem = shoppingCartItemRepository.findByShoppingCartIdAndProductSkuId(shoppingCart.getId(),productSkuId);
			if (shoppingCartItem == null) {
				return Result.fail(102,"查询失败","购物车内商品不存在");
			}
            shoppingCart.setTotalQuantity(shoppingCart.getTotalQuantity()-shoppingCartItem.getQuantity());
            ids.add(shoppingCartItem.getId());
		}

		if (ids==null || ids.isEmpty()){
			return Result.fail(102,"查询失败","购物车内商品不存在");
		}

		int shoppingCartItemRows = shoppingCartItemRepository.deleteByIds(ids);
        shoppingCartRepository.save(shoppingCart);

    	return Result.success(200,"删除购物车内商品成功","批量删除购物车内商品成功,共删除购物车内商品: " + shoppingCartItemRows + " 个");
	}

	@Override
	public JSONObject andSubtractShoppingCart(String userId, String skuId, Boolean isAdd, Integer quantity) {
		ShoppingCart shoppingCart = shoppingCartRepository.findByUserId(userId);
		if(shoppingCart==null){
			return Result.fail(102,"查询失败","获取不到购物车");
		}

		ShoppingCartItem shoppingCartItem = shoppingCartItemRepository.findByShoppingCartIdAndProductSkuId(shoppingCart.getId(),skuId);

		if (shoppingCartItem==null){
			return Result.fail(102,"查询失败","购物车内商品不存在");
		}

		if (isAdd!=null&&quantity==null) {
			if (isAdd) {
				shoppingCartItem.setQuantity(shoppingCartItem.getQuantity() + 1);
				shoppingCart.setTotalQuantity(shoppingCart.getTotalQuantity() + 1);
				shoppingCartItem = shoppingCartItemRepository.save(shoppingCartItem);
				if (StringUtils.isBlank(shoppingCartItem.getId())) {
					return Result.fail(110, "系统错误", "增加购物车内商品数量失败,请联系管理员");
				}
				shoppingCart = shoppingCartRepository.save(shoppingCart);
				if (StringUtils.isBlank(shoppingCart.getId())) {
					return Result.fail(110, "系统错误", "增加购物车总数失败,请联系管理员");
				}
			}

			if (!isAdd) {
				if (shoppingCartItem.getQuantity() - 1 <= 0) {
					shoppingCart.setTotalQuantity(shoppingCart.getTotalQuantity() - 1);
					shoppingCartItemRepository.deleteById(shoppingCartItem.getId());
					shoppingCart = shoppingCartRepository.save(shoppingCart);
					if (StringUtils.isBlank(shoppingCart.getId())) {
						return Result.fail(110, "系统错误", "减少购物车总数失败,请联系管理员");
					}

				} else {
					shoppingCartItem.setQuantity(shoppingCartItem.getQuantity() - 1);
					shoppingCart.setTotalQuantity(shoppingCart.getTotalQuantity() - 1);
					shoppingCartItem = shoppingCartItemRepository.save(shoppingCartItem);
					if (StringUtils.isBlank(shoppingCartItem.getId())) {
						return Result.fail(110, "系统错误", "减少购物车内商品数量失败,请联系管理员");
					}
					shoppingCart = shoppingCartRepository.save(shoppingCart);
					if (StringUtils.isBlank(shoppingCart.getId())) {
						return Result.fail(110, "系统错误", "减少购物车总数失败,请联系管理员");
					}
				}
			}
		}

		if (isAdd==null&&quantity!=null){
			int oldQuantity = shoppingCartItem.getQuantity();
			if (oldQuantity<quantity){
				shoppingCart.setTotalQuantity(shoppingCart.getTotalQuantity()+quantity);
			}
			if (oldQuantity>quantity){
				shoppingCart.setTotalQuantity(shoppingCart.getTotalQuantity()-quantity);
			}
			shoppingCartItem.setQuantity(quantity);
			shoppingCartItem = shoppingCartItemRepository.save(shoppingCartItem);
			if (StringUtils.isBlank(shoppingCartItem.getId())) {
				return Result.fail(110, "系统错误", "更新购物车内商品数量失败,请联系管理员");
			}
			shoppingCart = shoppingCartRepository.save(shoppingCart);
			if (StringUtils.isBlank(shoppingCart.getId())) {
				return Result.fail(110, "系统错误", "更新购物车总数失败,请联系管理员");
			}
		}
		return Result.success(200,"更新成功","修改购物车成功");
	}

	@Override
    public JSONObject updateShoppingCartById(String id,ShoppingCart newShoppingCart){
		logger.info("更新购物车: " + id);

		ShoppingCart oldShoppingCart = shoppingCartRepository.findById(id).orElse(null);
		if (oldShoppingCart==null){
			return Result.fail(102,"查询失败","购物车对象不存在");
		}

		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "";

		//开始合并对象
		JavaBeanUtil.copyProperties(oldShoppingCart,newShoppingCart,ignoreProperties);

    	newShoppingCart = shoppingCartRepository.save(newShoppingCart);

    	JSONObject shoppingCartBean = (JSONObject)JSON.toJSON(newShoppingCart);

    	return Result.success(200,"更新成功",shoppingCartBean);
    }

	@Override
    public JSONObject findShoppingCartByUserId(String userId){
    	logger.info("获取购物车: " + userId);

		ShoppingCart oldShoppingCar = shoppingCartRepository.findByUserId(userId);

		User user = userRepository.findById(userId).orElse(null);
		if (user==null){
			return Result.fail(110,"参数错误","用户不存在");
		}
		Set<Role> roles = user.getRoles();
		int priceLevel = 0;
		for (Role role :roles) {
			priceLevel = role.getLevel();
			break;
		}

		if (oldShoppingCar==null){
            return Result.fail(102,"查询失败","当前还没有购物车,请先去添加商品到购物车");
        }

		List<ShoppingCartItem> shoppingCartItemList = shoppingCartItemRepository.findByShoppingCartId(oldShoppingCar.getId());

		if (shoppingCartItemList!=null&&!shoppingCartItemList.isEmpty()) {
			Set<String> shopIdSet = new HashSet<>();
			int totalQuantity = 0;
			for (ShoppingCartItem shoppingCartItem : shoppingCartItemList) {
				ProductSku productSku = productSkuRepository.findById(shoppingCartItem.getProductSkuId()).orElse(null);
				if (productSku!=null) {

					Product product = productRepository.findByProductSkuId(productSku.getId());

					if (product != null){
						shoppingCartItem.setProductId(product.getId());
						shoppingCartItem.setShopId(product.getShopId());
						shopIdSet.add(product.getShopId());
					}
					//更新购物车sku图标
					String skuIcon = productSku.getSkuIcon();
					shoppingCartItem.setProductSkuIcon(skuIcon);
					//更新购物车sku相册
					String skuAlbum = productSku.getSkuAlbum();
					shoppingCartItem.setProductSkuAlbum(skuAlbum);
					//更新购物车sku视频
					String skuVideo = productSku.getSkuVideo();
					shoppingCartItem.setProductSkuVideo(skuVideo);
					//更新购物车sku名称
					String skuName = productSku.getSkuName();
					shoppingCartItem.setProductSkuName(skuName);

					//更新购物车sku上下架状态
					shoppingCartItem.setProductSkuOnline(productSku.isOnline());
					totalQuantity = totalQuantity + shoppingCartItem.getQuantity();
					//更新购物车价格
					BigDecimal price = productSkuService.whatPriceByRoleNameAndSku(priceLevel, productSku);
					shoppingCartItem.setPrice(price);
				}
			}
			oldShoppingCar.setTotalQuantity(totalQuantity);
			//更新购物车内项
			shoppingCartItemList = shoppingCartItemRepository.saveAll(shoppingCartItemList);
			//更新购物车总数
			oldShoppingCar = shoppingCartRepository.save(oldShoppingCar);

			//定义返回值最外层
			JSONObject shoppingCart = (JSONObject) JSON.toJSON(oldShoppingCar);
			//搜索店铺列表
			List<Shop> shopList = shopRepository.findAllById(shopIdSet);
			//根据店铺分组购物车内商品
			JSONArray shoppingCartItemArray = new JSONArray();
			for (Shop shop :shopList) {
				//定义返回值内层店铺
				JSONObject shopBean = (JSONObject)JSON.toJSON(shop);
				//定义返回值店铺层内层
				JSONArray productSkuList = new JSONArray();
				for (ShoppingCartItem shoppingCartItem :shoppingCartItemList) {
					//如果购物车内店铺一致和搜索的店铺列表ID一致
					if (shoppingCartItem.getShopId().equals(shop.getId())){
						//则转化JSON数据,并加入店铺层内层
						JSONObject shoppingCartItemBean = (JSONObject)JSON.toJSON(shoppingCartItem);
						productSkuList.add(shoppingCartItemBean);
					}
				}
				shopBean.put("productSkuList",productSkuList);
				shoppingCartItemArray.add(shopBean);
			}

			shoppingCart.put("shoppingCartItemList", shoppingCartItemArray);
			return Result.success(200,"查询成功",shoppingCart);
		}

		return Result.fail(102,"参数错误", "您当前购物还没有商品,请先添加商品");

    }

	@Override
    public JSONObject findShoppingCartByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取购物车列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}
		Page shoppingCartListPage = findShoppingCartList(keyword,orderBy,beginTime,endTime,page,rows);

		if(shoppingCartListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}

    	JSONObject result = new JSONObject();
		result.put("rowsTotal",shoppingCartListPage.getTotalElements());
		result.put("page",shoppingCartListPage.getNumber()+1);
		result.put("rows",shoppingCartListPage.getSize());
		result.put("shoppingCartList",shoppingCartListPage.getContent());
		return Result.success(200,"查询成功",result);
	}

	private Page findShoppingCartList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page shoppingCartListPage = shoppingCartRepository.findAll(new Specification<ShoppingCart>() {
			@Override
			public Predicate toPredicate(Root<ShoppingCart> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
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

        if (!shoppingCartListPage.hasContent()){
        	return null;
        }

		return shoppingCartListPage;
	}

	@Override
	public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page shoppingCartListPage = findShoppingCartList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<ShoppingCart> shoppingCartList = new ArrayList<>();

		if (shoppingCartListPage!=null){
			shoppingCartList.addAll(shoppingCartListPage.getContent());
		}

		if (shoppingCartList!=null&&!shoppingCartList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(shoppingCartList, "购物车列表", "购物车列表",ShoppingCart.class, "购物车列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}

}