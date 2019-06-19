package com.revisit.springboot.service.assembleproduct.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.component.uuid.CustomizeUUIDGenerate;
import com.revisit.springboot.entity.assembleproduct.AssembleProduct;
import com.revisit.springboot.entity.assembleproductsku.AssembleProductSku;
import com.revisit.springboot.entity.assemblerelation.AssembleRelation;
import com.revisit.springboot.entity.orderform.OrderForm;
import com.revisit.springboot.entity.product.Product;
import com.revisit.springboot.entity.productsku.ProductSku;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.entity.wallet.Wallet;
import com.revisit.springboot.repository.assembleproduct.AssembleProductRepository;
import com.revisit.springboot.repository.assembleproductsku.AssembleProductSkuRepository;
import com.revisit.springboot.repository.assemblerelation.AssembleRelationRepository;
import com.revisit.springboot.repository.orderform.OrderFormRepository;
import com.revisit.springboot.repository.product.ProductRepository;
import com.revisit.springboot.repository.productsku.ProductSkuRepository;
import com.revisit.springboot.repository.user.UserRepository;
import com.revisit.springboot.service.assembleproduct.AssembleProductService;
import com.revisit.springboot.service.user.UserService;
import com.revisit.springboot.service.wechat.WeChatPayService;
import com.revisit.springboot.utils.*;
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
 * AssembleProduct逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-05-04 22:45:06
 */
@Service
public class AssembleProductServiceImpl implements AssembleProductService {

    @Autowired
    private AssembleProductRepository assembleProductRepository;

    @Autowired
	private AssembleProductSkuRepository assembleProductSkuRepository;

    @Autowired
	private AssembleRelationRepository assembleRelationRepository;

    @Autowired
	private OrderFormRepository orderFormRepository;

    @Autowired
	private WeChatPayService weChatPayService;

   /* @Autowired
	private WalletRepository walletRepository;*/

	@Autowired
	private ProductRepository productRepository;

    @Autowired
	private UserService userService;

    @Autowired
	private UserRepository userRepository;

    @Autowired
	private ProductSkuRepository productSkuRepository;

    private final static Logger logger = LoggerFactory.getLogger(AssembleProductServiceImpl.class);

