package com.revisit.springboot.service.shop.impl;
import com.revisit.springboot.entity.product.Product;
import com.revisit.springboot.entity.productsku.ProductSku;
import com.revisit.springboot.entity.shop.Shop;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.repository.product.ProductRepository;
import com.revisit.springboot.repository.productsku.ProductSkuRepository;
import com.revisit.springboot.repository.shop.ShopRepository;
import com.revisit.springboot.repository.user.UserRepository;
import com.revisit.springboot.service.product.ProductService;
import com.revisit.springboot.service.shop.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletResponse;


/**
 * Shop逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-06-10 16:14:48
 */
@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
	private UserRepository userRepository;

    @Autowired
	private ProductRepository productRepository;

    @Autowired
	private ProductSkuRepository productSkuRepository;

    @Autowired
	private ProductService productService;

	private final static Logger logger = LoggerFactory.getLogger(ShopServiceImpl.class);

    @Override
    public JSONObject addShop(Shop shop) {
		logger.info("新增店铺");

		User user = userRepository.findById(shop.getUserId()).orElse(null);
		if (user == null){
			return Result.fail(102,"参数错误","用户不存在");
		}

		Shop oldShop = shopRepository.findByUserId(user.getId());
		if (oldShop!=null){
			return Result.fail(102,"参数错误","店铺已存在");
		}

		oldShop = shopRepository.findByName(shop.getName());
		if (oldShop!=null){
			return Result.fail(102,"参数错误","店铺名称已被使用");
		}

		if (StringUtils.isBlank(shop.getUserRealName())) {
			shop.setUserRealName(user.getRealName());
		}
		if (StringUtils.isBlank(shop.getMobile())) {
			shop.setMobile(user.getMobile());
		}
		shop.setPrice(new BigDecimal(0));
		//保存此对象
		shop = shopRepository.save(shop);

		// JSONObject shopBean = (JSONObject)JSON.toJSON(shop);

        if (StringUtils.isBlank(shop.getId())){
            return Result.fail(110,"系统错误","新增店铺失败,请联系管理员");
        }

        return Result.success(200,"新增店铺成功",shop);
    }

    @Override
    public JSONObject deleteShopById(String id){

		logger.info("删除店铺: " + id);

		List<String> ids = new ArrayList<>();
		int productRows = 0;
		if (StringUtils.contains(id,(","))){
			String[] split = StringUtils.split(id,",");
    		for (String s :split) {
				Shop shop = shopRepository.findById(s).orElse(null);
				if(shop!=null){
					List<Product> productList = productRepository.findByShopId(shop.getId());
					if (productList!=null&&!productList.isEmpty()){
						for (Product product :productList) {
							JSONObject result = productService.deleteProductById(shop.getUserId(), product.getId());
							if (result.getInteger("code")==200){
								productRows += 1;
							}else{
								return Result.fail(102,"参数错误","删除店铺商品时出错,错误原因: "+result.getString("errorMsg"));
							}
						}
					}
					ids.add(s);
    			}
			}
    	}else{
			Shop shop = shopRepository.findById(id).orElse(null);
			if (shop == null) {
				return Result.fail(102,"查询失败","店铺对象不存在");
			}
			List<Product> productList = productRepository.findByShopId(shop.getId());
			if (productList!=null&&!productList.isEmpty()){
				for (Product product :productList) {
					JSONObject result = productService.deleteProductById(shop.getUserId(), product.getId());
					if (result.getInteger("code")==200){
						productRows += 1;
					}else{
						return Result.fail(102,"参数错误","删除店铺商品时出错,错误原因: "+result.getString("errorMsg"));
					}
				}
			}
    		ids.add(id);
		}

		if (ids==null || ids.isEmpty()){
			return Result.fail(102,"查询失败","店铺对象不存在");
		}

		int shopRows = shopRepository.deleteByIds(ids);

    	return Result.success(200,"删除店铺成功","批量删除店铺成功,共删除店铺: " + shopRows + " 个,店铺内商品: "+productRows+" 个");
	}

    @Override
    public JSONObject updateShopById(String id,Shop newShop){
		logger.info("更新店铺: " + id);

		Shop oldShop = shopRepository.findById(id).orElse(null);
		if (oldShop==null){
			return Result.fail(102,"查询失败","店铺对象不存在");
		}
		
		if (!oldShop.getUserId().equals(newShop.getUserId())){
			return Result.fail(102,"查询失败","你不是店铺拥有者");
		}
		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "userId";

		//开始合并对象
		JavaBeanUtil.copyProperties(oldShop,newShop,ignoreProperties);

    	newShop = shopRepository.save(newShop);

    	JSONObject shopBean = (JSONObject)JSON.toJSON(newShop);

    	return Result.success(200,"更新成功",shopBean);
    }

	@Override
    public JSONObject findShopById(String id){
    	logger.info("获取店铺: " + id);

		Shop shop = shopRepository.findById(id).orElse(null);

		if(shop == null){
    		return Result.fail(102,"查询失败","店铺对象不存在");
    	}

    	JSONObject shopBean = (JSONObject)JSON.toJSON(shop);

		return Result.success(200,"查询成功",shopBean);
    }

	@Override
	public JSONObject findShopByUserId(String userId) {
		logger.info("获取我的店铺: " + userId);

		Shop shop = shopRepository.findByUserId(userId);

		if(shop == null){
			return Result.fail(102,"查询失败","店铺对象不存在");
		}

		JSONObject shopBean = (JSONObject)JSON.toJSON(shop);

		return Result.success(200,"查询成功",shopBean);
	}

	@Override
	public String findShopIdByUserId(String userId) {
		Shop shop = shopRepository.findByUserId(userId);

		if(shop == null){
			return "";
		}
		return shop.getId();
	}

	@Override
    public JSONObject findShopByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取店铺列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}
		Page shopListPage = findShopList(keyword,orderBy,beginTime,endTime,page,rows);

		if(shopListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}

		JSONObject result = new JSONObject();
		result.put("rowsTotal",shopListPage.getTotalElements());
		result.put("page",shopListPage.getNumber()+1);
		result.put("rows",shopListPage.getSize());
		result.put("shopList",shopListPage.getContent());
		return Result.success(200,"查询成功",result);
	}

	private Page findShopList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page shopListPage = shopRepository.findAll(new Specification<Shop>() {
			@Override
			public Predicate toPredicate(Root<Shop> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
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

        if (!shopListPage.hasContent()){
        	return null;
        }

		return shopListPage;
	}

	@Override
	public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page shopListPage = findShopList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<Shop> shopList = new ArrayList<>();

		if (shopListPage!=null){
			shopList.addAll(shopListPage.getContent());
		}

		if (shopList!=null&&!shopList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(shopList, "店铺列表", "店铺列表",Shop.class, "店铺列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}

}