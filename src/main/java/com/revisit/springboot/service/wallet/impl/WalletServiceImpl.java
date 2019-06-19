// package com.revisit.springboot.service.wallet.impl;
//
// import com.alibaba.fastjson.JSON;
// import com.alibaba.fastjson.JSONArray;
// import com.alibaba.fastjson.JSONObject;
// import com.revisit.springboot.component.uuid.CustomizeUUIDGenerate;
// import com.revisit.springboot.entity.orderform.OrderForm;
// import com.revisit.springboot.entity.productsku.ProductSku;
// import com.revisit.springboot.entity.system.SystemSetting;
// import com.revisit.springboot.entity.user.Role;
// import com.revisit.springboot.entity.user.User;
// import com.revisit.springboot.entity.wallet.Wallet;
// import com.revisit.springboot.repository.orderform.OrderFormRepository;
// import com.revisit.springboot.repository.productsku.ProductSkuRepository;
// import com.revisit.springboot.repository.system.SystemSettingRepository;
// import com.revisit.springboot.repository.user.RoleRepository;
// import com.revisit.springboot.repository.user.UserRepository;
// import com.revisit.springboot.repository.wallet.WalletRepository;
// import com.revisit.springboot.service.orderform.OrderFormService;
// import com.revisit.springboot.service.productsku.ProductSkuService;
// import com.revisit.springboot.service.user.UserService;
// import com.revisit.springboot.service.wallet.WalletService;
// import com.revisit.springboot.utils.*;
// import org.apache.commons.codec.digest.DigestUtils;
// import org.apache.commons.lang3.StringUtils;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification;
// import org.springframework.stereotype.Service;
//
// import javax.persistence.criteria.CriteriaBuilder;
// import javax.persistence.criteria.CriteriaQuery;
// import javax.persistence.criteria.Predicate;
// import javax.persistence.criteria.Root;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.math.BigDecimal;
// import java.util.*;
//
//
// /**
//  * Wallet逻辑层接口类
//  * @author Revisit-Moon
//  * @date 2019-04-20 18:57:43
//  */
// @Service
// public class WalletServiceImpl implements WalletService {
//
// 	@Autowired
// 	private WalletRepository walletRepository;
//
// 	// @Autowired
//     // private RebateRepository rebateRepository;
//
// 	// @Autowired
// 	// private AssembleRelationRepository assembleRelationRepository;
//
// 	// @Autowired
// 	// private AssembleProductRepository assembleProductRepository;
//
// 	// @Autowired
// 	// private AssembleProductSkuRepository assembleProductSkuRepository;
//
// 	@Autowired
// 	private UserService userService;
//
// 	// @Autowired
// 	// private TeamRepository teamRepository;
//
// 	// @Autowired
// 	// private TeamUserRelationRepository teamUserRelationRepository;
//
// 	@Autowired
// 	private UserRepository userRepository;
//
// 	@Autowired
// 	private RoleRepository roleRepository;
//
// 	// @Autowired
// 	// private CouponRelationRepository couponRelationRepository;
//
// 	@Autowired
// 	private OrderFormRepository orderFormRepository;
//
// 	// @Autowired
// 	// private OrderFormRecordingService orderFormRecordingService;
//
// 	@Autowired
// 	private ProductSkuService productSkuService;
//
// 	@Autowired
// 	private ProductSkuRepository productSkuRepository;
//
// 	// @Autowired
// 	// private RechargeItemRepository rechargeItemRepository;
//
// 	@Autowired
// 	private OrderFormService orderFormService;
//
// 	@Autowired
// 	private SystemSettingRepository systemSettingRepository;
//
// 	@Autowired
// 	private HttpServletRequest request;
//
// 	private final static Logger logger = LoggerFactory.getLogger(WalletServiceImpl.class);
//
// 	@Override
// 	public JSONObject addWallet(Wallet wallet) {
// 		logger.info("新增Wallet");
//
//
// 		//保存此对象
// 		wallet = walletRepository.save(wallet);
//
// 		if (StringUtils.isBlank(wallet.getId())) {
// 			return Result.fail(110, "系统错误", "新增Wallet失败,请联系管理员");
// 		}
//
// 		JSONObject walletBean = (JSONObject) JSON.toJSON(wallet);
// 		return Result.success(200, "新增Wallet成功", walletBean);
// 	}
//
// 	@Override
// 	public JSONObject deleteWalletById(String id) {
//
// 		logger.info("删除Wallet: " + id);
//
// 		List<String> ids = new ArrayList<>();
// 		if (StringUtils.contains(id, (","))) {
// 			String[] split = StringUtils.split(id, ",");
// 			for (String s : split) {
// 				Wallet wallet = walletRepository.findById(s).orElse(null);
// 				if (wallet != null) {
// 					ids.add(s);
// 				}
// 			}
// 		} else {
// 			Wallet wallet = walletRepository.findById(id).orElse(null);
// 			if (wallet == null) {
// 				return Result.fail(102, "查询失败", "Wallet对象不存在");
// 			}
// 			ids.add(id);
// 		}
//
// 		if (ids == null || ids.isEmpty()) {
// 			return Result.fail(102, "查询失败", "Wallet对象不存在");
// 		}
//
// 		int walletRows = walletRepository.deleteByIds(ids);
//
// 		return Result.success(200, "删除Wallet成功", "批量删除Wallet成功,共删除Wallet: " + walletRows + " 个");
// 	}
//
// 	@Override
// 	public JSONObject updateWalletById(String id, Wallet newWallet) {
// 		logger.info("更新Wallet: " + id);
//
// 		Wallet oldWallet = walletRepository.findById(id).orElse(null);
// 		if (oldWallet == null) {
// 			return Result.fail(102, "查询失败", "Wallet对象不存在");
// 		}
//
// 		//设置不更新字段,默认空值会被源对象替换
// 		String ignoreProperties = "";
//
// 		//开始合并对象
// 		JavaBeanUtil.copyProperties(oldWallet, newWallet, ignoreProperties);
//
// 		newWallet = walletRepository.save(newWallet);
//
// 		JSONObject walletBean = (JSONObject) JSON.toJSON(newWallet);
//
// 		return Result.success(200, "更新成功", walletBean);
// 	}
//
// 	@Override
// 	public JSONObject findWalletByUserId(String userId) {
// 		logger.info("获取Wallet: " + userId);
//
// 		Wallet wallet = walletRepository.findByUserId(userId);
//
// 		if (wallet == null) {
// 			wallet = new Wallet();
// 			wallet.setUserId(userId);
// 			walletRepository.save(wallet);
// 			// return Result.fail(102,"查询失败","Wallet对象不存在");
// 		}
//
// 		JSONObject walletBean = (JSONObject) JSON.toJSON(wallet);
// 		User user = userRepository.findById(userId).orElse(null);
// 		// Role role = userService.getRole(user);
// 		//团队一年总消费
// 		// Team userTeam= teamRepository.findByUserId(userId);
// 		// BigDecimal oneYearBuyTotalPrice = new BigDecimal(0);
// 		// if (userTeam!=null){
// 		// 	oneYearBuyTotalPrice = userTeam.getNowYearPrice();
// 		// }
//
// 		// List<Role> nextLevelRoleList = roleRepository.findByNextLevel(role.getLevel());
// 		// walletBean.put("nowLevelPrice", oneYearBuyTotalPrice);
// 		// walletBean.put("nowLevelName", role.getRoleName());
//
// 		BigDecimal upLevelPrice = new BigDecimal(0);
//
// 		// for (Role nextLevelRole :nextLevelRoleList) {
// 		// 	if (role.getUpLevelPrice().compareTo(nextLevelRole.getUpLevelPrice())<0){
// 		// 		upLevelPrice = nextLevelRole.getUpLevelPrice();
// 		// 		walletBean.put("upLevelName", nextLevelRole.getRoleName());
// 		// 		break;
// 		// 	}
// 		// }
// 		// if (upLevelPrice.compareTo(new BigDecimal(0))==0){
// 			// walletBean.put("upLevelPrice", oneYearBuyTotalPrice);
// 			// walletBean.put("levelPercentage", 100);
// 		// }else {
// 		// 	walletBean.put("upLevelPrice", upLevelPrice);
// 		// 	BigDecimal levelPercentage = new BigDecimal(0);
// 		// 	if (oneYearBuyTotalPrice.compareTo(new BigDecimal(0))>0){
// 		// 		BigDecimal difference = MoonUtil.mathematical(oneYearBuyTotalPrice, "/", upLevelPrice, 4);
// 		// 		levelPercentage = MoonUtil.mathematical(difference, "*", 100, 2);
// 		// 	}
// 		// 	walletBean.put("levelPercentage", levelPercentage);
// 		// }
// 		return Result.success(200, "查询成功", walletBean);
// 	}
//
// 	@Override
// 	public Wallet findByUserId(String userId) {
// 		return walletRepository.findByUserId(userId);
// 	}
//
// 	@Override
// 	public JSONObject findWalletByList(String keyword, String orderBy, Date beginTime, Date endTime, Integer page, Integer rows) {
//
// 		logger.info("根据条件获取Wallet列表: " + keyword);
//
// 		//如果当前页数是空,则默认第一页
// 		if (page == null) {
// 			page = 1;
// 		}
// 		//如果需要查询条数为空,则默认查询10条
// 		if (rows == null) {
// 			rows = 10;
// 		}
// 		Page walletListPage = findWalletList(keyword, orderBy, beginTime, endTime, page, rows);
//
// 		if (walletListPage == null) {
// 			return Result.fail(102, "参数有误", "获取不到相关数据");
// 		}
//
// 		JSONObject result = new JSONObject();
// 		result.put("rowsTotal", walletListPage.getTotalElements());
// 		result.put("page", walletListPage.getNumber() + 1);
// 		result.put("rows", walletListPage.getSize());
// 		result.put("walletList", walletListPage.getContent());
// 		return Result.success(200, "查询成功", result);
// 	}
//
// 	private Page findWalletList(String keyword, String orderBy, Date beginTime, Date endTime, Integer page, Integer rows) {
// 		//分页插件
// 		PageableUtil pageableUtil = new PageableUtil(page, rows, orderBy);
// 		Pageable pageable = pageableUtil.getPageable();
// 		Page walletListPage = walletRepository.findAll(new Specification<Wallet>() {
// 			@Override
// 			public Predicate toPredicate(Root<Wallet> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
// 				List<Predicate> predicateList = new ArrayList<>();
// 				//指定查询对象
// 				if (StringUtils.isNotBlank(keyword)) {
// 					predicateList.add(criteriaBuilder.or(criteriaBuilder.like(root.get("id"), "%" + keyword + "%")
// 							, criteriaBuilder.like(root.get("id"), "%" + keyword + "%")
// 							, criteriaBuilder.like(root.get("id"), "%" + keyword + "%")));
// 				}
//
// 				if (beginTime != null) {
// 					predicateList.add(criteriaBuilder.and(criteriaBuilder.greaterThan(root.get("createTime"), beginTime)));
// 				}
//
// 				if (endTime != null) {
// 					predicateList.add(criteriaBuilder.and(criteriaBuilder.lessThan(root.get("createTime"), endTime)));
// 				}
// 				return query.where(predicateList.toArray(new Predicate[predicateList.size()])).getRestriction();
// 			}
// 		}, pageable);
//
// 		if (!walletListPage.hasContent()) {
// 			return null;
// 		}
//
// 		return walletListPage;
// 	}
//
// 	@Override
// 	public void exportExcel(String keyword, String orderBy, Date beginTime, Date endTime, HttpServletResponse response) {
//
// 		//从数据库获取需要导出的数据
// 		Page walletListPage = findWalletList(keyword, orderBy, beginTime, endTime, 1, Integer.MAX_VALUE);
//
// 		List<Wallet> walletList = new ArrayList<>();
//
// 		if (walletListPage != null) {
// 			walletList.addAll(walletListPage.getContent());
// 		}
//
// 		if (walletList != null && !walletList.isEmpty()) {
// 			//导出操作
// 			ExcelUtil.exportExcel(walletList, "WalletList列表", "WalletList列表", Wallet.class, "WalletList列表.xls", response);
// 		} else {
// 			try {
// 				response.getWriter().write(Result.fail(102, "导出Excel失败", "查询不到相关数据").toJSONString());
// 			} catch (Exception e) {
// 				e.printStackTrace();
// 			}
// 		}
// 	}
//
// 	/**
// 	 * 〈钱包支付〉
// 	 *
// 	 * @param orderForm
// 	 * @return:
// 	 * @since: 1.0.0
// 	 * @Author: Revisit-Moon
// 	 * @Date: 2019/5/7 1:01 AM
// 	 */
// 	@Override
// 	public JSONObject walletPay(OrderForm orderForm) {
//
// 		if (!orderForm.getStatus().equals("待付款")){
// 			logger.info("已处理的订单");
// 			return Result.success(200,"新增订单成功",orderForm.getId());
// 		}
// 		Date payTime = orderForm.getPayTime();
// 		if (payTime==null){
// 			orderForm.setPayTime(MoonUtil.getNowTimeSecondPrecision());
// 		}
//
// 		Wallet wallet = walletRepository.findByUserId(orderForm.getUserId());
//
// 		BigDecimal orderFormFee = orderForm.getOrderFormFee();
// 		// if (orderFormFee.compareTo(wallet.getCanWithdrawPrice().add(wallet.getNoWithdrawPrice()))>0){
// 		// 	return Result.fail(102,"钱包支付失败","您的余额不足");
// 		// }
//
// 		// BigDecimal noWithdrawPrice = wallet.getNoWithdrawPrice();
// 		// BigDecimal canWithdrawPrice = wallet.getCanWithdrawPrice();
//
// 		// if (noWithdrawPrice.compareTo(orderFormFee)>=0){
// 		// 	wallet.setNoWithdrawPrice(MoonUtil.mathematical(wallet.getNoWithdrawPrice(), "-", orderFormFee, 2));
// 		// }
//
// 		// if (noWithdrawPrice.compareTo(orderFormFee)<0){
// 		// 	BigDecimal lastOrderFee = MoonUtil.mathematical(orderFormFee, "-", wallet.getNoWithdrawPrice(), 2);
// 		// 	if (!MoonUtil.isZero(lastOrderFee)){
// 		// 		if (canWithdrawPrice.compareTo(lastOrderFee)<0){
// 		// 			return Result.fail(102,"钱包支付失败","您的余额不足");
// 		// 		}
// 		// 		wallet.setNoWithdrawPrice(new BigDecimal(0));
// 		// 		wallet.setCanWithdrawPrice(MoonUtil.mathematical(wallet.getCanWithdrawPrice(), "-", lastOrderFee, 2));
// 		// 	}
// 		// }
//
// 		orderForm.setSubmitOrderFormIp(MoonUtil.getRealIpAddress(request));
// 		String thirdPartyOrderNumber = orderForm.getThirdPartyOrderNumber();
// 		if (StringUtils.isBlank(thirdPartyOrderNumber)) {
// 			//生成第三方订单号
// 			orderForm.setThirdPartyOrderNumber(MoonUtil.createWeChatOrderNum());
// 		}
//
// 		// JSONObject orderFormConsumeRecordingResult = orderFormRecordingService.addOrderFormConsumeRecording(orderForm);
// 		// if (orderFormConsumeRecordingResult.getInteger("code")!=200){
// 		// 	return Result.fail(110,"系统错误","保存消费记录出错");
// 		// }
// 		// 转换商品数据为JSON对象
// 		// JSONArray productArray = JSONArray.parseArray(orderForm.getProductData());
// 		// if (productArray == null || productArray.isEmpty()) {
// 		// 	return Result.fail(102,"参数错误","订单数据为空");
// 		// }
//
// 		if (orderForm.getOrderFormType().equals("商品")) {
// 			// orderForm.setRewardStatus("等待返佣");
// 			User user = userRepository.findById(orderForm.getUserId()).orElse(null);
//
// 			String referrerId = user.getReferrerId();
// 			//如果有推荐人
// 			if (StringUtils.isNotBlank(referrerId)) {
// 				User referrerUser = userRepository.findByReferrerCode(referrerId);
// 				logger.info("调用推荐人消费奖励模块");
// 				JSONObject resultBean = buyReward(user, orderForm, referrerUser);
// 				if (resultBean.getInteger("code") != 200) {
// 					return resultBean;
// 				}
// 			}
//
// 			//是否使用优惠券
// 			// String useDiscountId = orderForm.getUseDiscountId();
// 			// if (StringUtils.isNotBlank(useDiscountId)) {
// 			// 	CouponRelation couponRelation = couponRelationRepository.findByCouponIdAndUserId(useDiscountId, orderForm.getUserId());
// 			// 	if (couponRelation != null) {
// 			// 		couponRelation.setAlreadyUse(true);
// 			// 		couponRelation.setStatus("已使用");
// 			// 		couponRelationRepository.save(couponRelation);
// 			// 	}
// 			// }
//
// 			//定义待更新的商品sku数组
// 			Map<String, Object> skuIdAndStockMap = new HashMap<>();
// 			//获取订单数据内容
// 			// for (int i = 0; i < productArray.size(); i++) {
// 			// 	JSONObject productData = null;
// 			// 	try {
// 			// 		productData = productArray.getJSONObject(i);
// 			// 		String skuId = productData.getString("skuId");
// 			// 		int buyNumber = productData.getInteger("buyNumber");
// 			// 		skuIdAndStockMap.put(skuId, buyNumber);
// 			// 	} catch (Exception e) {
// 			// 		return Result.fail(110, "参数错误", "转换商品数据出错");
// 			// 	}
// 			// }
// 			if (skuIdAndStockMap.isEmpty()) {
// 				return Result.fail(102,"参数错误","商品数据为空");
// 			}
// 			if (productSkuService.deductStock(skuIdAndStockMap)) {
// 				if (orderForm.getDeliveryMode().equals("自提")) {
// 					orderForm.setStatus("待提货");
// 					// String sign = orderForm.getId() + orderForm.getOpenId() + orderForm.getConsignee() + orderForm.getStatus() + MoonUtil.dataToyMdHmsNotSymbol(orderForm.getPayTime());
// 					try {
// 						// orderForm.setExtractionSign(DigestUtils.md5Hex(sign.getBytes("UTF-8")).toUpperCase());
// 					} catch (Exception e) {
// 						logger.info("加密错误,请手动加密");
// 						e.printStackTrace();
// 					}
// 				}
// 				if (orderForm.getDeliveryMode().equals("配送")) {
// 					orderForm.setStatus("待发货");
// 				}
// 				orderForm.setPayTime(MoonUtil.getNowTimeSecondPrecision());
// 				orderForm = orderFormRepository.saveAndFlush(orderForm);
//
// 				if (StringUtils.isBlank(orderForm.getId())) {
// 					return Result.fail(110, "系统错误", "更新订单失败,请联系管理员");
// 				}
// 				wallet = walletRepository.save(wallet);
// 				if (StringUtils.isBlank(wallet.getId())) {
// 					return Result.fail(110, "系统错误", "更新钱包失败,请联系管理员");
// 				}
// 			}else{
// 				return Result.fail(110,"系统错误","扣除商品库存失败");
// 			}
// 		}
// 		if (orderForm.getOrderFormType().equals("拼团")){
// 			JSONObject assemblePayBean = assembleProductOrderFormCallBackHandler(orderForm);
// 			if (assemblePayBean.getInteger("code")!=200){
// 				return Result.success(200, "钱包支付失败", assemblePayBean.getString("errorMsg"));
// 			}
// 			wallet = walletRepository.save(wallet);
// 			if (StringUtils.isBlank(wallet.getId())) {
// 				return Result.fail(110, "系统错误", "更新钱包失败,请联系管理员");
// 			}
// 		}
// 		return Result.success(200, "钱包支付成功", orderForm);
// 	}
//
// 	/**
// 	 * 〈拼团订单处理模块〉
// 	 *
// 	 * @param oldOrderForm
// 	 * @return:
// 	 * @since: 1.0.0
// 	 * @Author: Revisit-Moon
// 	 * @Date: 2019/4/20 6:11 PM
// 	 */
// 	private JSONObject assembleProductOrderFormCallBackHandler(OrderForm oldOrderForm){
//
// 		oldOrderForm.setStatus("已支付,待成团");
//
// 		oldOrderForm = orderFormRepository.saveAndFlush(oldOrderForm);
// 		JSONObject orderFormConsumeRecordingResult = orderFormRecordingService.addOrderFormConsumeRecording(oldOrderForm);
// 		if (orderFormConsumeRecordingResult.getInteger("code")!=200){
// 			return Result.fail(102,"新增订单记录失败",orderFormConsumeRecordingResult.getString("errorMsg"));
// 		}
// 		//转换商品数据为JSON对象
// 		JSONArray productArray = JSONArray.parseArray(oldOrderForm.getProductData());
// 		if (productArray == null || productArray.isEmpty()) {
// 			return Result.fail(102,"参数错误","商品数据为空");
// 		}
// 		User user = userRepository.findById(oldOrderForm.getUserId()).orElse(null);
// 		AssembleProductSku assembleProductSku = null;
// 		AssembleProduct assembleProduct = null;
// 		//获取订单数据内容
// 		for (int i = 0; i < productArray.size(); i++) {
// 			JSONObject productData = null;
// 			try {
// 				productData = productArray.getJSONObject(i);
// 				String assembleProductSkuId = productData.getString("assembleProductSkuId");
//
// 				if (assembleProductSku==null) {
// 					assembleProductSku = assembleProductSkuRepository.findById(assembleProductSkuId).orElse(null);
// 				}
//
// 				if (assembleProductSku==null){
// 					return Result.fail(102,"参数错误","拼团商品sku为空");
// 				}
//
// 				if (assembleProduct==null) {
// 					assembleProduct = assembleProductRepository.findById(assembleProductSku.getAssembleProductId()).orElse(null);
// 				}
//
// 				if (assembleProduct==null){
// 					return Result.fail(102,"参数错误","拼团商品为空");
// 				}
//
// 			} catch (Exception e) {
// 				return Result.fail(110,"系统错误","转换拼团商品出错");
// 			}
// 		}
//
// 		//搜索该拼团商品的团队Id
// 		// List<AssembleRelation> assembleRelationList = assembleRelationRepository.findByTeamId(assembleProduct.getNowTeamId());
// 		//
// 		// if (assembleRelationList==null||assembleRelationList.size()<assembleProduct.getFullSize()){
// 		// 	if (assembleRelationList==null){
// 		// 		assembleRelationList = new ArrayList<>();
// 		// 	}
// 		// 	AssembleRelation assembleRelation = new AssembleRelation();
// 		// 	assembleRelation.setUserId(user.getId());
// 		// 	assembleRelation.setAssembleProductId(assembleProduct.getId());
// 		// 	assembleRelation.setAvatar(user.getWeChatAvatar());
// 		// 	assembleRelation.setOrderFormId(oldOrderForm.getId());
// 		// 	assembleRelation.setRealName(user.getRealName());
// 		// 	assembleRelation.setWeChatName(user.getWeChatName());
// 		// 	assembleRelation.setTeamId(assembleProduct.getNowTeamId());
// 		// 	assembleRelation.setEndTime(assembleProduct.getEndTime());
// 		// 	assembleRelationList.add(assembleRelation);
// 		// }
//
// 		if (assembleRelationList.size()==assembleProduct.getFullSize()){
// 			for (AssembleRelation assembleRelationBean :assembleRelationList) {
// 				//修改状态
// 				assembleRelationBean.setStatus("已成团");
// 				//修改订单状态
// 				OrderForm doneOrderForm = orderFormRepository.findById(assembleRelationBean.getOrderFormId()).orElse(null);
// 				if (doneOrderForm!=null) {
// 					if (doneOrderForm.getDeliveryMode().equals("自提")) {
// 						doneOrderForm.setStatus("待提货");
// 						String sign = doneOrderForm.getId() + doneOrderForm.getOpenId() + doneOrderForm.getConsignee() + doneOrderForm.getStatus() + MoonUtil.dataToyMdHmsNotSymbol(doneOrderForm.getPayTime());
// 						try {
// 							doneOrderForm.setExtractionSign(DigestUtils.md5Hex(sign.getBytes("UTF-8")).toUpperCase());
// 						} catch (Exception e) {
// 							logger.info("加密错误,请手动加密");
// 							e.printStackTrace();
// 						}
// 					}
// 					if (doneOrderForm.getDeliveryMode().equals("配送")) {
// 						doneOrderForm.setStatus("待发货");
// 					}
// 					doneOrderForm.setPayTime(MoonUtil.getNowTimeSecondPrecision());
// 					doneOrderForm = orderFormRepository.save(doneOrderForm);
// 					if (StringUtils.isBlank(doneOrderForm.getId())){
// 						return Result.fail(110,"系统错误","更新订单出错");
// 					}
// 				}
// 			}
// 			assembleProduct.setNowTeamId(CustomizeUUIDGenerate.generateBase64UUID(UUID.randomUUID().toString()));
// 			assembleProduct = assembleProductRepository.save(assembleProduct);
// 			if (StringUtils.isBlank(assembleProduct.getId())){
// 				return Result.fail(110,"系统错误","更新拼团商品出错");
// 			}
// 		}
//
// 		assembleRelationList = assembleRelationRepository.saveAll(assembleRelationList);
//
// 		if (assembleRelationList==null||assembleRelationList.isEmpty()){
// 			return Result.fail(110,"系统错误","更新拼团团队关系出错");
// 		}
//
// 		return Result.success(200,"钱包支付成功","拼团成功");
// 	}
//
// 	@Override
// 	public JSONObject referrerReward(User user, OrderForm orderForm, User referrerUser) {
// 		BigDecimal rechargePrice = orderForm.getOrderFormFee();
// 		BigDecimal rewardPrice = new BigDecimal(0);
//
// 		Role userRole = userService.getRole(user);
//
// 		Role referrerUserRole = userService.getRole(referrerUser);
//
// 		if (userRole == null || referrerUserRole == null) {
// 			return Result.fail(102, "处理失败", "处理失败,推荐人或被推荐人角色为空");
// 		}
// 		if (referrerUserRole.isDistributorship()) {
// 			logger.info("推荐人可参与分销");
// 			SystemSetting systemSetting = systemSettingRepository.findSystemSetting();
// 			if (userRole.getLevel() >= referrerUserRole.getLevel()) {
// 				if (!user.isAlreadyPayReward()) {
// 					Wallet referrerUserWallet = walletRepository.findByUserId(referrerUser.getId());
// 					BigDecimal referralRewardsPercentage = systemSetting.getReferralRewardsPercentage();
// 					referralRewardsPercentage = MoonUtil.mathematical(referralRewardsPercentage, "/", 100, 2);
// 					if (referralRewardsPercentage.compareTo(new BigDecimal(0)) > 0) {
// 						rewardPrice = MoonUtil.mathematical(rechargePrice, "*", referralRewardsPercentage, 2);
// 						referrerUserWallet.setCanWithdrawPrice(referrerUserWallet.getCanWithdrawPrice().add(rewardPrice));
// 						referrerUserWallet.setTotalIncome(referrerUserWallet.getTotalIncome().add(rewardPrice));
// 						referrerUserWallet = walletRepository.save(referrerUserWallet);
// 						if (StringUtils.isBlank(referrerUserWallet.getId())) {
// 							return Result.fail(102, "处理失败", "更新推荐人账户余额时出错");
// 						}
// 						user.setAlreadyPayReward(true);
// 						JSONObject orderFormConsumeRecordingResult = orderFormRecordingService.addOrderFormRewardRecording("一次性推荐奖励","已到账",rewardPrice,orderForm,referrerUser);
// 						if (orderFormConsumeRecordingResult.getInteger("code") != 200) {
// 							return Result.fail(102, "处理失败", orderFormConsumeRecordingResult.getString("errorMsg"));
// 						}
// 					}
// 				}
// 			}
//
// 			if (userRole.getLevel() < referrerUserRole.getLevel()) {
// 				Wallet referrerUserWallet = walletRepository.findByUserId(referrerUser.getId());
// 				BigDecimal referralRewardsPercentage = systemSetting.getReferralRewardsPercentage();
// 				referralRewardsPercentage = MoonUtil.mathematical(referralRewardsPercentage, "/", 100, 2);
// 				if (referralRewardsPercentage.compareTo(new BigDecimal(0)) > 0) {
// 					rewardPrice = MoonUtil.mathematical(rechargePrice, "*", referralRewardsPercentage, 2);
// 					referrerUserWallet.setCanWithdrawPrice(referrerUserWallet.getCanWithdrawPrice().add(rewardPrice));
//                     referrerUserWallet.setTotalIncome(referrerUserWallet.getTotalIncome().add(rewardPrice));
//                     referrerUserWallet = walletRepository.save(referrerUserWallet);
// 					if (StringUtils.isBlank(referrerUserWallet.getId())) {
// 						return Result.fail(102, "处理失败", "更新推荐人账户余额时出错");
// 					}
// 					user.setAlreadyPayReward(false);
// 					JSONObject orderFormConsumeRecordingResult = orderFormRecordingService.addOrderFormRewardRecording("充值奖励","已到账",rewardPrice,orderForm,referrerUser);
// 					if (orderFormConsumeRecordingResult.getInteger("code") != 200) {
// 						return Result.fail(102, "处理失败", orderFormConsumeRecordingResult.getString("errorMsg"));
// 					}
// 				}
// 			}
// 		}
// 		//调用会员升级模块
// 		userRole = userRechargeUpLevel(user,rechargePrice);
// 		if (userRole==null){
// 			return Result.fail(102, "处理失败", "调用会员升级模块失败");
// 		}
// 		user = userRepository.save(user);
// 		if (StringUtils.isBlank(user.getId())) {
// 			return Result.fail(102, "处理失败", "更新被推荐人时出错");
// 		}
//
// 		JSONObject orderFormConsumeRecordingResult = orderFormRecordingService.addOrderFormRechargeConsumeRecording(orderForm);
// 		if (orderFormConsumeRecordingResult.getInteger("code")!=200){
// 			return Result.fail(110,"系统错误","保存消费记录出错");
// 		}
//
// 		return Result.success(200, "处理成功", user);
// 	}
//
// 	@Override
// 	public JSONObject buyReward(User user, OrderForm orderForm, User referrerUser) {
//
// 		//调用消费奖励模块
// 		BigDecimal rewardFee = new BigDecimal(0);
//
// 		//获取用户团队
// 		Team userTeam = teamRepository.findByUserId(user.getId());
//
// 		//更新年业绩
// 		userTeam.setNowYearPrice(userTeam.getNowYearPrice().add(orderForm.getOrderFormFee()));
//
// 		//插入业绩记录
// 		JSONArray achievementsArray = JSONArray.parseArray(userTeam.getTotalAchievements());
// 		JSONObject achievementsBean = new JSONObject();
// 		achievementsBean.put("userId",user.getId());
// 		achievementsBean.put("userName",orderForm.getConsignee());
// 		achievementsBean.put("orderDetail",orderForm.getOrderFormDetail());
// 		achievementsBean.put("type","消费");
// 		achievementsBean.put("price",orderForm.getOrderFormFee());
// 		achievementsBean.put("payTime",orderForm.getPayTime());
// 		achievementsArray.add(achievementsBean);
// 		userTeam.setTotalAchievements(achievementsArray.toJSONString());
//
// 		//保存最新的团队数据
// 		userTeam = teamRepository.save(userTeam);
// 		if (StringUtils.isBlank(userTeam.getId())){
// 			return Result.fail(102, "处理失败", "更新用户团队失败");
// 		}
//
// 		//获取用户团队关系记录
// 		TeamUserRelation userTeamRelation = teamUserRelationRepository.findByUserId(user.getId());
//
// 		//更新个人业绩
// 		userTeamRelation.setSelfAchievements(userTeamRelation.getSelfAchievements().add(orderForm.getOrderFormFee()));
//
// 		//保存最新的用户团队关系记录
// 		userTeamRelation = teamUserRelationRepository.save(userTeamRelation);
// 		if (StringUtils.isBlank(userTeamRelation.getId())){
// 			return Result.fail(102, "处理失败", "更新用户团队关系失败");
// 		}
//
// 		//如果推荐用户不是空
// 		if (referrerUser!=null) {
// 			//获取推荐人用户团队
// 			Team referrerUserTeam = teamRepository.findByUserId(referrerUser.getId());
// 			//更新团队业绩记录
// 			referrerUserTeam.setTotalAchievements(achievementsArray.toJSONString());
// 			//更新团队业绩
// 			referrerUserTeam.setNowYearPrice(referrerUserTeam.getNowYearPrice().add(orderForm.getOrderFormFee()));
// 			//保存最新团队的数据
// 			referrerUserTeam = teamRepository.save(referrerUserTeam);
// 			//推荐人调用会员升级模块
// 			referrerUser = userConsumeUpLevel(referrerUser, referrerUserTeam.getNowYearPrice());
//
// 			if (referrerUser==null){
// 				return Result.fail(102, "处理失败", "推荐人调用会员升级模块失败");
// 			}
//
// 			String topReferrerId = referrerUser.getReferrerId();
// 			if(StringUtils.isNotBlank(topReferrerId)){
// 				User topReferrerUser= userRepository.findByReferrerCode(topReferrerId);
// 				//获取顶级推荐人用户团队
// 				if (topReferrerUser!=null) {
// 					Team topReferrerUserTeam = teamRepository.findByUserId(topReferrerUser.getId());
// 					//更新团队业绩记录
// 					topReferrerUserTeam.setTotalAchievements(achievementsArray.toJSONString());
// 					//更新团队业绩
// 					topReferrerUserTeam.setNowYearPrice(topReferrerUserTeam.getNowYearPrice().add(orderForm.getOrderFormFee()));
// 					//保存最新团队的数据
// 					topReferrerUserTeam = teamRepository.save(topReferrerUserTeam);
// 					//推荐人调用会员升级模块
// 					topReferrerUser = userConsumeUpLevel(topReferrerUser, topReferrerUserTeam.getNowYearPrice());
// 					if (topReferrerUser==null){
// 						return Result.fail(102, "处理失败", "推荐人调用会员升级模块失败");
// 					}
// 				}
// 			}
// 			// 奖励模块
// 			// JSONObject  buyRewardResult = recursiveBuyReward(user, orderForm, referrerUser);
// 			// if(buyRewardResult.getInteger("code")!=200){
// 			// 	return buyRewardResult;
// 			// }
//
// 			//调用计算返佣模块
// 			JSONObject  buyRewardResult = calculateRebate(user, orderForm, referrerUser);
// 			if(buyRewardResult.getInteger("code")!=200){
// 				return buyRewardResult;
// 			}
//
// 		}
//
// 		//调用用户升级模块
// 		user = userConsumeUpLevel(user,userTeam.getNowYearPrice());
// 		if (user==null){
// 			return Result.fail(102, "处理失败", "调用会员升级模块失败");
// 		}
//
// 		return Result.success(200, "处理成功", user);
// 	}
//
// 	/**
// 	 * 〈计算返佣〉
// 	 *
// 	 * @param user,orderForm,referrerUser
// 	 * @return:
// 	 * @since: 1.0.0
// 	 * @Author: Revisit-Moon
// 	 * @Date: 2019/5/10 3:09 PM
// 	 */
// 	@Override
// 	public JSONObject calculateRebate(User user, OrderForm orderForm, User referrerUser) {
// 		JSONObject result = new JSONObject();
// 		Role userRole = userService.getRole(user);
// 		BigDecimal rewardFee = new BigDecimal(0);
// 		Role referrerUserRole = userService.getRole(referrerUser);
// 		Rebate rebate = new Rebate();
//         rebate.setOrderFromId(orderForm.getId());
//         rebate.setUserId(user.getId());
//         String realName = user.getRealName();
//         if (StringUtils.isBlank(realName)) {
//             rebate.setUserName(user.getWeChatName());
//         }else{
//             rebate.setUserName(realName);
//         }
// 		//如果推荐人的角色是参与分销的角色,则计算奖励
// 		if (referrerUserRole.isDistributorship()) {
// 			logger.info("推荐人可参与分销");
// 			if (userRole.getLevel() < referrerUserRole.getLevel()) {
// 				logger.info("推荐人会员等级比被推荐人高级");
// 				BigDecimal referrerUserProductTotalFee = getReferrerUserProductTotalFee(referrerUserRole.getPriceLevel(), orderForm.getProductData());
// 				logger.info("推荐人购买此订单应付: " + referrerUserProductTotalFee);
// 				if (referrerUserProductTotalFee != null && referrerUserProductTotalFee.compareTo(new BigDecimal(0)) > 0) {
// 					referrerUserProductTotalFee = referrerUserProductTotalFee.add(orderForm.getPackageFee());
// 					referrerUserProductTotalFee = referrerUserProductTotalFee.add(orderForm.getLogisticsFee());
// 					rewardFee = MoonUtil.mathematical(orderForm.getOrderFormFee(), "-", referrerUserProductTotalFee, 2);
// 					logger.info("推荐人的奖励为: " + rewardFee + "元");
// 					if (rewardFee.compareTo(new BigDecimal(0)) > 0) {
// 						rebate.setReferrerUserId(referrerUser.getId());
// 						String referrerRealName = referrerUser.getRealName();
// 						if (StringUtils.isBlank(referrerRealName)) {
// 							rebate.setReferrerUserName(user.getWeChatName());
// 						}else{
// 							rebate.setReferrerUserName(referrerRealName);
// 						}
// 					}else{
// 						//如果奖励金额不大于0,则归零
// 						rewardFee = new BigDecimal(0);
// 					}
// 					rebate.setReferrerRebateFee(rewardFee);
// 					JSONObject orderFormRecordingResult = orderFormRecordingService.addOrderFormRewardRecording("分销奖励收入", "等待处理", rewardFee, orderForm, referrerUser);
// 					if(orderFormRecordingResult.getInteger("code")!=200){
// 						return orderFormRecordingResult;
// 					}
// 					rebate.setReferrerUserOrderFromRecordId(orderFormRecordingResult.getJSONObject("data").getString("id"));
// 					result.put("referrerUserReward",orderFormRecordingResult.get("data"));
// 				}
// 			}
// 		}
//
//
// 		String topUserReferrerCode = referrerUser.getReferrerId();
// 		if (StringUtils.isNotBlank(topUserReferrerCode)){
// 			BigDecimal topRewardFee = new BigDecimal(0);
// 			User topReferrerUser = userRepository.findByReferrerCode(topUserReferrerCode);
// 			if (topReferrerUser!=null){
// 				Role topReferrerUserRole = userService.getRole(topReferrerUser);
// 				if (topReferrerUserRole.isDistributorship()) {
// 					logger.info("顶级推荐人可参与分销");
// 					if (userRole.getLevel() < topReferrerUserRole.getLevel()) {
// 						logger.info("顶级推荐人会员等级比被推荐人高级");
// 						BigDecimal topReferrerUserProductTotalFee = getReferrerUserProductTotalFee(topReferrerUserRole.getPriceLevel(), orderForm.getProductData());
// 						logger.info("顶级推荐人购买此订单应付: " + topReferrerUserProductTotalFee);
// 						if (topReferrerUserProductTotalFee != null && topReferrerUserProductTotalFee.compareTo(new BigDecimal(0)) > 0) {
// 							topReferrerUserProductTotalFee = topReferrerUserProductTotalFee.add(orderForm.getPackageFee());
// 							topReferrerUserProductTotalFee = topReferrerUserProductTotalFee.add(orderForm.getLogisticsFee());
// 							topRewardFee = MoonUtil.mathematical(orderForm.getOrderFormFee(), "-", topReferrerUserProductTotalFee, 2);
// 							logger.info("顶级推荐人的奖励为: " + topRewardFee + "元");
// 							if (topRewardFee.compareTo(new BigDecimal(0)) > 0) {
// 								rebate.setTopReferrerUserId(topReferrerUser.getId());
// 								String topReferrerUserRealName = topReferrerUser.getRealName();
// 								if (StringUtils.isBlank(topReferrerUserRealName)) {
// 									rebate.setTopReferrerUserName(topReferrerUser.getWeChatName());
// 								}else{
// 									rebate.setTopReferrerUserName(topReferrerUserRealName);
// 								}
// 							}else{
// 								//如果奖励金额不大于0,则归零
// 								topRewardFee = new BigDecimal(0);
// 							}
// 							rebate.setTopReferrerRebateFee(topRewardFee);
// 							JSONObject topOrderFormRecordingResult = orderFormRecordingService.addOrderFormRewardRecording("分销奖励收入", "等待处理", topRewardFee, orderForm, topReferrerUser);
// 							if(topOrderFormRecordingResult.getInteger("code")!=200){
// 								return topOrderFormRecordingResult;
// 							}
// 							rebate.setTopUserOrderFromRecordId(topOrderFormRecordingResult.getJSONObject("data").getString("id"));
// 							result.put("topReferrerUserReward",topOrderFormRecordingResult.get("data"));
// 						}
// 					}
// 				}
// 			}
// 		}
//
// 		//判断是否需要记录
// 		String referrerUserId = rebate.getReferrerUserId();
// 		String topReferrerUserId = rebate.getTopReferrerUserId();
//
// 		if (StringUtils.isNotBlank(referrerUserId)||StringUtils.isNotBlank(topReferrerUserId)) {
// 			Rebate oldRebate = rebateRepository.findByUserIdAndOrderFromId(user.getId(), orderForm.getId());
// 			if (oldRebate == null) {
// 				rebate = rebateRepository.save(rebate);
// 			} else {
// 				rebate = oldRebate;
// 			}
// 			if (StringUtils.isBlank(rebate.getId())) {
// 				return Result.fail(110, "系统错误", "调用计算返佣模块失败");
// 			}
// 			orderForm.setRewardStatus("等待返佣");
// 			orderForm = orderFormRepository.save(orderForm);
// 			if (StringUtils.isBlank(orderForm.getId())) {
// 				return Result.fail(110, "系统错误", "调用计算返佣模块失败");
// 			}
// 		}
// 		return Result.success(200,"调用计算返佣模块成功",result);
// 	}
//
// 	//旧奖励模块
// 	// private JSONObject recursiveBuyReward(User user,OrderForm orderForm,User referrerUser){
// 	// 	JSONObject result = new JSONObject();
// 	// 	Role userRole = userService.getRole(user);
// 	// 	BigDecimal rewardFee = new BigDecimal(0);
// 	// 	Role referrerUserRole = userService.getRole(referrerUser);
// 	// 	//如果推荐人的角色是参与分销的角色,则计算奖励
// 	// 	if (referrerUserRole.isDistributorship()) {
// 	// 		logger.info("推荐人可参与分销");
// 	// 		if (userRole.getLevel() < referrerUserRole.getLevel()) {
// 	// 			logger.info("推荐人会员等级比被推荐人高级");
// 	// 			BigDecimal referrerUserProductTotalFee = getReferrerUserProductTotalFee(referrerUserRole.getPriceLevel(), orderForm.getProductData());
// 	// 			logger.info("推荐人购买此订单应付: " + referrerUserProductTotalFee);
// 	// 			if (referrerUserProductTotalFee != null && referrerUserProductTotalFee.compareTo(new BigDecimal(0)) > 0) {
// 	// 				referrerUserProductTotalFee = referrerUserProductTotalFee.add(orderForm.getPackageFee());
// 	// 				referrerUserProductTotalFee = referrerUserProductTotalFee.add(orderForm.getLogisticsFee());
// 	// 				rewardFee = MoonUtil.mathematical(orderForm.getOrderFormFee(), "-", referrerUserProductTotalFee, 2);
// 	// 				logger.info("推荐人的奖励为: " + rewardFee + "元");
// 	// 				if (rewardFee.compareTo(new BigDecimal(0)) > 0) {
// 	// 					Wallet referrerUserWallet = walletRepository.findByUserId(referrerUser.getId());
// 	// 					referrerUserWallet.setCanWithdrawPrice(MoonUtil.mathematical(referrerUserWallet.getCanWithdrawPrice(), "+", rewardFee, 2));
// 	// 				   	referrerUserWallet.setTotalIncome(referrerUserWallet.getTotalIncome().add(rewardFee));
// 	// 				   	referrerUserWallet = walletRepository.save(referrerUserWallet);
// 	// 					if (StringUtils.isBlank(referrerUserWallet.getId())) {
// 	// 						return Result.fail(102, "处理失败", "更新被推荐人时出错");
// 	// 					}
// 	// 				}else{
// 	// 					//如果奖励金额不大于0,则归零
// 	// 					rewardFee = new BigDecimal(0);
// 	// 				}
// 	// 			   JSONObject orderFormRecordingResult = orderFormRecordingService.addOrderFormRewardRecording("分销奖励收入", "已到账", rewardFee, orderForm, referrerUser);
// 	// 			   if(orderFormRecordingResult.getInteger("code")!=200){
// 	// 				   return orderFormRecordingResult;
// 	// 			   }
// 	// 			   result.put("referrerUserReward",orderFormRecordingResult.get("data"));
// 	// 			}
// 	// 		}
// 	// 	}
//     //
//     //
// 	// 	String topUserReferrerCode = referrerUser.getReferrerId();
// 	// 	if (StringUtils.isNotBlank(topUserReferrerCode)){
// 	// 		BigDecimal topRewardFee = new BigDecimal(0);
// 	// 		User topReferrerUser = userRepository.findByReferrerCode(topUserReferrerCode);
// 	// 		if (topReferrerUser!=null){
// 	// 			Role topReferrerUserRole = userService.getRole(topReferrerUser);
// 	// 			if (topReferrerUserRole.isDistributorship()) {
// 	// 				logger.info("顶级推荐人可参与分销");
// 	// 				if (userRole.getLevel() < topReferrerUserRole.getLevel()) {
// 	// 					logger.info("顶级推荐人会员等级比被推荐人高级");
// 	// 					BigDecimal topReferrerUserProductTotalFee = getReferrerUserProductTotalFee(topReferrerUserRole.getPriceLevel(), orderForm.getProductData());
// 	// 					logger.info("顶级推荐人购买此订单应付: " + topReferrerUserProductTotalFee);
// 	// 					if (topReferrerUserProductTotalFee != null && topReferrerUserProductTotalFee.compareTo(new BigDecimal(0)) > 0) {
// 	// 						topReferrerUserProductTotalFee = topReferrerUserProductTotalFee.add(orderForm.getPackageFee());
// 	// 						topReferrerUserProductTotalFee = topReferrerUserProductTotalFee.add(orderForm.getLogisticsFee());
// 	// 						topRewardFee = MoonUtil.mathematical(orderForm.getOrderFormFee(), "-", topReferrerUserProductTotalFee, 2);
// 	// 						logger.info("顶级推荐人的奖励为: " + topRewardFee + "元");
// 	// 						if (topRewardFee.compareTo(new BigDecimal(0)) > 0) {
// 	// 							Wallet topReferrerUserWallet = walletRepository.findByUserId(topReferrerUser.getId());
// 	// 							topReferrerUserWallet.setCanWithdrawPrice(MoonUtil.mathematical(topReferrerUserWallet.getCanWithdrawPrice(), "+", topRewardFee, 2));
// 	// 						   	topReferrerUserWallet.setTotalIncome(topReferrerUserWallet.getTotalIncome().add(topRewardFee));
// 	// 						   	topReferrerUserWallet = walletRepository.save(topReferrerUserWallet);
// 	// 							if (StringUtils.isBlank(topReferrerUserWallet.getId())) {
// 	// 								return Result.fail(102, "处理失败", "更新顶级被推荐人时出错");
// 	// 							}
// 	// 						}else{
// 	// 							//如果奖励金额不大于0,则归零
// 	// 							topRewardFee = new BigDecimal(0);
// 	// 						}
// 	// 						JSONObject topOrderFormRecordingResult = orderFormRecordingService.addOrderFormRewardRecording("分销奖励收入", "已到账", topRewardFee, orderForm, topReferrerUser);
// 	// 						if(topOrderFormRecordingResult.getInteger("code")!=200){
// 	// 							return topOrderFormRecordingResult;
// 	// 						}
//     //
// 	// 					   result.put("topReferrerUserReward",topOrderFormRecordingResult.get("data"));
// 	// 				   }
// 	// 				}
// 	// 			}
// 	// 		}
// 	// 	}
// 	// 	return Result.success(200,"调用升级模块成功",result);
// 	// }
//
// 	/**
// 	 * 〈用户充值升级模块〉
// 	 *
// 	 * @param user
// 	 * @return:
// 	 * @since: 1.0.0
// 	 * @Author: Revisit-Moon
// 	 * @Date: 2019/4/28 4:00 AM
// 	 */
// 	private Role userRechargeUpLevel(User user,BigDecimal rechargePrice) {
//
// 		Role role = userService.getRole(user);
//         List<Role> nextLevelRoleList = roleRepository.findByNextLevel(role.getLevel());
//         boolean canUpLevel = false;
//         BigDecimal upLevelNeedPrice = new BigDecimal(0);
//         for (Role nextLevelRole :nextLevelRoleList) {
//         	if (nextLevelRole.getLevel()>role.getLevel()) {
// 				upLevelNeedPrice = nextLevelRole.getUpLevelPrice();
// 				//如果升级金额不等于0且个人业绩加团队业绩的金额大于升级金额,则替换角色
// 				if (upLevelNeedPrice.compareTo(new BigDecimal(0)) > 0
// 						&& rechargePrice.compareTo(upLevelNeedPrice) >= 0) {
// 					canUpLevel = true;
// 					Set<Role> roles = user.getRoles();
// 					roles.clear();
// 					roles.add(nextLevelRole);
// 					user.setRoles(roles);
// 				}
// 			}
//         }
//
//         if (canUpLevel) {
//             user = userRepository.save(user);
//             role = userService.getRole(user);
//         }
// 		return role;
// 	}
//
// 	/**
// 	 * 〈用户消费升级模块〉
// 	 *
// 	 * @param user
// 	 * @return:
// 	 * @since: 1.0.0
// 	 * @Author: Revisit-Moon
// 	 * @Date: 2019/4/28 4:00 AM
// 	 */
// 	private User userConsumeUpLevel(User user,BigDecimal userAllOneYearBuyTotalPrice) {
// 		// Role userRole = userService.getRole(user);
// 		// List<Role> nextLevelRoleList = roleRepository.findByNextLevel(userRole.getLevel());
// 		// boolean canUpLevel = false;
// 		// BigDecimal upLevelNeedPrice = new BigDecimal(0);
// 		// for (Role nextLevelRole :nextLevelRoleList) {
// 		// 	如果获取到的下级会员等级比自身高
// 			// if (nextLevelRole.getLevel()>userRole.getLevel()) {
// 				判断团队业绩是否达到可升级金额,是则替换角色
// 				// upLevelNeedPrice = nextLevelRole.getUpLevelPrice();
// 				// if (upLevelNeedPrice.compareTo(new BigDecimal(0)) > 0
// 				// 		&& userAllOneYearBuyTotalPrice.compareTo(upLevelNeedPrice)>= 0) {
// 				// 	canUpLevel = true;
// 				// 	userRole = nextLevelRole;
// 				// 	Set<Role> roles = user.getRoles();
// 				// 	roles.clear();
// 				// 	roles.add(userRole);
// 				// 	user.setRoles(roles);
// 				// 	break;
// 				// }
// 			// }
// 		// }
//
// 		// if (canUpLevel) {
// 		// 	user.setMemberEndTime(null);
//         //     user = userRepository.save(user);
//         // }
// 		return user;
// 	}
//
// 	/**
// 	 * 〈获取推荐人纯商品价格〉
// 	 *
// 	 * @param referrerUserRolePriceLevel,orderFormData
// 	 * @return:
// 	 * @since: 1.0.0
// 	 * @Author: Revisit-Moon
// 	 * @Date: 2019/4/28 3:34 AM
// 	 */
// 	private BigDecimal getReferrerUserProductTotalFee(int referrerUserRolePriceLevel,String orderFormData){
//
// 		//判断该订单购买人是否使用了优惠券
// 		JSONArray productDataArray = JSONArray.parseArray(orderFormData);
//
// 		//获取订单数据内容
// 		BigDecimal referrerUserTotalPrice = new BigDecimal(0);
// 		for (int i = 0; i < productDataArray.size() ; i++) {
// 			JSONObject productData = null;
// 			try {
// 				productData = productDataArray.getJSONObject(i);
// 				String skuId = productData.getString("skuId");
//
// 				int buyNumber = productData.getInteger("buyNumber");
//
// 				ProductSku sku = productSkuRepository.findById(skuId).orElse(null);
//
// 				if (sku != null) {
// 					BigDecimal realUnitPrice = productSkuService.whatPriceByRoleNameAndSku(referrerUserRolePriceLevel, sku);
// 					referrerUserTotalPrice = referrerUserTotalPrice.add(MoonUtil.mathematical(realUnitPrice,"*",buyNumber,2));
// 				}
// 			} catch (Exception e) {
// 				return null;
// 			}
// 		}
// 		return referrerUserTotalPrice;
// 	}
// }