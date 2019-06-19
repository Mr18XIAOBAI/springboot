/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: WeChatUtil
 * Author:   Revisit-Moon
 * Date:     2019/2/12 4:15 PM
 * Description: WeChatUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/12 4:15 PM        1.0              描述
 */

package com.revisit.springboot.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.component.uuid.CustomizeUUIDGenerate;
import com.revisit.springboot.component.wechat.WeChat;
import com.revisit.springboot.entity.orderform.OrderForm;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.RSAPublicKeySpec;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 〈WeChatUtil〉
 *
 * @author Revisit-Moon
 * @create 2019/2/12
 * @since 1.0.0
 */
@Component
public class WeChatUtil {

    static WeChat wechat;

    @Autowired
    WeChat weChat;

    @PostConstruct
    public void init() {
        wechat = weChat;
    }

    private final static Logger logger = LoggerFactory.getLogger(WeChatUtil.class);

    //参数分别代表 算法名称/加密模式/数据填充方式
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    //Base64解密对象
    final static Base64.Decoder decoder = Base64.getDecoder();

    //Base64加密对象
    final static Base64.Encoder encoder = Base64.getEncoder();

    //银行名字对应编号集合
    final static Map<String,Integer> bankMap = new HashMap<>();
    private final static void initBankMap() {
        bankMap.put("工商银行",1002);
        bankMap.put("农业银行",1005);
        bankMap.put("中国银行",1026);
        bankMap.put("建设银行",1003);
        bankMap.put("招商银行",1001);
        bankMap.put("邮储银行",1066);
        bankMap.put("交通银行",1020);
        bankMap.put("浦发银行",1004);
        bankMap.put("民生银行",1006);
        bankMap.put("兴业银行",1009);
        bankMap.put("平安银行",1010);
        bankMap.put("中信银行",1021);
        bankMap.put("华夏银行",1025);
        bankMap.put("广发银行",1027);
        bankMap.put("光大银行",1022);
        bankMap.put("北京银行",4836);
        bankMap.put("宁波银行",1056);
    }


    public static String getMiniProgramUrl(String jsCode){

        String miniProgramUrl = "https://api.weixin.qq.com/sns/jscode2session?grant_type=authorization_code"
                + "&appid=" + wechat.getWeChatAppId()
                + "&secret=" + wechat.getWeChatAppSecret()
                + "&js_code=" + jsCode;
        return miniProgramUrl;
    }

    /**
     * 〈解密小程序登录系统时获取的wx.getUserInfo()〉
     *
     * @param encryptedData,iv,sessionKey
     * @return: String
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/21 12:56 PM
     */
    public static String decryptWeChatMiniProgramSessionKey(String encryptedData, String iv, String sessionKey){
        String result = null;
        try {
            // base64解密
            byte[] aesKey = decoder.decode(sessionKey.getBytes("UTF-8"));   // 得到密钥数组
            byte[] aesIv = decoder.decode(iv.getBytes("UTF-8"));    // 得到偏移量数组
            byte[] aesEncryptedData = decoder.decode(encryptedData.getBytes("UTF-8"));  // 得到待解密数据数组
            //指定算法/解密方式/填充模式
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            //创建密钥规格对象
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");

            // 初始化解密对象
            cipher.init(Cipher.DECRYPT_MODE, keySpec, generateIV(aesIv));

            //开始解密
            byte[] doFinal = cipher.doFinal(aesEncryptedData);

            //如果结果不为空
            if(null != doFinal && doFinal.length > 0){
                result = new String(doFinal, "UTF-8");
            }
            logger.info("WeChatDecryptData解密成功: " + result);
            // closeEncryptionObject(cipher,keySpec,aesKey,aesIv,aesEncryptedData);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("WeChatDecryptData解密失败: "+e.getCause());
            return null;
        }
    }

    /**
     * 〈获取加密算法的初始向量〉
     *
     * @param iv
     * @return: IvParameterSpec
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/21 12:56 PM
     */
    private static IvParameterSpec generateIV(byte[] iv) throws Exception {
        return new IvParameterSpec(iv);
    }