    @Override
    public JSONObject addAssembleProduct(AssembleProduct assembleProduct, String productId, Map<String,Object> skuIdMap) {
        logger.info("新增拼团商品");

		// AssembleProduct oldAssembleProduct = assembleProductRepository.findByProductId(assembleProduct.getProductId());

		// if (oldAssembleProduct!=null){
		// 	return Result.fail(102,"参数错误","拼团商品已存在");
		// }

		Product product = productRepository.findById(productId).orElse(null);
		if (product==null){
			return Result.fail(102,"参数错误","商品不存在");
		}

		AssembleProduct existAssembleProductName = assembleProductRepository.findByName(product.getProductName());
		if(existAssembleProductName!=null){
			return Result.fail(102,"参数错误","拼团商品名称已被使用");
		}

		Integer maxSortNumber = assembleProductRepository.findMaxSortNumber();

		//设置为最大的排序号
		if (maxSortNumber==null) {
			assembleProduct.setSortNumber(0);
		}else{
			assembleProduct.setSortNumber(maxSortNumber + 1);
		}

		//如果没填开始时间,默认马上开始
		if (assembleProduct.getBeginTime() == null){
			assembleProduct.setBeginTime(new Date());
		}else{
			Date nowTime = new Date();
			if (nowTime == MoonUtil.contrastTime(nowTime,assembleProduct.getBeginTime())){
				assembleProduct.setOnline(true);
				assembleProduct.setStatus("正在热拼");
			}else{
				assembleProduct.setOnline(false);
				assembleProduct.setStatus("等待开始");
			}
		}

		assembleProduct.setName(product.getProductName());
		assembleProduct.setVideo(product.getProductVideo());
		List<String> oldProductFileList = new ArrayList<>();
		String mainIcon = product.getProductMainIcon();
		if (StringUtils.isNotBlank(mainIcon)) {
			oldProductFileList.add(mainIcon);
			String fileList = FileUtil.copyFileList(oldProductFileList);
			assembleProduct.setMainIcon(fileList);
			oldProductFileList.clear();
		}

		String video = product.getProductVideo();
		if (StringUtils.isNotBlank(video)) {
			oldProductFileList.add(video);
			String fileList = FileUtil.copyFileList(oldProductFileList);
			assembleProduct.setVideo(fileList);
			oldProductFileList.clear();
		}

		assembleProduct.setTotalSales(product.getProductTotalSales());
		assembleProduct.setTotalViews(product.getProductTotalViews());
		assembleProduct.setNowTeamId(CustomizeUUIDGenerate.generateBase64UUID(UUID.randomUUID().toString()));

		//保存此对象
		assembleProduct = assembleProductRepository.save(assembleProduct);

		if (StringUtils.isBlank(assembleProduct.getId())){
			return Result.fail(110,"系统错误","新增拼团商品失败,请联系管理员");
		}

		for (Map.Entry skuEntry :skuIdMap.entrySet()) {
			//如果skuId不等于22个字符,跳过
			if (skuEntry.getKey().toString().length()!=22){
				continue;
			}
			ProductSku productSku = productSkuRepository.findById(skuEntry.getKey().toString()).orElse(null);
			//如果sku为空,跳过
			if (productSku==null){
				continue;
			}
			//如果sku的商品Id不一致,跳过
			if (!productSku.getProductId().equals(product.getId())){
				continue;
			}
			AssembleProductSku assembleProductSku = new AssembleProductSku();
			assembleProductSku.setAssembleProductId(assembleProduct.getId());
			List<String> oldProductSkuFileList = MoonUtil.getStringListByComma(productSku.getSkuAlbum());
			if (oldProductSkuFileList==null||oldProductSkuFileList.isEmpty()){
				oldProductSkuFileList = new ArrayList<>();
			}
			if (StringUtils.isNotBlank(productSku.getSkuAlbum())) {
				String fileList = FileUtil.copyFileList(oldProductSkuFileList);
				assembleProductSku.setSkuAlbum(fileList);
				oldProductSkuFileList.clear();
			}
			String skuIcon = productSku.getSkuIcon();
			if (StringUtils.isNotBlank(skuIcon)){
				oldProductSkuFileList.add(skuIcon);
				String fileList = FileUtil.copyFileList(oldProductSkuFileList);
				assembleProductSku.setSkuIcon(fileList);
				oldProductSkuFileList.clear();
			}
			String skuVideo = productSku.getSkuVideo();
			if (StringUtils.isNotBlank(skuVideo)){
				oldProductSkuFileList.add(skuVideo);
				String fileList = FileUtil.copyFileList(oldProductSkuFileList);
				assembleProductSku.setSkuVideo(fileList);
				oldProductSkuFileList.clear();
			}
			assembleProductSku.setSkuPrice(new BigDecimal(skuEntry.getValue().toString()));
			assembleProductSku.setSkuSalePrice(productSku.getSkuSalePrice());
			assembleProductSku.setSkuClickRate(productSku.getSkuClickRate());
			assembleProductSku.setSkuDetail(productSku.getSkuDetail());
			assembleProductSku.setSkuUnit(productSku.getSkuUnit());
			assembleProductSku.setSkuName(productSku.getSkuName());
			assembleProductSku.setSkuSales(productSku.getSkuSales());
			assembleProductSku.setOnline(true);
			assembleProductSku.setSortNumber(productSku.getSortNumber());
			assembleProductSkuRepository.save(assembleProductSku);
		}

		JSONObject assembleProductBean = (JSONObject)JSON.toJSON(assembleProduct);
        return Result.success(200,"新增拼团商品成功",assembleProductBean);
    }

    @Override
    public JSONObject deleteAssembleProductById(String id){

		logger.info("删除拼团商品: " + id);

		List<String> ids = new ArrayList<>();
		if (StringUtils.contains(id,(","))){
			String[] split = StringUtils.split(id,",");
    		for (String s :split) {
				AssembleProduct assembleProduct = assembleProductRepository.findById(s).orElse(null);
				if(assembleProduct!=null){
					assembleProductRepository.allSortNumberMinusOneBySortNumber(assembleProduct.getSortNumber());
					deleteFile(assembleProduct);
					ids.add(s);
    			}
			}
    	}else{
			AssembleProduct assembleProduct = assembleProductRepository.findById(id).orElse(null);
			if (assembleProduct == null) {
				return Result.fail(102,"查询失败","拼团商品对象不存在");
			}
			assembleProductRepository.allSortNumberMinusOneBySortNumber(assembleProduct.getSortNumber());
			deleteFile(assembleProduct);
    		ids.add(id);
		}

		if (ids==null || ids.isEmpty()){
			return Result.fail(102,"查询失败","拼团商品对象不存在");
		}

		int assembleProductRows = assembleProductRepository.deleteByIds(ids);
		int assembleProductSkuRows = assembleProductSkuRepository.deleteByAssembleProductIds(ids);

		return Result.success(200,"删除拼团商品成功","批量删除拼团商品成功,共删除拼团商品: " + assembleProductRows + " 个,拼团商品sku: "+assembleProductSkuRows+" 个");
	}

