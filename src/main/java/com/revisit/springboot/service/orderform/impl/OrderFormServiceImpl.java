package com.revisit.springboot.service.orderform.impl;
import com.revisit.springboot.entity.addressmanage.AddressManage;
import com.revisit.springboot.entity.orderform.OrderForm;
import com.revisit.springboot.entity.productsku.ProductSku;
import com.revisit.springboot.entity.shop.Shop;
import com.revisit.springboot.entity.system.SystemSetting;
import com.revisit.springboot.entity.user.Role;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.repository.addressmanage.AddressManageRepository;
import com.revisit.springboot.repository.orderform.OrderFormRepository;
import com.revisit.springboot.repository.product.ProductRepository;
import com.revisit.springboot.repository.productsku.ProductSkuRepository;
import com.revisit.springboot.repository.shop.ShopRepository;
import com.revisit.springboot.repository.system.SystemSettingRepository;
import com.revisit.springboot.repository.user.UserRepository;
import com.revisit.springboot.service.orderform.OrderFormService;
import com.revisit.springboot.service.product.ProductService;
import com.revisit.springboot.service.productsku.ProductSkuService;
import com.revisit.springboot.service.wechat.WeChatPayService;
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

import java.math.BigDecimal;
import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletResponse;


/**
 * OrderForm逻辑层接口类
 * @author Revisit-Moon
 * @date 2019-03-01 15:58:42
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class OrderFormServiceImpl implements OrderFormService {

    @Autowired
    private OrderFormRepository orderFormRepository;

    @Autowired
	private UserRepository userRepository;

    @Autowired
	private ShopRepository shopRepository;

    @Autowired
	private SystemSettingRepository systemSettingRepository;

    @Autowired
	private AddressManageRepository addressManageRepository;

    @Autowired
	private ProductSkuService productSkuService;

    @Autowired
	private WeChatPayService weChatPayService;

    @Autowired
	private ProductSkuRepository productSkuRepository;

    private final static Logger logger = LoggerFactory.getLogger(OrderFormServiceImpl.class);

	@Override
	public JSONObject addOrderForm(OrderForm orderForm,String addressId) {

		logger.info("新增订单");

		//获取前端输入的用户ID
		String userId = orderForm.getUserId();
		if (StringUtils.isBlank(userId)){
			return Result.fail(102,"参数错误","用户id不能为空");
		}

		//如果用户不存在,可能是已删除用户
		User user = userRepository.findById(userId).orElse(null);
		if (user==null){
			return Result.fail(102,"参数错误","用户对象不存在");
		}

		//如果用户存在则设置订单买家ID
		orderForm.setUserId(user.getId());
		//设置微信openId
		orderForm.setOpenId(user.getWeChatOpenId());

		//获取订单支付方式
		String paymentMode = orderForm.getPaymentMode();

		//如果订单支付方式为空,则返回参数错误
		if (StringUtils.isBlank(paymentMode)){
			return Result.fail(102,"参数错误","支付方式不能为空");
		}

		//判断是否当前系统支付的支付方式,如果不是则返回参数错误
		if (!paymentMode.equals("微信支付")
				&&!paymentMode.equals("支付宝支付")
				&&!paymentMode.equals("线下支付")
				&&!paymentMode.equals("钱包支付")){
			return Result.fail(102,"参数错误","请重新选择支付方式");
		}

		//如果需要调用第三方支付模块,则判断第三模块必填参数是否已填
		if (!paymentMode.equals("钱包支付")&&!paymentMode.equals("线下支付")){
			//获取第三方支付方式场景是否存在
			String useScenes = orderForm.getUseScenes();
			if (StringUtils.isBlank(useScenes)){
				return Result.fail(102,"参数错误","非钱包或线下支付使用场景值必填");
			}
			//如果是微信支付则必须是JSAPI或NATIVE或MWEB
			if (!useScenes.equals("JSAPI")
					&&!useScenes.equals("NATIVE")
					&&!useScenes.equals("MWEB")){
				return Result.fail(102,"参数错误",paymentMode+"方式下场景不正确: "+useScenes);
			}
		}

		String orderFormType = orderForm.getOrderFormType();
		if (StringUtils.isBlank(orderFormType)){
			return Result.fail(102,"参数错误","订单类型不能为空");
		}

		//判断订单是否已存在
		OrderForm oldOrderForm = orderFormRepository.findByUserIdAndProductData(userId,orderForm.getProductData());
		if (oldOrderForm!=null){
			//更新旧对象
			orderForm.setId(oldOrderForm.getId());
			//判断旧订单的支付方式是否一致
			if (oldOrderForm.getPaymentMode().equals(orderForm.getPaymentMode())){
				if (!orderForm.getPaymentMode().equals("钱包支付")&&!orderForm.getPaymentMode().equals("线下转账")){
					if (oldOrderForm.getUseScenes().equals(orderForm.getUseScenes())){
						JSONObject waitPayOrderFromResult = new JSONObject();
						if(!oldOrderForm.getOrderFormType().equals("团购")) {
							waitPayOrderFromResult.put("errorMsg", "您已提交过该订单,请前往支付");
							waitPayOrderFromResult.put("waitPayOrderFormId", orderForm.getId());
							waitPayOrderFromResult.put("code", 101);
							waitPayOrderFromResult.put("msg", "新增订单失败");
							return waitPayOrderFromResult;
						}
					}
				}
			}
		}else{
			//先保存订单
			orderForm = orderFormRepository.save(orderForm);
		}

		if (StringUtils.isBlank(orderForm.getId())){
			return Result.fail(110,"系统错误","新增订单失败,请联系管理员");
		}

		JSONObject result = null;
		switch (orderFormType){
			//调用商品订单函数
			case "商品":{
				result = productOrderForm(orderForm, addressId, user);
				break;
			}
			//调用团购订单函数
			case "团购":{
				// result = assembleProductOrderForm(orderForm, addressId, user);
				break;
			}
			//调用充值会员订单函数
			case "充值会员":{
				// result = rechargeOrderForm(orderForm);
				break;
			}
			//调用充值钱包订单函数
			case "充值钱包":{
				// result = rechargeOrderForm(orderForm);
				break;
			}
			default:
				break;
		}


		if (result == null||result.getInteger("code")!=200){
			return Result.fail(110,"新增订单失败",result.getString("errorMsg"));
		}

		return Result.success(200,"新增订单成功",result);

		// JSONObject orderFormBean = (JSONObject)JSON.toJSON(orderForm);

	}

	//商品订单模块
	private JSONObject productOrderForm(OrderForm orderForm,String addressId,User user){

		//获取用户角色名
		int level = 0;
		Set<Role> roles = user.getRoles();
		for (Role role : roles) {
			level = role.getLevel();
			break;
		}

		String deliveryMode = orderForm.getDeliveryMode();
		if (StringUtils.isBlank(deliveryMode)){
			return Result.fail(102,"参数错误","请选择配送方式");
		}

		if (!deliveryMode.equals("配送")&&!deliveryMode.equals("自提")){
			return Result.fail(102,"参数错误","不支持该配送方式");
		}

		if (deliveryMode.equals("配送")) {
			if (StringUtils.isBlank(addressId)) {
				return Result.fail(102, "参数错误", "地址不能为空");
			}

			//判断收货地址是否存在
			AddressManage addressManage = addressManageRepository.findByIdAndUserId(addressId, user.getId());

			//如果收货人为空,则返回参数错误
			String consignee = addressManage.getConsignee();
			if (StringUtils.isBlank(consignee)) {
				return Result.fail(102, "参数错误", "请输入收货人名称");
			}

			//如果收货人手机为空,则返回参数错误
			String contactMobile = addressManage.getContactMobile();
			if (StringUtils.isBlank(contactMobile)) {
				return Result.fail(102, "参数错误", "请输入收件人联系电话");
			}

			//如果收货地址为空,则返回参数错误
			String address = addressManage.getAddress();
			if (StringUtils.isBlank(address)) {
				return Result.fail(102, "参数错误", "请输入收货地址");
			}

			orderForm.setConsignee(consignee);
			orderForm.setContactMobile(contactMobile);
			orderForm.setAddress(addressManage.getProvince()+addressManage.getCity()+addressManage.getDistrict()+addressManage.getAddress());

		}

		if (deliveryMode.equals("自提")){

			if (StringUtils.isBlank(orderForm.getConsignee())) {
				String realName = user.getRealName();
				if (StringUtils.isNotBlank(realName)){
					orderForm.setConsignee(realName);
				}else{
					orderForm.setConsignee(user.getWeChatName());
				}
			}
			if (StringUtils.isBlank(orderForm.getContactMobile())) {
				String mobile = user.getMobile();
				if (StringUtils.isNotBlank(mobile)) {
					orderForm.setContactMobile(mobile);
				}
			}

		}


		//转换商品数据为JSON对象
		JSONArray productArray = JSONArray.parseArray(orderForm.getProductData());
		if (productArray==null||productArray.isEmpty()){
			return Result.fail(102,"参数错误","商品数据不能为空");
		}

		//订单详细购买内容
		String orderFormDetail = "";

		//前端填写的订单总价
		BigDecimal orderFormFee = orderForm.getOrderFormFee();
		if (orderFormFee.compareTo(new BigDecimal(0))<=0){
			return Result.fail(102,"参数错误","订单价格不能为空");
		}

		//系统计算的订单总价
		BigDecimal totalPrice = new BigDecimal(0);

		//系统计算的纯商品价格
		BigDecimal productFee = new BigDecimal(0);

		//订单运费
		BigDecimal totalLogisticsFee = new BigDecimal(0);

		//系统计算的包装费
		BigDecimal totalPackageFee = new BigDecimal(0);

		//总购买数量
		int totalBuyNumber = 0;

		SystemSetting systemSetting = systemSettingRepository.findSystemSetting();

		//获取订单数据内容
		for (int i = 0; i < productArray.size() ; i++) {
			JSONObject productData = null;
			try {
				productData = productArray.getJSONObject(i);
				String skuId = productData.getString("skuId");
				if(StringUtils.isBlank(skuId)){
					return Result.fail(102,"参数错误","商品ID不能为空");
				}

				int buyNumber = productData.getInteger("buyNumber");

				if(buyNumber==0){
					return Result.fail(102,"参数错误","购买数量不能为0");
				}

				BigDecimal unitPrice = productData.getBigDecimal("unitPrice");

				if(unitPrice.compareTo(new BigDecimal(0))==0){
					return Result.fail(102,"参数错误","单价不能为0");
				}

				ProductSku sku = productSkuRepository.findById(skuId).orElse(null);

				if(sku == null){
					return Result.fail(102,"参数错误","部分商品sku不存在,请确认下单项");
				}

				if(!sku.isOnline()){
					return Result.fail(102,"参数错误","部分商品sku下架,请确认下单项");
				}

				// if (systemSetting.isNeedStock()) {
				if ((sku.getSkuStock() - buyNumber) < 0) {
					return Result.fail(102, "参数错误", "部分商品sku库存不足,请确认下单项");
				}
				// }

				//计算单价是否一致
				BigDecimal realPrice = productSkuService.whatPriceByRoleNameAndSku(level, sku);
				if (unitPrice.compareTo(realPrice)!=0){
					return Result.fail(102,"参数错误","获取到的单价和实际单价不一致");
				}

				BigDecimal unitTotalPrice = MoonUtil.mathematical(unitPrice, "*", buyNumber, 2);

				//总购买数
				totalBuyNumber = totalBuyNumber + buyNumber;

				//纯商品价
				productFee = productFee.add(unitTotalPrice);

				orderFormDetail = orderFormDetail+ "["+sku.getSkuName()+" "+buyNumber+" "+sku.getSkuUnit()+" "+unitTotalPrice.toString()+" 元],";

				//计算价格
			}catch (Exception e){
				return Result.fail(102,"参数错误","商品数据转换出错");
			}
		}

		BigDecimal systemDeliveryFee = new BigDecimal(0);

		//如果是配送,则加上系统统一运费
		if (deliveryMode.equals("配送")) {
			systemDeliveryFee = systemDeliveryFee.add(systemSetting.getSystemDeliveryFee());
		}
		// BigDecimal packageFee = mathematicalPackageFee(totalBuyNumber, new BigDecimal(0));


		//如果是自提,则加上系统统一提货地址
		if (deliveryMode.equals("自提")){
			orderForm.setAddress(null);
			orderForm.setExtractionAddress(systemSetting.getUnifiedExtractionAddress());
		}

		// totalPackageFee = totalPackageFee.add(packageFee);

		if (totalPackageFee.compareTo(orderForm.getPackageFee())!=0){
			return Result.fail(102, "参数错误", "计算到的包装费: "+totalPackageFee+" 元和输入的包装费: "+orderForm.getPackageFee()+" 元不一致");
		}

		totalLogisticsFee = totalLogisticsFee.add(systemDeliveryFee);

		totalPrice = totalPrice.add(productFee);

		totalPrice = totalPrice.add(totalLogisticsFee);

		totalPrice = totalPrice.add(totalPackageFee);

		// Coupon coupon = null;

		BigDecimal discountPrice = new BigDecimal(0);
		//是否使用优惠券
		// String useDiscountId = orderForm.getUseDiscountId();
		// if (StringUtils.isNotBlank(useDiscountId)){
		// 	coupon = couponRepository.findByIdAndUserIdAndOrderFormPrice(useDiscountId, user.getId(), productFee);
		//
		// 	if (coupon!=null){
		// 		CouponRelation couponRelation = couponRelationRepository.findByCouponIdAndUserId(useDiscountId, user.getId());
		// 		if (couponRelation!=null){
		// 			if (!couponRelation.isAlreadyExpire()) {
		// 				if (coupon.getValidDay() > 0) {
		// 					Date nowTime = new Date();
		// 					if (nowTime == MoonUtil.contrastTime(MoonUtil.yMdHmsToAfterTime(couponRelation.getExpireTime(), coupon.getValidDay(), "天"), nowTime)) {
		// 						couponRelation.setAlreadyExpire(true);
		// 						couponRelation.setStatus("已过期");
		// 						couponRelation = couponRelationRepository.save(couponRelation);
		// 						return Result.fail(102, "新增订单失败", "优惠券已过期");
		// 					}
		// 				}
		// 			}
		// 			if (couponRelation.isAlreadyUse()){
		// 				return Result.fail(102, "新增订单失败", "优惠券已被使用");
		// 			}
		// 		}
		// 	}
		// }

		//如果使用优惠券
		// if (coupon!=null){
		// 	if (coupon.getType().equals("满减券")){
		// 		// totalPrice = totalPrice.subtract(MoonUtil.mathematical(productFee,"-",coupon.getDeductiblePrice(),2));
		// 		discountPrice = coupon.getDeductiblePrice();
		// 		totalPrice = totalPrice.subtract(productFee);
		// 		BigDecimal discountProductFee = productFee.subtract(discountPrice);
		// 		totalPrice = totalPrice.add(discountProductFee);
		// 	}
		// 	if (coupon.getType().equals("折扣券")){
		// 		discountPrice = MoonUtil.mathematical(productFee, "*", coupon.getDiscount(), 2);
		// 		totalPrice = totalPrice.subtract(productFee.subtract(discountPrice));
		// 		// totalPrice = totalPrice.subtract(discountPrice);
		// 	}
		// }


		if (totalPrice.compareTo(orderFormFee)!=0){
			return Result.fail(102,"参数错误","系统计算到的价格: "+totalPrice.toString()+"元,和输入的"+orderFormFee+"元不一致:");
		}

		orderForm.setDiscountPrice(discountPrice);

		orderForm.setLogisticsFee(totalLogisticsFee);

		orderForm.setProductFee(productFee);

		orderForm.setPackageFee(totalPackageFee);

		orderFormDetail = orderFormDetail.substring(0,orderFormDetail.length()-1);
		orderFormDetail = orderFormDetail+"[运费: "+orderForm.getLogisticsFee()+" 元,包装费: "+orderForm.getPackageFee()+"元]";
		orderForm.setOrderFormDetail(orderFormDetail);

		//调用微信支付
		switch (orderForm.getPaymentMode()){
			case "微信支付":{
				return weChatPayService.weChatPay(orderForm);
			}
			case "钱包支付":{
				// return walletService.walletPay(orderForm);
			}
			default:{
				return Result.fail(102,"参数错误","未知的支付方式");
			}
		}
	}

    @Override
    public JSONObject deleteOrderFormById(String id){

		logger.info("删除订单: " + id);

		if (StringUtils.contains(id,(","))&&id.length()>32){

			String[] split = StringUtils.split(id,",");

			List<String> ids = new ArrayList<>();

    		for (String s :split) {
				OrderForm orderForm = orderFormRepository.findById(s).orElse(null);
				if (orderForm!=null) {
					if (!orderForm.getStatus().equals("已取消")){
						return Result.success(102,"参数错误","删除订单失败,只能删除已取消的订单");
					}
					ids.add(s);
				}
			}

			int orderFormRow = orderFormRepository.deleteByIds(ids);

			return Result.success(200,"删除订单成功","批量删除订单成功,共删除订单: "+orderFormRow+" 个");
    	}

		OrderForm orderForm = orderFormRepository.findById(id).orElse(null);

		if (orderForm == null) {
    		return Result.fail(102,"查询失败","订单对象不存在");
		}

		if (!orderForm.getStatus().equals("已取消")){
			return Result.success(102,"参数错误","删除订单失败,只能删除已取消的订单");
		}

		//单个删除
        orderFormRepository.delete(orderForm);

		return Result.success(200,"删除成功","删除订单成功");
	}

    @Override
    public JSONObject updateOrderFormById(String id,OrderForm newOrderForm){
		logger.info("更新订单: " + id);

		OrderForm oldOrderForm = orderFormRepository.findById(id).orElse(null);
		if (oldOrderForm==null){
			return Result.fail(102,"查询失败","订单对象不存在");
		}

		//设置不更新字段,默认空值会被源对象替换
		String ignoreProperties = "";

		//开始合并对象
		JavaBeanUtil.copyProperties(oldOrderForm,newOrderForm,ignoreProperties);

    	newOrderForm = orderFormRepository.save(newOrderForm);

    	JSONObject orderFormBean = (JSONObject)JSON.toJSON(newOrderForm);

    	return Result.success(200,"更新成功",orderFormBean);
    }

	@Override
    public JSONObject findOrderFormById(String id){
    	logger.info("获取订单: " + id);

		OrderForm orderForm = orderFormRepository.findById(id).orElse(null);

		if(orderForm == null){
    		return Result.fail(102,"查询失败","订单对象不存在");
    	}

    	JSONObject orderFormBean = (JSONObject)JSON.toJSON(orderForm);

		return Result.success(200,"查询成功",orderFormBean);
    }

	@Override
	public JSONObject findOrderFormByList(String keyword,String userId,String shopId,String status,String orderFormType,String deliveryMode,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){

		logger.info("根据条件获取订单列表: " + keyword);

		//如果当前页数是空,则默认第一页
		if (page==null) {
			page = 1;
		}
		//如果需要查询条数为空,则默认查询10条
		if (rows==null){
			rows=10;
		}

		Page orderFormListPage = findOrderFormList(keyword,userId,shopId,status,orderFormType,deliveryMode,orderBy,beginTime,endTime,page,rows);

		if(orderFormListPage==null){
			return Result.fail(102,"参数有误","获取不到相关数据");
		}

		List <OrderForm> orderForms = (List<OrderForm>)orderFormListPage.getContent();

		JSONArray orderFormList = new JSONArray();
		for (OrderForm orderForm :orderForms) {
			if (orderForm.getOrderFormType().equals("团购")&&orderForm.getStatus().equals("待付款")){
				orderFormRepository.delete(orderForm);
				continue;
			}
			JSONObject orderFormBean = (JSONObject) JSON.toJSON(orderForm);
			JSONArray productDataArray = JSONArray.parseArray(orderForm.getProductData());
			orderFormBean.put("productData",productDataArray);
			orderFormList.add(orderFormBean);
		}


		JSONObject result = new JSONObject();
		result.put("rowsTotal",orderFormListPage.getTotalElements());
		result.put("page",orderFormListPage.getNumber()+1);
		result.put("rows",orderFormListPage.getSize());
		result.put("orderFormList",orderFormList);
		return Result.success(200,"查询成功",result);
	}

	private Page findOrderFormList(String keyword,String userId,String shopId,String status,String orderFormType,String deliveryMode,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows){
		//分页插件
		PageableUtil pageableUtil = new PageableUtil(page,rows,orderBy);
		Pageable pageable = pageableUtil.getPageable();
		Page orderFormListPage = orderFormRepository.findAll(new Specification<OrderForm>() {
			@Override
			public Predicate toPredicate(Root<OrderForm> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicateList = new ArrayList<>();
				//指定查询对象
				if (StringUtils.isNotBlank(keyword)) {
					predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("thirdPartyOrderNumber"), "%" + keyword + "%")
							, criteriaBuilder.like(root.get("consignee"), "%" + keyword + "%")
							, criteriaBuilder.like(root.get("contactMobile"), "%" + keyword + "%")
							, criteriaBuilder.like(root.get("remark"), "%" + keyword + "%")
							, criteriaBuilder.like(root.get("orderFormDetail"), "%" + keyword + "%")
							, criteriaBuilder.like(root.get("id"), "%" + keyword + "%")));
				}

				if(StringUtils.isNotBlank(userId)){
					predicateList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("userId"), userId)));
				}

				if(StringUtils.isNotBlank(status)){
					predicateList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), status)));
				}

				if(StringUtils.isNotBlank(orderFormType)){
					predicateList.add(criteriaBuilder.and(criteriaBuilder.equal(root.get("orderFormType"), orderFormType)));
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

		if (!orderFormListPage.hasContent()){
			return null;
		}

		return orderFormListPage;
	}

	@Override
	public void exportExcel(String keyword,String userId,String shopId,String status,String orderFormType,String deliveryMode,String orderBy,Date beginTime,Date endTime,HttpServletResponse response) {

		//从数据库获取需要导出的数据
		Page orderFormListPage = findOrderFormList(keyword,userId,shopId,status,orderFormType,deliveryMode,orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);

		List<OrderForm> orderFormList = new ArrayList<>();

		if (orderFormListPage!=null){
			orderFormList.addAll(orderFormListPage.getContent());
		}

		if (orderFormList!=null&&!orderFormList.isEmpty()) {
			//导出操作
			ExcelUtil.exportExcel(orderFormList, "订单列表", "订单列表",OrderForm.class, "订单列表.xls", response);
		}else {
			try {
				response.getWriter().write(Result.fail(102,"导出Excel失败","查询不到相关数据").toJSONString());
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	@Override
	public JSONObject toPayOrderFormById(String id, String userId) {
		logger.info("支付订单: " + id);

		OrderForm orderForm = orderFormRepository.findById(id).orElse(null);

		if(orderForm == null){
			return Result.fail(102,"查询失败","订单对象不存在");
		}

		if (!orderForm.getUserId().equals(userId)){
			return Result.fail(102,"查询失败","非法操作");
		}

		JSONObject result = weChatPayService.weChatPay(orderForm);

		if (result.getInteger("code")!=200){
			return Result.fail(102,"调用支付失败",result.getString("errorMsg"));
		}

		return Result.success(200,"等待支付",result);
	}

	/**
	 * 〈订单退款〉
	 *
	 * @param orderFormId,howMuch
	 * @return:
	 * @since: 1.0.0
	 * @Author: Revisit-Moon
	 * @Date: 2019/4/14 7:11 PM
	 */
	@Override
	public JSONObject refundMoney(String orderFormId, BigDecimal howMuch) {
		logger.info("订单退款: "+orderFormId);
		logger.info("退款金额: "+howMuch);

		OrderForm orderForm = orderFormRepository.findById(orderFormId).orElse(null);

		if (orderForm ==null){
			return Result.fail(102,"退款失败","订单不存在");
		}

		// if (!orderForm.getOrderFormType().equals("商品")){
		//     return Result.fail(102,"退款失败","只有商品订单可以退款");
		// }

		if (orderForm.getStatus().equals("待付款")){
			return Result.fail(102,"退款失败","订单当前未支付");
		}

		if (orderForm.getStatus().equals("已完结")){
			return Result.fail(102,"退款失败","订单已完结");
		}

		if (howMuch==null){
			howMuch = orderForm.getOrderFormFee();
		}

		if (StringUtils.isBlank(orderForm.getThirdRefundOrderNumber())){
			orderForm.setThirdRefundOrderNumber(MoonUtil.createWeChatOrderNum());
			orderForm = orderFormRepository.save(orderForm);
		}
		//根据不同订单调用不同接口
		return weChatPayService.weChatPayRefundMoney(orderForm, howMuch);
	}

	/**
	 * 〈订单发货〉
	 *
	 * @param orderFormId,logisticsCompany,logisticsNumber
	 * @return:
	 * @since: 1.0.0
	 * @Author: Revisit-Moon
	 * @Date: 2019/4/17 3:10 PM
	 */
	@Override
	public JSONObject orderFromDelivery(String orderFormId, String logisticsCompany, String logisticsNumber) {
		logger.info("订单发货");

		OrderForm orderForm = orderFormRepository.findById(orderFormId).orElse(null);

		if (orderForm ==null){
			return Result.fail(102,"发货失败","订单不存在");
		}

		if (!orderForm.getDeliveryMode().equals("配送")){
			return Result.fail(102,"发货失败","不是配送订单");
		}

		if (!orderForm.getStatus().equals("待发货")){
			return Result.fail(102,"发货失败","订单当前未支付");
		}


		orderForm.setLogisticsCompany(logisticsCompany);
		orderForm.setLogisticsNumber(logisticsNumber);
		orderForm.setLogisticsTime(new Date());
		orderForm.setStatus("待收货");
		orderForm = orderFormRepository.save(orderForm);
		if (StringUtils.isBlank(orderForm.getId())){
			return Result.fail(102,"发货失败","请联系管理员");
		}
		return Result.success(200,"发货成功",orderForm);
	}

	/**
	 * 〈确认收货〉
	 *
	 * @param orderFormId,logisticsCompany,logisticsNumber
	 * @return:
	 * @since: 1.0.0
	 * @Author: Revisit-Moon
	 * @Date: 2019/4/17 3:10 PM
	 */
	@Override
	public JSONObject orderFromDone(String userId,String orderFormId) {
		logger.info("确认收货");

		OrderForm orderForm = orderFormRepository.findById(orderFormId).orElse(null);

		if (orderForm ==null){
			return Result.fail(102,"收货失败","订单不存在");
		}

		if (!orderForm.getStatus().equals("待收货")){
			return Result.fail(102,"收货失败","订单当前未发货");
		}

		if (!orderForm.getUserId().equals(userId)){
			return Result.fail(102,"收货失败","非法操作");
		}
		orderForm.setStatus("完结");
		orderForm = orderFormRepository.save(orderForm);
		if (StringUtils.isBlank(orderForm.getId())){
			return Result.fail(102,"收货失败","请联系管理员");
		}
		return Result.success(200,"收货成功",orderForm);
	}

	/**
	 * 〈取消订单〉
	 *
	 * @param orderFormId,logisticsCompany,logisticsNumber
	 * @return:
	 * @since: 1.0.0
	 * @Author: Revisit-Moon
	 * @Date: 2019/4/17 3:10 PM
	 */
	@Override
	public JSONObject orderFromCancel(String userId,String orderFormId) {
		logger.info("取消订单");

		OrderForm orderForm = orderFormRepository.findById(orderFormId).orElse(null);

		if (orderForm ==null){
			return Result.fail(102,"取消订单失败","订单不存在");
		}

		if (!orderForm.getStatus().equals("待付款")){
			return Result.fail(102,"取消订单失败","当前订单不可取消");
		}

		if (!orderForm.getUserId().equals(userId)){
			return Result.fail(102,"取消订单失败","非法操作");
		}
		orderForm.setStatus("已取消");
		orderForm = orderFormRepository.save(orderForm);
		if (StringUtils.isBlank(orderForm.getId())){
			return Result.fail(102,"取消订单失败","请联系管理员");
		}
		return Result.success(200,"取消订单成功",orderForm);
	}
}