    /**
     * 〈获取微信支付树形Map〉
     *
     * @param orderForm
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:29 PM
     */
    public static Map<String,Object> getWeChatPayTreeMap(OrderForm orderForm){
        Map<String, Object> asciiSortMap = new TreeMap<>();
        //写入微信系统基础信息
        asciiSortMap.put("appid",wechat.getWeChatAppId());
        asciiSortMap.put("mch_id",wechat.getWeChatMchId());
        //写入终端设备
        asciiSortMap.put("device_info","WEB");
        //写入随机字符串
        asciiSortMap.put("nonce_str",CustomizeUUIDGenerate.generateBase64UUID(UUID.randomUUID().toString()));
        //写入商品描述
        String orderFormDetail = orderForm.getOrderFormDetail();
        logger.info("订单详情: "+ orderFormDetail);

        //修复订单详情中含有emoji表情
        orderFormDetail = MoonUtil.fixEmoji(orderFormDetail);

        //修复订单详情超过128字节
        orderFormDetail = MoonUtil.limitStringLength(orderFormDetail,125)+"...";

        //写入订单详情
        asciiSortMap.put("body",orderFormDetail);
        //写入自定义参数
        // String productData = orderform.getProductData();
        // if(StringUtils.isNotBlank(productData)) {
        //     asciiSortMap.put("attach", orderform.getProductData());
        // }

        //写入系统单号
        asciiSortMap.put("out_trade_no",orderForm.getThirdPartyOrderNumber());
        //写入订单总价
        asciiSortMap.put("total_fee",MoonUtil.mathematical(orderForm.getOrderFormFee(),"*",100,0).intValue());
        //写入发起订单的IP
        asciiSortMap.put("spbill_create_ip",orderForm.getSubmitOrderFormIp());
        //写入订单开始时间
        asciiSortMap.put("time_start",getNowTimeYMDHSM(new Date()));
        //写入订单结束时间
        asciiSortMap.put("time_expire",getNowTimeYMDHSM(MoonUtil.yMdHmsToAfterTime(new Date(),2,"小时")));
        //写入订单支付类型
        asciiSortMap.put("trade_type",orderForm.getUseScenes());

        if (orderForm.getUseScenes().equals("JSAPI")){
            asciiSortMap.put("openid",orderForm.getOpenId());
        }
        if (orderForm.getUseScenes().equals("NATIVE")){
            asciiSortMap.put("product_id",orderForm.getThirdPartyOrderNumber());
        }
        if (orderForm.getUseScenes().equals("MWEB")){
            JSONObject mWebJsonBean = new JSONObject();
            JSONObject scene_info = new JSONObject();
            scene_info.put("type","Wap");
            scene_info.put("wap_url",wechat.getWeChatWapUrl());
            scene_info.put("wap_name",wechat.getWeChatWapName());
            mWebJsonBean.put("h5_info",scene_info);
            asciiSortMap.put("scene_info",scene_info);
        }
        //写入加密方式
        asciiSortMap.put("sign_type","MD5");
        //写入订单回调地址
        asciiSortMap.put("notify_url",wechat.getWeChatPayNotifyUrl());

        //获取ASCII排序后的签名
        String sign = getSign(asciiSortMap);

        asciiSortMap.put("sign",sign);

        return asciiSortMap;
    }

    /**
     * 〈获取微信支付企业付款树形Map〉
     *
     * @param withdrawalRecord
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:29 PM
     */
    // public static Map<String,Object> getWeChatCorporatePayTreeMap(WithdrawalRecord withdrawalRecord){
    //     Map<String, Object> asciiSortMap = new TreeMap<>();
    //
    //     //写入随机字符串
    //     asciiSortMap.put("nonce_str",CustomizeUUIDGenerate.generateBase64UUID(UUID.randomUUID().toString()));
    //     //写入第三方单号
    //     asciiSortMap.put("partner_trade_no",withdrawalRecord.getThirdPartyOrderNumber());
    //
    //     //如果付款到银行卡写入效验真名参数
    //     if (withdrawalRecord.getWithdrawalTarget().equals("银行卡")) {
    //         File file = new File(ClassUtils.getDefaultClassLoader().getResource("").getPath()+ "public.pem");
    //         if (!file.exists()){
    //             logger.info("RSA公钥不存在,重新获取");
    //             getRsaPublicPem();
    //         }
    //         try {
    //             PublicKey publicKey = readLocalPubKey();
    //             asciiSortMap.put("mch_id",wechat.getWeChatMchId());
    //             String encBankNo = encryptRSA(withdrawalRecord.getBankCardNumber().getBytes("UTF-8"), publicKey);
    //             if (StringUtils.isBlank(encBankNo)){
    //                 logger.info("RSA算法加密银行卡号失败");
    //                 return null;
    //             }
    //             asciiSortMap.put("enc_bank_no",encBankNo);
    //             String encTrueName = encryptRSA(withdrawalRecord.getBankUserRealName().getBytes("UTF-8"), publicKey);
    //             if (StringUtils.isBlank(encTrueName)){
    //                 logger.info("RSA算法加密持卡人姓名失败");
    //                 return null;
    //             }
    //             asciiSortMap.put("enc_true_name",encTrueName);
    //             asciiSortMap.put("bank_code", withdrawalRecord.getBankCode());
    //         }catch (Exception e){
    //             logger.info("RSA算法加密失败");
    //             return null;
    //         }
    //     }
    //     //如果付款到微信钱包,不效验真名
    //     if (withdrawalRecord.getWithdrawalTarget().equals("微信钱包")) {
    //         asciiSortMap.put("check_name", "NO_CHECK");
    //         //写入微信系统基础信息
    //         asciiSortMap.put("mch_appid",wechat.getWeChatAppId());
    //         asciiSortMap.put("mchid",wechat.getWeChatMchId());
    //         asciiSortMap.put("spbill_create_ip",withdrawalRecord.getSubmitWithdrawalIp());
    //         //写入终端设备
    //         asciiSortMap.put("device_info","WEB");
    //         //写入用户openId
    //         asciiSortMap.put("openid",withdrawalRecord.getWithdrawalUserOpenId());
    //     }
    //     //付款金额
    //     asciiSortMap.put("amount",MoonUtil.mathematical(withdrawalRecord.getWithdrawalPrice(),"*",100,0).intValue());
    //     //写入描诉
    //     asciiSortMap.put("desc",withdrawalRecord.getRemark());
    //
    //     //获取ASCII排序后的签名
    //     String sign = getSign(asciiSortMap);
    //
    //     asciiSortMap.put("sign",sign);
    //
    //     return asciiSortMap;
    // }