	private void deleteFile(AssembleProduct assembleProduct){
		if(assembleProduct!=null){
			List<AssembleProductSku> assembleProductSkuList = assembleProductSkuRepository.findByAssembleProductId(assembleProduct.getId());
			if (assembleProductSkuList!=null&&!assembleProductSkuList.isEmpty()){
				for (AssembleProductSku assembleProductSku :assembleProductSkuList) {
					assembleProductSkuRepository.allSortNumberMinusOneBySortNumberAndRelationId(assembleProductSku.getSortNumber(),assembleProductSku.getAssembleProductId());
					List<String> oldAssembleProductSkuAlbumList = MoonUtil.getStringListByComma(assembleProductSku.getSkuAlbum());
					FileUtil.deleteFileList(oldAssembleProductSkuAlbumList);
					List<String> oldAssembleProductSkuVideoList = MoonUtil.getStringListByComma(assembleProductSku.getSkuVideo());
					FileUtil.deleteFileList(oldAssembleProductSkuVideoList);
					List<String> oldAssembleProductSkuIconList = MoonUtil.getStringListByComma(assembleProductSku.getSkuIcon());
					FileUtil.deleteFileList(oldAssembleProductSkuIconList);
				}

			}
			List<String> oldAssembleProductIconList = MoonUtil.getStringListByComma(assembleProduct.getMainIcon());
			List<String> oldAssembleProductVideoList = MoonUtil.getStringListByComma(assembleProduct.getVideo());
			FileUtil.deleteFileList(oldAssembleProductIconList);
			FileUtil.deleteFileList(oldAssembleProductVideoList);
		}
	}

    @Override
    public JSONObject updateAssembleProductById(String id,AssembleProduct newAssembleProduct){
		logger.info("更新拼团商品: " + id);

		AssembleProduct oldAssembleProduct = assembleProductRepository.findById(id).orElse(null);
		if (oldAssembleProduct==null){
			return Result.fail(102,"查询失败","拼团商品对象不存在");
		}
		String name = newAssembleProduct.getName();
		if (StringUtils.isNotBlank(name)) {
			if (!oldAssembleProduct.getName().equals(name)) {
				AssembleProduct existAssembleProductName = assembleProductRepository.findByName(name);
				if(existAssembleProductName!=null){
					return Result.fail(102,"参数错误","拼团商品名称已被使用");
				}
			}
		}


		if (newAssembleProduct.getFullSize()!=oldAssembleProduct.getFullSize()&&oldAssembleProduct.getFullSize()<2){
			return Result.fail(102,"参数错误","拼团人数不能小于2");
		}
		Date endTime = newAssembleProduct.getEndTime();
		Date oldEndTime = oldAssembleProduct.getEndTime();
		Date nowTime = new Date();
		if (endTime!=null) {
			if (endTime.getTime()!=oldEndTime.getTime()) {
				if (nowTime == MoonUtil.contrastTime(nowTime, endTime)) {
					return Result.fail(102, "参数错误", "拼团商品结束时间不能小于当前时间");
				}
			}
		}
		Date beginTime = newAssembleProduct.getBeginTime();
		Date oldBeginTime = oldAssembleProduct.getBeginTime();
		if (beginTime!=null) {
			if (beginTime.getTime() != oldBeginTime.getTime()) {
				if (beginTime == MoonUtil.contrastTime(beginTime, endTime)) {
					return Result.fail(102, "参数错误", "拼团商品开始时间不能小于结束时间");
				}
			}
			if (nowTime == MoonUtil.contrastTime(nowTime, beginTime)) {
				newAssembleProduct.setStatus("正在热拼");
				newAssembleProduct.setOnline(true);
			}else{
				newAssembleProduct.setStatus("等待开始");
				newAssembleProduct.setOnline(false);
			}
		}

		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "productId,sortNumber,status";

		//开始合并对象
		JavaBeanUtil.copyProperties(oldAssembleProduct,newAssembleProduct,ignoreProperties);

    	newAssembleProduct = assembleProductRepository.save(newAssembleProduct);

    	JSONObject assembleProductBean = (JSONObject)JSON.toJSON(newAssembleProduct);

    	return Result.success(200,"更新成功",assembleProductBean);
    }

