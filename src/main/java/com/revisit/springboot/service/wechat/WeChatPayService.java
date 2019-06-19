/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: WeChatPayService
 * Author:   Revisit-Moon
 * Date:     2019/2/21 11:40 AM
 * Description: WeChatPayService
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/21 11:40 AM        1.0              描述
 */

package com.revisit.springboot.service.wechat;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.orderform.OrderForm;

import java.math.BigDecimal;

/**
 * 〈WeChatPayService〉
 *
 * @author Revisit-Moon
 * @create 2019/2/21
 * @since 1.0.0
 */

public interface WeChatPayService {
    //微信支付统一下单
    JSONObject weChatPay(OrderForm orderForm);

    //微信支付结果回调
    String weChatPayNotify(String weChatXml);

    //微信支付申请退款
    JSONObject weChatPayRefundMoney(OrderForm orderForm,BigDecimal howMuch);

    //微信支付申请退款结果回调
    String weChatRefundMoneyNotify(String weChatXml);

}
