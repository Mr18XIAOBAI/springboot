/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: WeChat
 * Author:   Revisit-Moon
 * Date:     2019/2/12 3:49 PM
 * Description: wechat.WeChat
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/12 3:49 PM        1.0              描述
 */

package com.revisit.springboot.component.wechat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 〈wechat.WeChat〉
 *
 * @author Revisit-Moon
 * @create 2019/2/12
 * @since 1.0.0
 */
@Component
public class WeChat {

    @Autowired(required=false)
    private HttpServletRequest request;

    private static String weChatAppId;

    private static String weChatAppSecret;

    private static String weChatMchId;

    private static String weChatApiKey;

    private static String weChatToken;

    private static String weChatRsaUrl;

    private static String weChatPayNotifyUrl;

    private static String weChatPayRefundMoneyNotifyUrl;

    private static String weChatPayRefundUrl;

    private static String weChatPayUnifiedOrderUrl;

    private static String weChatCorporatePayUrl;

    private static String weChatCorporatePayBankUrl;

    private static String weChatCorporatePayNotifyUrl;

    private static String weChatCorporatePayBankNotifyUrl;

    private static Long weChatTokenExpiresTime = 0L;

    private static String weChatWapName;

    private static String weChatWapUrl;

    public WeChat() {
    }

    //微信AppId
    public String getWeChatAppId() {
        return weChatAppId;
    }

    @Value("${WECHAT_APPID}")
    public void setWeChatAppId(String weChatAppId) {
        WeChat.weChatAppId = weChatAppId;
    }



    //微信AppSecret
    public String getWeChatAppSecret() {
        return weChatAppSecret;
    }

    @Value("${WECHAT_SECRET}")
    public void setWeChatAppSecret(String weChatAppSecret) {
        WeChat.weChatAppSecret = weChatAppSecret;
    }

    //微信token
    public String getWeChatToken() {
        return weChatToken;
    }

    public void setWeChatToken(String weChatToken) {
        WeChat.weChatToken = weChatToken;
    }

    //微信token过期时间
    public Long getWeChatTokenExpiresTime() {
        return weChatTokenExpiresTime;
    }

    public void setWeChatTokenExpiresTime(Long weChatTokenExpiresTime) {
        WeChat.weChatTokenExpiresTime = weChatTokenExpiresTime;
    }

    //微信商户id
    public String getWeChatMchId() {
        return weChatMchId;
    }

    @Value("${WECHAT_MCH_ID}")
    public void setWeChatMchId(String weChatMchId) {
        WeChat.weChatMchId = weChatMchId;
    }

    //微信ApiKey
    public String getWeChatApiKey() {
        return weChatApiKey;
    }

    @Value("${WECHAT_API_KEY}")
    public void setWeChatApiKey(String weChatApiKey) {
        WeChat.weChatApiKey = weChatApiKey;
    }


    //微信商户id
    public String getWeChatRsaUrl() {
        return weChatRsaUrl;
    }

    @Value("${WECHAT_PAY_RSA_URL}")
    public void setWeChatRsaUrl(String weChatRsaUrl) {
        WeChat.weChatRsaUrl = weChatRsaUrl;
    }


    //微信支付统一下单地址
    public String getWeChatPayUnifiedOrderUrl() {
        return weChatPayUnifiedOrderUrl;
    }

    @Value("${WECHAT_PAY_UNIFIED_ORDER_URL}")
    public void setWeChatPayUnifiedOrderUrl(String weChatPayUnifiedOrderUrl) {
        WeChat.weChatPayUnifiedOrderUrl = weChatPayUnifiedOrderUrl;
    }

    //微信支付回调地址
    public String getWeChatPayNotifyUrl() {
        return request.getScheme()+"://"+request.getServerName()+request.getContextPath()+weChatPayNotifyUrl;
    }

    @Value("${WECHAT_PAY_NOTIFY_URL}")
    public void setWeChatPayNotifyUrl(String weChatPayNotifyUrl) {
        WeChat.weChatPayNotifyUrl = weChatPayNotifyUrl;
    }

    //微信支付企业付款地址
    public String getWeChatCorporatePayUrl() {
        return weChatCorporatePayUrl;
    }

    @Value("${WECHAT_PAY_CORPORATE_PAY_URL}")
    public void setWeChatCorporatePayUrl(String weChatCorporatePayUrl) {
        WeChat.weChatCorporatePayUrl = weChatCorporatePayUrl;
    }

    //微信支付企业付款回调地址
    public String getWeChatCorporatePayNotifyUrl() {
        return weChatCorporatePayNotifyUrl;
    }

    @Value("${WECHAT_PAY_CORPORATE_PAY_NOTIFY_URL}")
    public void setWeChatCorporatePayNotifyUrl(String weChatCorporatePayNotifyUrl) {
        WeChat.weChatCorporatePayNotifyUrl = weChatCorporatePayNotifyUrl;
    }

    //微信支付企业付款到银行地址
    public String getWeChatCorporatePayBankUrl() {
        return weChatCorporatePayBankUrl;
    }

    @Value("${WECHAT_PAY_CORPORATE_PAY_BANK_URL}")
    public void setWeChatCorporatePayBankUrl(String weChatCorporatePayBankUrl) {
        WeChat.weChatCorporatePayBankUrl = weChatCorporatePayBankUrl;
    }


    //微信支付企业付款到银行回调地址
    public String getWeChatCorporatePayBankNotifyUrl() {
        return weChatCorporatePayBankNotifyUrl;
    }

    @Value("${WECHAT_PAY_CORPORATE_PAY_BANK_NOTIFY_URL}")
    public void setWeChatCorporatePayBankNotifyUrl(String weChatCorporatePayBankNotifyUrl) {
        WeChat.weChatCorporatePayBankNotifyUrl = weChatCorporatePayBankNotifyUrl;
    }

    //微信支付申请退款地址
    public String getWeChatPayRefundUrl() {
        return weChatPayRefundUrl;
    }

    @Value("${WECHAT_PAY_REFUND_MONEY_URL}")
    public void setWeChatPayRefundUrl(String weChatPayRefundUrl) {
        WeChat.weChatPayRefundUrl = weChatPayRefundUrl;
    }

    //微信支付退款回调地址
    public String getWeChatPayRefundMoneyNotifyUrl() {
        return request.getScheme()+"://"+request.getServerName()+request.getContextPath()+weChatPayRefundMoneyNotifyUrl;
    }

    @Value("${WECHAT_PAY_REFUND_MONEY_NOTIFY_URL}")
    public void setWeChatPayRefundMoneyNotifyUrl(String weChatPayRefundMoneyNotifyUrl) {
        WeChat.weChatPayRefundMoneyNotifyUrl = weChatPayRefundMoneyNotifyUrl;
    }

    //微信支付wap名称
    public String getWeChatWapName() {
        return weChatWapName;
    }

    @Value("${WECHAT_PAY_WAP_NAME}")
    public void setWeChatWapName(String weChatWapName) {
        WeChat.weChatWapName = weChatWapName;
    }

    //微信支付wap地址
    public String getWeChatWapUrl() {
        return request.getScheme()+"://"+request.getServerName()+request.getContextPath()+weChatWapUrl;
    }

    @Value("${WECHAT_PAY_WAP_URL}")
    public void setWeChatWapUrl(String weChatWapUrl) {
        WeChat.weChatWapUrl = weChatWapUrl;
    }
}