	@Override
    @Transactional(rollbackFor = { Exception.class })
    public JSONObject findAssembleProductById(String userId,String id){
    	logger.info("获取拼团商品: " + id);

		User user = userRepository.findById(userId).orElse(null);
		if (user==null){
			return Result.fail(102,"查询失败","获取用户失败");
		}

		boolean isAdmin = userService.isAdmin(user);

		AssembleProduct assembleProduct = assembleProductRepository.findById(id).orElse(null);

		if(assembleProduct == null){
    		return Result.fail(102,"查询失败","拼团商品对象不存在");
    	}

    	if (!assembleProduct.isOnline()){
			if (!isAdmin){
				return Result.fail(102,"查询失败","拼团商品对象已下架");
			}
		}

		Date nowTime = new Date();
		if (nowTime != MoonUtil.contrastTime(nowTime,assembleProduct.getBeginTime())){
			if (!assembleProduct.getStatus().equals("等待开始")){
				assembleProduct.setStatus("等待开始");
				assembleProduct.setOnline(false);
				assembleProduct = assembleProductRepository.save(assembleProduct);
			}
			if (!isAdmin) {
				return Result.fail(102, "查询失败", "拼团未开始");
			}
		}

		if (nowTime == MoonUtil.contrastTime(nowTime,assembleProduct.getBeginTime())
				&&assembleProduct.getEndTime()==MoonUtil.contrastTime(nowTime,assembleProduct.getEndTime())){
			if (!assembleProduct.getStatus().equals("正在热拼")){
				assembleProduct.setStatus("正在热拼");
				assembleProduct.setOnline(true);
				assembleProduct = assembleProductRepository.save(assembleProduct);
			}
		}

		if (nowTime == MoonUtil.contrastTime(nowTime,assembleProduct.getEndTime())){
			if (!assembleProduct.getStatus().equals("已结束")) {
				assembleProduct.setStatus("已结束");
				assembleProduct.setOnline(false);
				assembleProduct = assembleProductRepository.save(assembleProduct);
			}
			if (!isAdmin) {
				return Result.fail(102, "查询失败", "拼团已结束");
			}
		}

		assembleProduct.setClickRate(assembleProduct.getClickRate()+1);
		assembleProduct = assembleProductRepository.save(assembleProduct);
		if (StringUtils.isBlank(assembleProduct.getId())){
			return Result.fail(110,"系统错误","保存拼团商品时出错");
		}

		JSONObject assembleProductBean = (JSONObject)JSON.toJSON(assembleProduct);
		List<AssembleProductSku> assembleProductSkuList = assembleProductSkuRepository.findByAssembleProductId(assembleProduct.getId());
		if (assembleProductSkuList!=null&&!assembleProductSkuList.isEmpty()){
			AssembleProductSku assembleProductSku = assembleProductSkuList.get(0);
			assembleProductSku.setSkuClickRate(assembleProductSku.getSkuClickRate()+1);
			assembleProductSku = assembleProductSkuRepository.save(assembleProductSku);
		}
		JSONArray skuArray = new JSONArray();
		for (AssembleProductSku assembleProductSku :assembleProductSkuList) {
			if (!assembleProductSku.isOnline()) {
				continue;
			}
			JSONObject assembleProductSkuBean = (JSONObject) JSON.toJSON(assembleProductSku);
			skuArray.add(assembleProductSkuBean);
		}

		List<AssembleRelation> assembleRelationList = assembleRelationRepository.findByTeamId(assembleProduct.getNowTeamId());

		assembleProductBean.put("skuList",skuArray);
		assembleProductBean.put("assembleTeamList",assembleRelationList);
		return Result.success(200,"查询成功",assembleProductBean);
    }

	@Override
    @Transactional(rollbackFor = { Exception.class })
    public JSONObject findAssembleProductByList(String keyword,String userId,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取拼团商品列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}

		User user = userRepository.findById(userId).orElse(null);
		if (user==null){
			return Result.fail(102,"参数有误","获取用户失败");
		}

		boolean isAdmin = userService.isAdmin(user);

		assembleProductRepository.updateTotalViewAndSales();
		Page assembleProductListPage = findAssembleProductList(keyword,orderBy,beginTime,endTime,page,rows);

		if(assembleProductListPage==null){
    		return Result.fail(102,"参数有误","获取不到相关数据");
		}
		//获取搜索到的拼团商品集合
		List<AssembleProduct> assembleProductList = assembleProductListPage.getContent();

