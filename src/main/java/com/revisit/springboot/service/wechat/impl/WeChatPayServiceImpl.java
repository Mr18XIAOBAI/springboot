/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: WeChatPayServiceImpl
 * Author:   Revisit-Moon
 * Date:     2019/2/21 11:41 AM
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/21 11:41 AM        1.0              描述
 */

package com.revisit.springboot.service.wechat.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.component.uuid.CustomizeUUIDGenerate;
import com.revisit.springboot.entity.orderform.OrderForm;
import com.revisit.springboot.entity.productsku.ProductSku;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.repository.orderform.OrderFormRepository;
import com.revisit.springboot.repository.product.ProductRepository;
import com.revisit.springboot.repository.productsku.ProductSkuRepository;
import com.revisit.springboot.repository.user.UserRepository;
import com.revisit.springboot.service.productsku.ProductSkuService;
import com.revisit.springboot.service.wechat.WeChatPayService;
import com.revisit.springboot.utils.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 〈〉
 *
 * @author Revisit-Moon
 * @create 2019/2/21
 * @since 1.0.0
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class WeChatPayServiceImpl implements WeChatPayService {

    @Autowired
    private OrderFormRepository orderFormRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductSkuService productSkuService;

    @Autowired
    private ProductRepository productRepository;

    private ProductSkuRepository productSkuRepository;

    @Autowired
    private HttpServletRequest request;

    private final static Logger logger = LoggerFactory.getLogger(WeChatPayService.class);


    /**
     * 〈微信统一下单〉
     *
     * @param orderForm
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/14 3:12 PM
     */
    @Override
    public JSONObject weChatPay(OrderForm orderForm) {
        try {
            String paymentMode = orderForm.getPaymentMode();
            if (StringUtils.isBlank(paymentMode)){
                return Result.fail(102,"参数错误","请选择支付方式");
            }
            String useScenes = orderForm.getUseScenes();
            //获取用户IP地址
            orderForm.setSubmitOrderFormIp(MoonUtil.getRealIpAddress(request));
            String thirdPartyOrderNumber = orderForm.getThirdPartyOrderNumber();
            if (StringUtils.isBlank(thirdPartyOrderNumber)) {
                //生成第三方订单号
                orderForm.setThirdPartyOrderNumber(MoonUtil.createWeChatOrderNum());
            }
            //获取ASCII排序后且MD5加密后的字符串
            Map<String, Object> weChatPayTreeMap = WeChatUtil.getWeChatPayTreeMap(orderForm);
            //获取要发送给微信服务器统一下单接口的xml格式的字符串
            String weChatPayXmlStr = WeChatUtil.getWeChatPayXmlStr(weChatPayTreeMap);
            //发送给微信服务器,在JSAPI模式下,如果返回的是json数据.则代表错误
            String xmlPost = HttpUtil.httpXmlPost(WeChatUtil.getWeChatPayUnifiedOrderUrl(), weChatPayXmlStr);
            logger.info("微信支付发起结果: ");
            logger.info("\n"+xmlPost);
            //将微信系统返回的xml格式化成Map集合
            Map<String, Object> weChatXmlMap = WeChatUtil.weChatXmlToString(xmlPost);
            if (weChatXmlMap == null) {
                return Result.fail(110, "系统错误", "微信支付返回xml解析错误,请联系管理员");
            }
            //微信服务器返回的通信标识
            if (!weChatXmlMap.get("return_code").equals("SUCCESS")) {
                return Result.fail(102, "参数错误", "微信支付发起失败,失败原因: "+weChatXmlMap.get("return_msg"));
            }
            //微信服务器返回的业务标识
            if (!weChatXmlMap.get("result_code").equals("SUCCESS")) {
                if((weChatXmlMap.get("err_code_des").toString().contains("商户订单号重复"))){
                    orderForm.setThirdPartyOrderNumber(MoonUtil.createWeChatOrderNum());
                    return weChatPay(orderForm);
                }
                return Result.fail(102, "参数错误", "微信支付发起失败,错误码: "+weChatXmlMap.get("err_code")+",错误信息: "+weChatXmlMap.get("err_code_des"));
            }

            orderForm = orderFormRepository.save(orderForm);
            if (StringUtils.isBlank(orderForm.getId())){
                return Result.fail(110,"系统错误","保存订单失败");
            }
            //如果是JSAPI支付模式
            if (useScenes.equals("JSAPI")) {
                //再次MD5加密封装参数给前端调用
                Map<String, Object> payData = WeChatUtil.getJSAPIPayData(weChatXmlMap);
                payData.put("orderFormId",orderForm.getId());
                return Result.success(200, "调用JSAPI支付成功", payData);
            }

            //如果是NATIVE支付模式
            if (useScenes.equals("NATIVE")) {
                String code_url = weChatXmlMap.get("code_url").toString();
                if (StringUtils.isBlank(code_url)){
                    return Result.fail(102, "参数错误", "二维码生成失败,错误原因: "+weChatXmlMap);
                }else{
                    return Result.success(200,"生成二维码成功",code_url);
                }
            }

            //如果是MWEB支付模式
            if (useScenes.equals("MWEB")) {
                String code_url = weChatXmlMap.get("code_url").toString();
                if (StringUtils.isBlank(code_url)){
                    return Result.fail(102, "参数错误", "二维码生成失败,错误原因: "+weChatXmlMap);
                }else{
                    return Result.success(200,"生成二维码成功",code_url);
                }
            }

            //如果是APP支付模式
            if (useScenes.equals("APP")) {
                return Result.success(102,"参数错误","APP支付模式暂未开发");
            }

            return null;
        }catch (Exception e){
            return Result.fail(110,"系统错误","发起微信支付失败,错误信息: "+e.getCause().toString());
        }
    }

    /**
     * 〈微信支付结果回调处理〉
     *
     * @param weChatXml
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/14 3:13 PM
     */
    @Override
    public String weChatPayNotify(String weChatXml) {
        Map<String, Object> xmlMap = WeChatUtil.xmlToMap(weChatXml);
        boolean isWeChat = WeChatUtil.checkIsWeChatSign(xmlMap);
        if (isWeChat){
            String thirdPartyOrderNumber = xmlMap.get("out_trade_no").toString();
            if (StringUtils.isBlank(thirdPartyOrderNumber)){
                return WeChatUtil.getWeChatPayFailXml("out_trade_no参数为空");
            }
            OrderForm oldOrderForm = orderFormRepository.findByThirdPartyOrderNumber(thirdPartyOrderNumber);
            if (oldOrderForm==null){
                return WeChatUtil.getWeChatPayFailXml("订单不存在");
            }

            if (!oldOrderForm.getStatus().equals("待付款")){
                logger.info("已处理的订单,直接回复微信服务器");
                return WeChatUtil.getWeChatPaySuccessXml();
            }

            oldOrderForm.setPayTime(MoonUtil.getNowTimeSecondPrecision());
            switch (oldOrderForm.getOrderFormType()) {
                case "商品":{
                    //调用商品订单处理模块
                    return productOrderFormCallBackHandler(oldOrderForm);
                }
                case "充值会员": {
                    //调用会员充值订单处理模块
                    // return memberOrderFormCallBackHandler(oldOrderForm);
                }
                case "充值钱包": {
                    //调用充值钱包订单处理模块
                    // return rechargeWalletOrderFormCallBackHandler(oldOrderForm);
                }
                case "团购": {
                    //调用拼团订单处理模块
                    // return assembleProductOrderFormCallBackHandler(oldOrderForm);
                }
                default: {
                    return WeChatUtil.getWeChatPayFailXml("系统不存在该类型的订单");
                }
            }
        }else {
            return WeChatUtil.getWeChatPayFailXml("签名验证失败");
        }
    }

    /**
     * 〈微信订单退款〉
     *
     * @param orderForm,howMuch
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/7 12:53 AM
     */
    @Override
    public JSONObject weChatPayRefundMoney(OrderForm orderForm,BigDecimal howMuch) {
        try {
            //获取ASCII排序后且MD5加密后的字符串
            Map<String, Object> weChatPayRefundMoneyTreeMap = WeChatUtil.getWeChatPayRefundMoneyTreeMap(orderForm, howMuch);

            //获取要发送给微信服务器统一下单接口的xml格式的字符串
            String weChatRefundMoneyXmlStr = WeChatUtil.getWeChatPayXmlStr(weChatPayRefundMoneyTreeMap);

            //退款需要使用SSL证书,HTTPS协议
            String xmlPost = HttpUtil.httpXmlPostSSL(WeChatUtil.getWeChatPayRefundUrl(), weChatRefundMoneyXmlStr);
            logger.info("微信支付退款申请结果: ");
            logger.info("\n"+xmlPost);
            //将微信系统返回的xml格式化成Map集合
            Map<String, Object> weChatXmlMap = WeChatUtil.weChatXmlToString(xmlPost);
            if (weChatXmlMap == null) {
                return Result.fail(110, "系统错误", "微信支付申请退款返回xml解析错误,请联系管理员");
            }
            //微信服务器返回的通信标识
            if (!weChatXmlMap.get("return_code").equals("SUCCESS")) {
                return Result.fail(102, "参数错误", "微信支付申请退款发起失败,失败原因: "+weChatXmlMap.get("return_msg"));
            }
            //微信服务器返回的业务标识
            if (!weChatXmlMap.get("result_code").equals("SUCCESS")) {
                return Result.fail(102, "参数错误", "微信支付申请退款发起失败,错误码: "+weChatXmlMap.get("err_code")+",错误信息: "+weChatXmlMap.get("err_code_des"));
            }
        }catch (Exception e){
            return Result.fail(102, "参数错误", "微信支付申请退款失败: "+e.getCause());
        }
        return Result.success(200,"发送微信退款申请成功","结果请通过退款查询接口查询");
    }

    /**
     * 〈微信退款结果回调〉
     *
     * @param weChatXml
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/7 12:53 AM
     */
    @Override
    public String weChatRefundMoneyNotify(String weChatXml) {
        Map<String, Object> xmlMap = WeChatUtil.xmlToMap(weChatXml);
        //判断是否微信服务器调用
        if (xmlMap.get("appid")==null||xmlMap.get("mch_id")==null){
            return WeChatUtil.getWeChatPayFailXml("非法操作");
        }

        if (!xmlMap.get("appid").toString().equals(WeChatUtil.getWeChatAppId())
                ||!xmlMap.get("mch_id").toString().equals(WeChatUtil.getWeChatMchId())){
            return WeChatUtil.getWeChatPayFailXml("非法操作");
        }

        xmlMap = WeChatUtil.decryptWeChatRefundMoneyXmlMap(xmlMap);
        if (xmlMap!=null){
            if(xmlMap.get("refund_status").toString().equals("SUCCESS")) {
                String thirdPartyOrderNumber = xmlMap.get("out_trade_no").toString();
                if (StringUtils.isBlank(thirdPartyOrderNumber)) {
                    return WeChatUtil.getWeChatPayFailXml("out_trade_no参数为空");
                }
                OrderForm oldOrderForm = orderFormRepository.findByThirdPartyOrderNumber(thirdPartyOrderNumber);
                if (oldOrderForm == null) {
                    return WeChatUtil.getWeChatPayFailXml("订单不存在");
                }

                if (oldOrderForm.getStatus().equals("已退款")) {
                    logger.info("已处理的订单,直接回复微信服务器");
                    return WeChatUtil.getWeChatPaySuccessXml();
                }

                // JSONObject refundRecordingBean = orderFormRecordingService.addOrderFormRefundRecording(oldOrderForm, oldOrderForm.getOrderFormType() + "退款");
                // if (refundRecordingBean.getInteger("code")!=200){
                //     return WeChatUtil.getWeChatPayFailXml("保存订单记录失败");
                // }
                oldOrderForm.setStatus("已退款");
                oldOrderForm = orderFormRepository.saveAndFlush(oldOrderForm);
                if (StringUtils.isBlank(oldOrderForm.getId())){
                    return WeChatUtil.getWeChatPayFailXml("更新订单失败");
                }
            }
            return WeChatUtil.getWeChatPaySuccessXml();
        }else {
            return WeChatUtil.getWeChatPayFailXml("服务器解密失败");
        }
    }

    /**
     * 〈商品订单处理模块〉
     *
     * @param oldOrderForm
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/20 6:11 PM
     */
    private String productOrderFormCallBackHandler(OrderForm oldOrderForm){
        if (oldOrderForm.getDeliveryMode().equals("自提")) {
            oldOrderForm.setStatus("待提货");
            String sign = oldOrderForm.getId() + oldOrderForm.getOpenId() + oldOrderForm.getConsignee() + oldOrderForm.getStatus() + MoonUtil.dataToyMdHmsNotSymbol(oldOrderForm.getPayTime());
            logger.info("自提订单拼接后的字符串: "+sign);
            try {
                oldOrderForm.setExtractionSign(DigestUtils.md5Hex(sign.getBytes("UTF-8")).toUpperCase());
                logger.info("自提订单加密结果: "+oldOrderForm.getExtractionSign());
            } catch (Exception e) {
                logger.info("加密错误,请手动加密");
                e.printStackTrace();
            }
        }
        if (oldOrderForm.getDeliveryMode().equals("配送")) {
            oldOrderForm.setStatus("待发货");
        }
        // oldOrderForm.setPayTime(MoonUtil.getNowTimeSecondPrecision());
        oldOrderForm = orderFormRepository.saveAndFlush(oldOrderForm);

        // String useDiscountId = oldOrderForm.getUseDiscountId();
        // if (StringUtils.isNotBlank(useDiscountId)){
        //     CouponRelation couponRelation = couponRelationRepository.findByCouponIdAndUserId(useDiscountId, oldOrderForm.getUserId());
        //     if (couponRelation!=null){
        //         couponRelation.setAlreadyUse(true);
        //         couponRelation.setStatus("已使用");
        //         couponRelationRepository.save(couponRelation);
        //     }
        // }
        // JSONObject orderFormConsumeRecordingResult = orderFormRecordingService.addOrderFormConsumeRecording(oldOrderForm);
        // if (orderFormConsumeRecordingResult.getInteger("code")!=200){
        //     return WeChatUtil.getWeChatPayFailXml(orderFormConsumeRecordingResult.getString("errorMsg"));
        // }
        //转换商品数据为JSON对象
        JSONArray productArray = JSONArray.parseArray(oldOrderForm.getProductData());
        if (productArray == null || productArray.isEmpty()) {
            return WeChatUtil.getWeChatPayFailXml("订单数据为空");
        }
        User user = userRepository.findById(oldOrderForm.getUserId()).orElse(null);
        String referrerId = user.getReferrerId();
        //如果有推荐人
        if (StringUtils.isNotBlank(referrerId)){
            User referrerUser = userRepository.findByReferrerCode(referrerId);
            // logger.info("调用推荐人消费奖励模块");
            // JSONObject resultBean = walletService.buyReward(user,oldOrderForm,referrerUser);
            // if (resultBean.getInteger("code")!=200){
            //     return WeChatUtil.getWeChatPayFailXml(resultBean.getString("errorMsg"));
            // }
        }
        //定义待更新的商品sku数组
        Map<String, Object> skuIdAndStockMap = new HashMap<>();
        Map<String, JSONObject> splitOrderFormMap = new HashMap<>();
        //获取订单数据内容
        for (int i = 0; i < productArray.size(); i++) {
            JSONObject productData = null;
            try {
                productData = productArray.getJSONObject(i);
                String skuId = productData.getString("skuId");
                int buyNumber = productData.getInteger("buyNumber");
                skuIdAndStockMap.put(skuId, buyNumber);
                //获得分单所需信息
                String shopId = productRepository.findShopIdByProductSkuId(skuId);
                if (StringUtils.isNotBlank(shopId)){
                    JSONObject splitOrderFormBean = splitOrderFormMap.get(shopId);
                    if (splitOrderFormBean==null||splitOrderFormBean.isEmpty()) {
                        splitOrderFormBean = new JSONObject();
                        splitOrderFormBean.put("shopId", shopId);
                        JSONArray productDataArray = new JSONArray();
                        productDataArray.add(productData);
                        splitOrderFormBean.put("productData",productDataArray);
                    }else {
                        JSONArray oldProductDataArray = splitOrderFormBean.getJSONArray("productData");
                        oldProductDataArray.add(productData);
                        splitOrderFormBean.put("shopId", shopId);
                        splitOrderFormBean.put("productData",oldProductDataArray);
                    }
                }
            } catch (Exception e) {
                return WeChatUtil.getWeChatPayFailXml("转换商品数据出错");
            }
        }

        if (skuIdAndStockMap.isEmpty()) {
            return WeChatUtil.getWeChatPayFailXml("商品数据为空");
        }

        if (!productSkuService.deductStock(skuIdAndStockMap)) {
            return WeChatUtil.getWeChatPayFailXml("服务器异常");
        }

        //分单
        for (Map.Entry entry :splitOrderFormMap.entrySet()) {
            OrderForm orderForm = new OrderForm();
            //复制对象
            JavaBeanUtil.copyProperties(oldOrderForm,orderForm);
            String shopId = entry.getKey().toString();
            String productDataArray = entry.getValue().toString();
            orderForm.setShopId(shopId);
            orderForm.setProductData(productDataArray);
            String orderFormDetail = "";
            for (int i = 0; i < productArray.size() ; i++) {
                JSONObject productData = productArray.getJSONObject(i);
                String skuId = productData.getString("skuId");
                int buyNumber = productData.getInteger("buyNumber");
                BigDecimal unitPrice = productData.getBigDecimal("unitPrice");
                BigDecimal unitTotalPrice = MoonUtil.mathematical(unitPrice, "*", buyNumber, 2);
                ProductSku sku = productSkuRepository.findById(skuId).orElse(null);
                if (sku == null){
                    return WeChatUtil.getWeChatPayFailXml("获取商品异常");
                }
                orderFormDetail += "[" + sku.getSkuName() + " " + buyNumber + " " + sku.getSkuUnit() + " " + unitTotalPrice.toString() + " 元],";
            }
            orderFormDetail = orderFormDetail.substring(0,orderFormDetail.length()-1);
            orderFormDetail = orderFormDetail+"[运费: "+orderForm.getLogisticsFee()+" 元,包装费: "+orderForm.getPackageFee()+"元]";
            orderForm.setOrderFormDetail(orderFormDetail);
            orderForm = orderFormRepository.save(orderForm);
            if (StringUtils.isBlank(orderForm.getId())||orderForm.getId().equals(oldOrderForm.getId())){
                return WeChatUtil.getWeChatPayFailXml("拆分订单失败");
            }
        }

        // orderFormRepository.delete(oldOrderForm);

        return WeChatUtil.getWeChatPaySuccessXml();
    }
}