    /**
     * 〈获取微信支付查询企业付款树形Map〉
     *
     * @param thirdPartyOrderNumber
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:29 PM
     */
    public static Map<String,Object> getSelectWeChatCorporatePayTreeMap(String thirdPartyOrderNumber){
        Map<String, Object> asciiSortMap = new TreeMap<>();

        //写入随机字符串
        asciiSortMap.put("nonce_str",CustomizeUUIDGenerate.generateBase64UUID(UUID.randomUUID().toString()));
        //写入第三方单号
        asciiSortMap.put("partner_trade_no",thirdPartyOrderNumber);
        //付款金额
        asciiSortMap.put("mch_id",wechat.getWeChatMchId());
        //写入描诉
        asciiSortMap.put("appid",wechat.getWeChatAppId());

        //获取ASCII排序后的签名
        String sign = getSign(asciiSortMap);

        asciiSortMap.put("sign",sign);

        return asciiSortMap;
    }


    /**
     * 〈获取微信支付申请退款树形Map〉
     *
     * @param orderForm,howMuch
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:29 PM
     */
    public static Map<String,Object> getWeChatPayRefundMoneyTreeMap(OrderForm orderForm, BigDecimal howMuch){
        Map<String, Object> asciiSortMap = new TreeMap<>();
        //写入微信系统基础信息
        asciiSortMap.put("appid",wechat.getWeChatAppId());
        asciiSortMap.put("mch_id",wechat.getWeChatMchId());
        //写入随机字符串
        asciiSortMap.put("nonce_str",CustomizeUUIDGenerate.generateBase64UUID(UUID.randomUUID().toString()));

        //写入订单退款回调地址
        asciiSortMap.put("notify_url",wechat.getWeChatPayRefundMoneyNotifyUrl());

        //写入退款单号
        asciiSortMap.put("out_refund_no", orderForm.getThirdRefundOrderNumber());


        //写入系统单号
        asciiSortMap.put("out_trade_no",orderForm.getThirdPartyOrderNumber());
        if (howMuch.compareTo(new BigDecimal(0))==0) {
            asciiSortMap.put("refund_fee", orderForm.getOrderFormFee());
        }else{
            asciiSortMap.put("refund_fee", MoonUtil.mathematical(howMuch,"*",100,0).intValue());
        }

        //写入订单总价
        asciiSortMap.put("total_fee",MoonUtil.mathematical(orderForm.getOrderFormFee(),"*",100,0).intValue());

        //写入加密方式
        asciiSortMap.put("sign_type","MD5");

        //获取ASCII排序后的签名
        String sign = getSign(asciiSortMap);

        asciiSortMap.put("sign",sign);

        return asciiSortMap;
    }