		//定义返回的拼团商品JSON数组
		JSONArray assembleProductArray = new JSONArray();

		Date nowTime = new Date();

		for (AssembleProduct assembleProduct :assembleProductList) {
			JSONArray assembleProductSkuArray = new JSONArray();
			//判断拼团商品是否上架,且在有效时间内
			if (!assembleProduct.isOnline()
					||nowTime != MoonUtil.contrastTime(nowTime,assembleProduct.getBeginTime())
					||nowTime == MoonUtil.contrastTime(nowTime,assembleProduct.getEndTime())) {
				//如果不是判断是否开始
				if (nowTime != MoonUtil.contrastTime(nowTime,assembleProduct.getBeginTime())){
					if (!assembleProduct.getStatus().equals("等待开始")){
						assembleProduct.setStatus("等待开始");
						assembleProduct.setOnline(false);
						assembleProduct = assembleProductRepository.save(assembleProduct);
					}
				}

				if (nowTime == MoonUtil.contrastTime(nowTime,assembleProduct.getBeginTime())
						&&assembleProduct.getEndTime()==MoonUtil.contrastTime(nowTime,assembleProduct.getEndTime())){
					if (!assembleProduct.getStatus().equals("正在热拼")){
						assembleProduct.setStatus("正在热拼");
						assembleProduct.setOnline(true);
						assembleProduct = assembleProductRepository.save(assembleProduct);
					}
				}
				//是否结束
				if (nowTime == MoonUtil.contrastTime(nowTime,assembleProduct.getEndTime())){
					if (!assembleProduct.getStatus().equals("已结束")) {
						assembleProduct.setStatus("已结束");
						assembleProduct.setOnline(false);
						assembleProduct = assembleProductRepository.save(assembleProduct);
					}
				}
				//如果不是管理员查询,跳过
				if (!isAdmin) {
					continue;
				}
			}
			JSONObject assembleProductBean = (JSONObject) JSON.toJSON(assembleProduct);
			List<AssembleProductSku> assembleProductSkuList = assembleProductSkuRepository.findByAssembleProductId(assembleProduct.getId());
			for (AssembleProductSku assembleProductSku :assembleProductSkuList) {
				//判断拼团商品sku是否在线
				if (!assembleProductSku.isOnline()) {
					//如果是管理员查询,不跳过
					if (!isAdmin) {
						continue;
					}
				}

				JSONObject assembleProductSkuBean = (JSONObject) JSON.toJSON(assembleProductSku);
				assembleProductSkuArray.add(assembleProductSkuBean);
			}
			int doneTeamTotal = assembleRelationRepository.findDoneTeamTotalByAssembleProductId(assembleProduct.getId());
			assembleProductBean.put("doneTeamTotal",doneTeamTotal);
			assembleProductBean.put("skuList",assembleProductSkuArray);
			assembleProductArray.add(assembleProductBean);
		}

		JSONObject result = new JSONObject();
		result.put("rowsTotal",assembleProductListPage.getTotalElements());
		result.put("page",assembleProductListPage.getNumber()+1);
		result.put("rows",assembleProductListPage.getSize());
		result.put("assembleProductList",assembleProductArray);
		return Result.success(200,"查询成功",result);
	}

	private Page findAssembleProductList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page assembleProductListPage = assembleProductRepository.findAll(new Specification<AssembleProduct>() {
			@Override
			public Predicate toPredicate(Root<AssembleProduct> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicateList = new ArrayList<>();
        		//指定查询对象
        		if (StringUtils.isNotBlank(keyword)) {
        			predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("name"), "%" + keyword + "%")
        			// , criteriaBuilder.like(root.get("id"), "%" + keyword + "%")
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

        if (!assembleProductListPage.hasContent()){
        	return null;
        }

		return assembleProductListPage;
	}

	@Override
	public void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

        //从数据库获取需要导出的数据
        Page assembleProductListPage = findAssembleProductList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<AssembleProduct> assembleProductList = new ArrayList<>();

		if (assembleProductListPage!=null){
			assembleProductList.addAll(assembleProductListPage.getContent());
		}

		if (assembleProductList!=null&&!assembleProductList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(assembleProductList, "拼团商品列表", "拼团商品列表",AssembleProduct.class, "拼团商品列表.xls", response);
		}else {
            try {
            	response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
            }catch (Exception e){
            	e.printStackTrace();
            }
		}
	}

	@Override
	public JSONObject assembleProductSortUpOrDown(String id, String upOrDown) {
		AssembleProduct assembleProduct = assembleProductRepository.findById(id).orElse(null);

		if (assembleProduct==null){
			return Result.fail(102,"查询失败","该商品对象不存在");
		}
		Integer sortNumber = assembleProduct.getSortNumber();
		if (sortNumber==0&&upOrDown.equals("上移")){
			return Result.fail(102,"排序失败","该商品当前已在同级别分类的最顶端");
		}

		Integer maxSortNumber = assembleProductRepository.findMaxSortNumber();

		if (sortNumber==maxSortNumber&&upOrDown.equals("下移")){
			return Result.fail(102,"排序失败","该商品当前已在同级别分类的最末端");
		}
		if (upOrDown.equals("上移")){
			sortNumber = sortNumber-1;
		}

		if (upOrDown.equals("下移")){
			sortNumber = sortNumber+1;
		}
		AssembleProduct assembleBrothersProduct = assembleProductRepository.findBySortNumber(sortNumber);
		if (assembleBrothersProduct!=null){
			assembleBrothersProduct.setSortNumber(assembleProduct.getSortNumber());
			assembleProduct.setSortNumber(sortNumber);
			assembleProductRepository.save(assembleBrothersProduct);
			assembleProduct = assembleProductRepository.save(assembleProduct);
		}
		return Result.success(200,"排序成功",assembleProduct);
	}
	
	/**
	 * 〈拍卖订单自动退款〉
	 *
	 * @param
	 * @return: 
	 * @since: 1.0.0
	 * @Author: Revisit-Moon
	 * @Date: 2019/5/8 9:35 PM
	 */

	@Override
	public JSONObject assembleProductAutomaticRefund() {
		/*List<AssembleRelation> notDoneTeamTotal = assembleRelationRepository.findByNotDoneTeamTotal();
		boolean isNeedSave = false;
		if (notDoneTeamTotal!=null){
			for (AssembleRelation assembleRelation :notDoneTeamTotal) {
				AssembleProduct assembleProduct = assembleProductRepository.findById(assembleRelation.getAssembleProductId()).orElse(null);
				if (assembleRelation!=null){
					assembleProduct.setStatus("已结束");
					assembleProduct.setOnline(false);
					assembleProduct = assembleProductRepository.save(assembleProduct);
				}
				OrderForm orderForm = orderFormRepository.findById(assembleRelation.getOrderFormId()).orElse(null);
				if (orderForm!=null){
					if (orderForm.getStatus().equals("已支付,待成团")
							&&orderForm.getUserId().equals(assembleRelation.getUserId())
							&&orderForm.getOrderFormType().equals("拼团")){
						isNeedSave = true;
						//微信退款到来源
						// JSONObject result = weChatPayService.weChatPayRefundMoney(orderForm, orderForm.getOrderFormFee());
						// if (result.getInteger("code")!=200){
						// 	return Result.fail(110,"拼团自动退款失败",result.getString("errorMsg"));
						// }
						Wallet wallet = walletRepository.findByUserId(orderForm.getUserId());
						if (wallet!=null){
							wallet.setNoWithdrawPrice(wallet.getNoWithdrawPrice().add(orderForm.getOrderFormFee()));
							wallet = walletRepository.save(wallet);
							if (StringUtils.isBlank(wallet.getId())){
								return Result.fail(110,"系统错误","未成团订单退款失败");
							}
							orderForm.setStatus("已退款");
							orderForm = orderFormRepository.save(orderForm);
							if (StringUtils.isBlank(orderForm.getId())){
								return Result.fail(110,"系统错误","更新拼团退款订单失败");
							}

							JSONObject refundRecordingBean = orderFormRecordingService.addOrderFormRefundRecording(orderForm, orderForm.getOrderFormType() + "退款");
							if (refundRecordingBean.getInteger("code")!=200){
								return Result.fail(110,"新增拼团退款记录失败",refundRecordingBean.getString("errorMsg"));
							}
							assembleRelation.setStatus("拼团失败,已退款");
						}
					}
				}
			}
		}
		if (isNeedSave) {
			notDoneTeamTotal = assembleRelationRepository.saveAll(notDoneTeamTotal);
			if (notDoneTeamTotal == null || notDoneTeamTotal.isEmpty()) {
				Result.fail(110, "系统错误", "更新拼团记录失败");
			}
			return Result.success(200,"拼团自动退款成功",notDoneTeamTotal);
		}else{
			return Result.success(200,"拼团自动退款成功","无需更新");
		}*/
		return null;
	}

}