    /**
     * 〈效验是否微信通知〉
     *
     * @param xmlMap
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:30 PM
     */
    public static boolean checkIsWeChatSign(Map<String,Object> xmlMap){
        String oldSign = xmlMap.get("sign").toString();
        logger.info("获取到的微信通知签名: "+oldSign);

        List<Map.Entry<String, Object>> infoIds = new ArrayList<Map.Entry<String, Object>>(xmlMap.entrySet());
        // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
        Collections.sort(infoIds, new Comparator<Map.Entry<String, Object>>() {

            public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> item : infoIds) {
            if (item.getKey() != null || item.getKey() != "") {
                if (!item.getKey().equals("sign")) {
                    String key = item.getKey();
                    String val = item.getValue().toString();
                    if (!(val == "" || val == null)) {
                        sb.append(key + "=" + val + "&");
                    }
                }
            }
        }
        sb.append("key="+wechat.getWeChatApiKey());
        String sign = DigestUtils.md5Hex(sb.toString()).toUpperCase();
        logger.info("系统计算到的签名结果"+sign);
        if (oldSign.equals(sign)){
            return true;
        }
        return false;
    }

    /**
     * 〈解密微信通知参数〉
     *
     * @param refundMoneyXmlMap
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:31 PM
     */
    public static Map<String,Object> decryptWeChatRefundMoneyXmlMap(Map<String,Object> refundMoneyXmlMap){
        try {
            //验证xml
            String returnCode = refundMoneyXmlMap.get("return_code").toString();
            if (returnCode.equals("FAIL")) {
                logger.info("微信退款通信失败: " + refundMoneyXmlMap.get("return_msg").toString());
                return null;
            }

            String reqInfo = refundMoneyXmlMap.get("req_info").toString();
            byte[] decode = Base64.getDecoder().decode(reqInfo.getBytes("UTF-8"));
            byte[] md5Key = DigestUtils.md5Hex(wechat.getWeChatApiKey()).toLowerCase().getBytes();
            SecretKeySpec secretKey = new SecretKeySpec(md5Key, "AES");
            Security.addProvider(new BouncyCastleProvider());
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding","BC");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            Map<String,Object> decryptWeChatRefundMoneyXmlMap = xmlToMap(new String(cipher.doFinal(decode)));
            logger.info("微信支付退款结果解密成功: "+decryptWeChatRefundMoneyXmlMap.toString());
            return decryptWeChatRefundMoneyXmlMap;
        }catch (Exception e){
            logger.info("参数解密失败!"+e.getCause());
            return null;
        }
    }

    /**
     * 〈获取yyyyMMddHHmmss格式的时间字符串〉
     *
     * @param date
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:31 PM
     */
    public static String getNowTimeYMDHSM(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        return sdf.format(date);
    }

    /**
     * 〈获取与微信服务器通信的sign签名参数〉
     *
     * @param asciiSortMap
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:31 PM
     */
    private static String getSign(Map<String,Object> asciiSortMap){
        String sign = "";
        if (asciiSortMap!=null&&!asciiSortMap.isEmpty()){
            StringBuffer stringBuffer = new StringBuffer();
            for (Map.Entry<String, Object> s : asciiSortMap.entrySet()) {
                String key = s.getKey();
                Object value = s.getValue();

                if (value==null||StringUtils.isBlank(value.toString())) {
                    //过滤空值
                    continue;
                }
                //拼接
                stringBuffer.append(MoonUtil.humpToUnderline(key)).append("=").append(value.toString()).append("&");
            }
            if (!asciiSortMap.isEmpty()) {
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            }
            logger.info("拼接后的字符串: "+stringBuffer.toString());

            //拼接商户秘钥
            stringBuffer.append("&key="+wechat.getWeChatApiKey());
            sign = DigestUtils.md5Hex(stringBuffer.toString()).toUpperCase();
            logger.info("拼接key后的加密结果: "+sign);

        }
        return sign;
    }

    /**
     * 〈获取前端调起JSAPI微信支付的sign签名参数〉
     *
     * @param asciiSortMap
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:33 PM
     */
    private static String getPaySign(Map<String,Object> asciiSortMap){
        String sign = "";
        if (asciiSortMap!=null&&!asciiSortMap.isEmpty()){
            StringBuffer stringBuffer = new StringBuffer();
            for (Map.Entry<String, Object> s : asciiSortMap.entrySet()) {
                String key = s.getKey();
                Object value = s.getValue();

                if (value==null||StringUtils.isBlank(value.toString())) {
                    //过滤空值
                    continue;
                }
                //拼接
                stringBuffer.append(key).append("=").append(value.toString()).append("&");
            }
            if (!asciiSortMap.isEmpty()) {
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            }
            logger.info("拼接后的支付字符串: "+stringBuffer.toString());

            //拼接商户秘钥
            stringBuffer.append("&key="+wechat.getWeChatApiKey());
            sign = DigestUtils.md5Hex(stringBuffer.toString()).toUpperCase();
            logger.info("拼接key后的支付字符串加密结果: "+sign);

        }
        return sign;
    }

    /**
     * 〈转换xml为Map〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 4:38 PM
     */
    public static Map<String,Object> xmlToMap(String xml) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //将xml转为dom对象
            Document doc = DocumentHelper.parseText(xml);
            //获取根节点
            Element root = doc.getRootElement();
            //遍历子元素
            List<Element> elements = root.elements();
            for (Object obj : elements) {
                root = (Element) obj;
                map.put(root.getName(), root.getTextTrim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }


    /**
     * 〈获取RSA公钥签名〉
     *
     * @param asciiSortMap
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/12 2:36 AM
     */
    private static String getRsaPemSign(Map<String,Object> asciiSortMap){
        String sign = "";
        if (asciiSortMap!=null&&!asciiSortMap.isEmpty()){
            StringBuffer stringBuffer = new StringBuffer();
            for (Map.Entry<String, Object> s : asciiSortMap.entrySet()) {
                String key = s.getKey();
                Object value = s.getValue();

                if (value==null||StringUtils.isBlank(value.toString())) {
                    //过滤空值
                    continue;
                }
                //拼接
                stringBuffer.append(MoonUtil.humpToUnderline(key)).append("=").append(value.toString()).append("&");
            }
            if (!asciiSortMap.isEmpty()) {
                stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            }
            logger.info("拼接后的字符串: "+stringBuffer.toString());

            //拼接商户秘钥
            stringBuffer.append("&key="+wechat.getWeChatApiKey());
            sign = DigestUtils.md5Hex(stringBuffer.toString()).toUpperCase();
            logger.info("拼接key后的加密结果: "+sign);
        }
        return sign;
    }

    /**
     * 〈保存RSA公钥到本地〉
     *
     * @param pubKey
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/12 2:36 AM
     */
    private static PublicKey savePubKeyToLocal(String pubKey) {
        logger.info("获取到微信服务器返回的RSA公钥: ");
        logger.info("\r"+pubKey);
        try {
            String classPath = ClassUtils.getDefaultClassLoader().getResource("").getPath();
            logger.info("RSA公钥保存路径: "+classPath);
            File file = new File(classPath + "public.pem");
            try {
                BufferedWriter writer = Files.newBufferedWriter(file.toPath());
                writer.write(pubKey);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //如果保存成功
            if (file.exists()&&file.isFile()) {
                readLocalPubKey();
                // loadPublicKey();
            }
            // RSAPublicKey rsaPublicKey =  RSAPublicKey.getInstance(decoder.decode(readLocalPubKey()));
            //
            // RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
            //
            // KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            //
            // PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            //
            // logger.info("PKCS#1转换PKCS#8后的公钥"+publicKey);
            // }
        }catch (Exception e){
            e.printStackTrace();
            logger.info("生成RSA公钥出错");
        }
        return null;
    }

    /**
     * 〈读取文件的RSA公钥〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/12 2:36 AM
     */
    public static PublicKey readLocalPubKey(){
        // if (StringUtils.isBlank(pubKey)) {
        try {
            File file = new File(ClassUtils.getDefaultClassLoader().getResource("").getPath() + "public.pem");
            if(file.exists()) {
                List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                StringBuilder sb = new StringBuilder();
                for (String line : lines) {
                    if (line.charAt(0) == '-') {
                        continue;
                    } else {
                        sb.append(line);
                        sb.append('\r');
                    }
                }
                // RSAPublicKey rsaPublicKey =  RSAPublicKey.getInstance(decoder.decode(sb.toString().getBytes("UTF-8")));
                //
                // RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
                //
                // KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                // byte[] buffer = decoder.decode(sb.toString().getBytes());
                // KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                // X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(buffer);
                // return keyFactory.generatePublic(publicKeySpec);
                return loadPublicKey(sb.toString());
            }else{
                getRsaPublicPem();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("读取公钥时出错,错误信息: " + e.getCause());
        }
        // }
        return null;
    }

    /**
     * 从字符串中加载公钥
     *
     * @param publicKeyStr 公钥数据字符串
     * @throws Exception 加载公钥时产生的异常
     */
    public static PublicKey loadPublicKey(String publicKeyStr) {
        //获取到PSCK#1的公钥
        logger.info("公钥字符串长度: "+publicKeyStr.length());
        //转成PSCK#8的公钥
        try {
            byte[] buffer = org.apache.commons.codec.binary.Base64.decodeBase64(publicKeyStr);
            RSAPublicKey rsaPublicKey = RSAPublicKey.getInstance(buffer);
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            // X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            // printPublicKeyInfo(keyFactory.generatePublic(keySpec));
            // return keyFactory.generatePublic(keySpec);
            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            logger.info("初始化公钥失败: "+e.getCause());
        }
        return null;
    }

    /**
     * 从字符串中加载公钥
     *
     */
    // public static PublicKey loadPublicKey(){
    //     logger.info("得到的公钥: ");
    //     logger.info(pubKey);
    //     try {
    //         byte[] buffer = decoder.decode(pubKey);
    //         KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    //         X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
    //         printPublicKeyInfo(keyFactory.generatePublic(keySpec));
    //         return keyFactory.generatePublic(keySpec);
    //     } catch (NoSuchAlgorithmException e) {
    //         logger.info("无此算法");
    //     } catch (InvalidKeySpecException e) {
    //         logger.info("公钥非法");
    //     } catch (NullPointerException e) {
    //         logger.info("公钥数据为空");
    //     }
    //     return null;
    // }

    /**
     * 打印公钥信息
     *
     * @param publicKey
     */
    public static void printPublicKeyInfo(PublicKey publicKey) {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        logger.info("----------RSAPublicKey----------");
        logger.info("Modulus.length=" + rsaPublicKey.getModulus().bitLength());
        logger.info("Modulus=" + rsaPublicKey.getModulus().toString());
        logger.info("PublicExponent.length=" + rsaPublicKey.getPublicExponent().bitLength());
        logger.info("PublicExponent=" + rsaPublicKey.getPublicExponent().toString());
    }

    /**
     * 用RSA公钥加密 <br>
     * 每次加密的字节数，不能超过密钥的长度值减去11
     * @param plainBytes 需加密数据的byte数据
     * @return 加密后的byte型数据
     */
    public static String encryptRSA(byte[] plainBytes,PublicKey publicKey){
        int keyByteSize = 2048 / 8;
        int encryptBlockSize = keyByteSize - 11;
        int nBlock = plainBytes.length / encryptBlockSize;
        if ((plainBytes.length % encryptBlockSize) != 0) {
            nBlock += 1;
        }
        ByteArrayOutputStream outbuf = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-1ANDMGF1PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            outbuf = new ByteArrayOutputStream(nBlock * keyByteSize);
            for (int offset = 0; offset < plainBytes.length; offset += encryptBlockSize) {
                int inputLen = plainBytes.length - offset;
                if (inputLen > encryptBlockSize) {
                    inputLen = encryptBlockSize;
                }
                byte[] encryptedBlock = cipher.doFinal(plainBytes, offset, inputLen);
                outbuf.write(encryptedBlock);
            }
            outbuf.flush();
            byte[] encryptedData = outbuf.toByteArray();
            return new String(encoder.encode(encryptedData));
        } catch (Exception e) {
            logger.info("RSA公钥加密错误:", e.getCause());
            return "";
        } finally {
            try {
                if (outbuf != null) {
                    outbuf.close();
                }
            } catch (Exception e) {
                logger.info("关闭输出流错误,错误原因:", e.getCause());
            }
        }
    }

    /**
     * 〈生成微信支付XML字符串〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 4:38 PM
     */
    public static String getWeChatPayXmlStr(Map<String,Object> xmlMap) {
        StringBuffer xml = new StringBuffer();
        xml.append("<xml>");
        for (Map.Entry entry: xmlMap.entrySet()){
            // if (!entry.getKey().equals("createTime")){
            xml.append("<"+entry.getKey()+">"+entry.getValue()+"</"+entry.getKey()+">");
            // }else {
            //     xml.append("<" + entry.getKey() + "><![CDATA[" + entry.getValue() + "]]></" + entry.getKey() + ">");
            // }
        }
        xml.append("</xml>");
        try {
            return new String(xml.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 〈转换微信返回的xmlMap为字符串〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2018/10/26 4:38 PM
     */
    public static Map<String,Object> weChatXmlToString(String xml) {
        Map<String,Object> xmlMap = new TreeMap<>();
        Document doc = null;
        try {
            //将字符串转成XML对象
            doc = DocumentHelper.parseText(xml);
            //获取根节点
            Element rootElt = doc.getRootElement();
            //获取根节点下的所有节点
            List<Element> list = rootElt.elements();
            //遍历节点
            for (Element element :list) {
                xmlMap.put(element.getName(),element.getText());
            }
            return xmlMap;
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 〈获取前端调起JSAPI微信支付的参数〉
     *
     * @param xmlMap
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:34 PM
     */
    public static Map<String,Object> getJSAPIPayData(Map<String,Object> xmlMap){
        Map<String,Object> waitSignMap = new TreeMap<>();
        waitSignMap.put("appId",xmlMap.get("appid"));
        waitSignMap.put("timeStamp",getNowTimeYMDHSM(new Date()));
        waitSignMap.put("nonceStr",xmlMap.get("nonce_str"));
        waitSignMap.put("package","prepay_id="+xmlMap.get("prepay_id"));
        waitSignMap.put("signType","MD5");
        String sign = getPaySign(waitSignMap);
        waitSignMap.put("paySign",sign);
        return waitSignMap;
    }

    /**
     * 〈判断小程序登录中的rawData参数是否与signature一致〉
     *
     * @param rawData,sessionKey,signature
     * @return: boolean
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/21 12:56 PM
     */
    public static boolean signatureIsValid(String rawData,String sessionKey,String signature){
        if (StringUtils.isNotBlank(rawData)
                &&StringUtils.isNotBlank(sessionKey)
                &&StringUtils.isNotBlank(signature)){
            try {
                //调整传入的rawData
                JSONObject rawDataBean = JSONObject.parseObject(rawData);
                Map<String,Object> fixRawData = new LinkedMap<>();
                fixRawData.put("nickName",rawDataBean.getString("nickName"));
                fixRawData.put("gender",rawDataBean.getInteger("gender"));
                fixRawData.put("language",rawDataBean.getString("language"));
                fixRawData.put("city",rawDataBean.getString("city"));
                fixRawData.put("province",rawDataBean.getString("province"));
                fixRawData.put("country",rawDataBean.getString("country"));
                fixRawData.put("avatarUrl",rawDataBean.getString("avatarUrl"));
                String shaSignature = DigestUtils.sha1Hex(JSON.toJSONString(fixRawData)+ sessionKey);
                if (!signature.equals(shaSignature)){
                    return false;
                }
            }catch (Exception e){
                logger.info("字符串转换Json对象出错,原字符串: "+rawData+" ,错误原因: "+e.getCause());
                return false;
            }
        }
        return true;
    }

    /**
     * 〈获取微信系统token〉
     *
     * @param
     * @return: String
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/21 12:56 PM
     */
    public static String getWeChatToken(){
        //获取当前时间时间戳
        long nowTime = new Date().getTime();
        //获取微信token过期时间时间戳
        Long expiresTime = wechat.getWeChatTokenExpiresTime();
        //如果过期时间是默认值,或者比当前时间的时间戳值要小,则重新请求微信服务器获取token
        if (expiresTime==0||expiresTime<nowTime){
            //设置请求url
            String requestUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+wechat.getWeChatAppId()+"&secret="+wechat.getWeChatAppSecret()+"";
            //发送请求,获得返回的json数据
            JSONObject result = MoonUtil.httpClientGet(requestUrl);
            //获取返回数据中的access_token字符串
            String access_token = result.getString("access_token");
            //如果正确返回则替换掉静态变量中的token和过期时间,并返回token
            if (StringUtils.isNotBlank(access_token)) {
                wechat.setWeChatToken(access_token);
                wechat.setWeChatTokenExpiresTime(nowTime + result.getLong("expires_in"));
                return wechat.getWeChatToken();
            }else{
                //如果微信服务器返回不正确则返回空
                return "";
            }
        }else{
            //如果token没过期且获取过,则直接返回静态变量中token
            return wechat.getWeChatToken();
        }
    }

    /**
     * 〈生成小程序二维码〉
     *
     * @param page,scene,type
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/21 12:56 PM
     */
    public static String generateQRCode(String page,String scene,String type){
        String requestUrl = "";
        JSONObject requestBean = new JSONObject();
        String weChatToken = getWeChatToken();
        if (StringUtils.isBlank(weChatToken)){
            logger.info("获取微信token失败: "+requestUrl);
            return "";
        }
        try {
            if (type.equals("永久")) {
                requestUrl = "https://api.weixin.qq.com/wxa/getwxacodeunlimit?access_token=" + weChatToken;

                logger.info("生成二维码的URL: "+requestUrl);
                if (StringUtils.isBlank(page)) {
                    page = "";
                }
                requestBean.put("page", page);
                if (StringUtils.isBlank(scene)){
                    return null;
                }
                requestBean.put("scene", scene);
            }
            if (type.equals("临时")){
                requestUrl = "https://api.weixin.qq.com/cgi-bin/wxaapp/createwxaqrcode?access_token=" + weChatToken;
                if (StringUtils.isBlank(page)){
                    return null;
                }
                requestBean.put("path",page);
            }
            InputStream inputStream = HttpUtil.httpJsonPostReturnInputStream(requestUrl, requestBean);
            if (inputStream==null){
                return null;
            }
            return FileUtil.inputStreamToImage(inputStream,UUID.randomUUID().toString().replaceAll("-","")+".jpg");
        }catch (Exception e){
            logger.info("生成二维码异常: " + e.getCause());
            return null;
        }
    }

    /**
     * 〈通用回复微信服务器处理成功的xml字符串〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:35 PM
     */
    public static String getWeChatPaySuccessXml(){
        logger.info("微信支付回调后续操作成功,已回复微信服务器");
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    /**
     * 〈通用回复微信服务器处理失败的xml字符串〉
     *
     * @param msg
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:35 PM
     */
    public static String getWeChatPayFailXml(String msg){
        try {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }catch (Exception e){
            logger.info("回滚事务异常: "+e.getCause());
        }
        logger.info("微信支付回调后续操作失败,已回复微信服务器: " + msg);
        return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA["+msg+"]]></return_msg></xml>";
    }

    /**
     * 〈是否系统对应的微信小程序〉
     *
     * @param appId
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/2/21 12:56 PM
     */
    public static boolean isSystemMiniProgram(String appId){
        if (appId.equals(wechat.getWeChatAppId())){
            return true;
        }
        return false;
    }

    /**
     * 加密
     * @param encryptData 加密的字符串
     * @param encryptKey key值
     * @return
     * @throws Exception
     */
    public static String encrypt(String encryptData, String encryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(), "AES"));
        byte[] b = cipher.doFinal(encryptData.getBytes("utf-8"));
        // 采用base64算法进行转码,避免出现中文乱码
        return encoder.encodeToString(b);

    }

    /**
     * 解密
     * @param encryptStr 解密的字符串
     * @param decryptKey 解密的key值
     * @return
     * @throws Exception
     */
    public static String decrypt(String encryptStr, String decryptKey) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(), "AES"));
        // 采用base64算法进行转码,避免出现中文乱码
        byte[] encryptBytes = decoder.decode(encryptStr);
        byte[] decryptBytes = cipher.doFinal(encryptBytes);
        return new String(decryptBytes);
    }


    /**
     * 〈获取微信支付企业付款银行名称〉
     *
     * @param bankName
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 9:08 PM
     */
    public static Integer getBankCode(String bankName){
        initBankMap();
        return bankMap.get(bankName);
    }

    /**
     * 〈获取微信支付企业付款银行编号〉
     *
     * @param bankCode
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 9:08 PM
     */
    public static String getBankName(int bankCode){
        initBankMap();
        for (Map.Entry entry :bankMap.entrySet()) {
            if(Integer.parseInt(entry.getValue().toString()) == bankCode){
                return entry.getKey().toString();
            }
        }
        return "";
    }

    /**
     * 〈获取微信支付企业付款支持的银行〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 9:08 PM
     */
    public static Map getAllBank(){
        initBankMap();
        return bankMap;
    }

    /**
     * 〈获取微信RSA公钥〉
     *
     * @param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/5/11 11:27 PM
     */
    public static PublicKey getRsaPublicPem() {
        Map<String, Object> asciiSortMap = new TreeMap<>();

        //写入商户ID
        asciiSortMap.put("mch_id",wechat.getWeChatMchId());
        //写入随机字符串
        asciiSortMap.put("nonce_str",CustomizeUUIDGenerate.generateBase64UUID(UUID.randomUUID().toString()));
        //写入加密方式
        asciiSortMap.put("sign_type","MD5");
        //获取ASCII排序后的签名
        String sign = getRsaPemSign(asciiSortMap);
        asciiSortMap.put("sign",sign);
        try {
            //获取要发送给微信服务器RSA公钥接口的xml格式的字符串
            String weChatPemXmlStr = WeChatUtil.getWeChatPayXmlStr(asciiSortMap);
            logger.info("拼接后的xml字符串: "+weChatPemXmlStr);
            //获取RSA公钥需要使用SSL证书,HTTPS协议
            String xmlPost = HttpUtil.httpXmlPostSSL(wechat.getWeChatRsaUrl(), weChatPemXmlStr);
            logger.info("获取微信RSA公钥结果: ");
            logger.info("\n"+xmlPost);
            //将微信系统返回的xml格式化成Map集合
            Map<String, Object> weChatXmlMap = WeChatUtil.weChatXmlToString(xmlPost);
            if (weChatXmlMap == null) {
                logger.info("微信支付获取RSA公钥返回xml解析错误,请联系管理员");
                return null;
            }
            //微信服务器返回的通信标识
            if (!weChatXmlMap.get("return_code").equals("SUCCESS")) {
                logger.info("微信支付获取RSA公钥请求失败,失败原因: "+weChatXmlMap.get("return_msg"));
                return null;
            }
            //微信服务器返回的业务标识
            if (!weChatXmlMap.get("result_code").equals("SUCCESS")) {
                logger.info("微信支付获取RSA公钥请求失败,错误码: "+weChatXmlMap.get("err_code")+",错误信息: "+weChatXmlMap.get("err_code_des"));
                return null;
            }

            if (!weChatXmlMap.get("mch_id").toString().equals(wechat.getWeChatMchId())){
                logger.info("微信支付获取RSA公钥请求失败,商户ID不一致");
                return null;
            }
            return savePubKeyToLocal(weChatXmlMap.get("pub_key").toString());
        }catch (Exception e){
            e.printStackTrace();
            logger.info("获取微信RSA公钥异常: "+e.getCause());
            return null;
        }
    }


    public static String getWeChatPayUnifiedOrderUrl(){
        return wechat.getWeChatPayUnifiedOrderUrl();
    }

    public static String getWeChatPayRefundUrl(){
        return wechat.getWeChatPayRefundUrl();
    }

    public static String getWeChatCorporatePayUrl(){
        return wechat.getWeChatCorporatePayUrl();
    }

    public static String getWeChatCorporatePayNotifyUrl(){
        return wechat.getWeChatCorporatePayNotifyUrl();
    }

    public static String getWeChatCorporatePayBankUrl(){

        return wechat.getWeChatCorporatePayBankUrl();
    }

    public static String getWeChatCorporatePayBankNotifyUrl(){
        return wechat.getWeChatCorporatePayBankNotifyUrl();
    }


    public static String getWeChatMchId(){
        return wechat.getWeChatMchId();
    }

    public static String getWeChatAppId(){
        return wechat.getWeChatAppId();
    }
}
