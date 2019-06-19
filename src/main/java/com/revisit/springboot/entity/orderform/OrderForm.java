/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: OrderForm
 * Author:   Revisit-Moon
 * Date:     2019/2/22 9:48 AM
 * Description: OrderForm
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/22 9:48 AM        1.0              描述
 */

package com.revisit.springboot.entity.orderform;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 〈OrderForm〉
 *
 * @author Revisit-Moon
 * @create 2019/2/22
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "order_form")
public class OrderForm extends BasicEntity {

    @Excel(name = "用户ID")
    @Column(length = 32)
    private String userId;                                              //用户ID

    @Column(length = 128)
    private String submitOrderFormIp;                                   //提交订单IP

    @Excel(name = "第三方订单号")
    @Column(length = 32)
    private String thirdPartyOrderNumber;                               //第三方订单号

    private String thirdRefundOrderNumber;                              //第三方退款订单号

    @Excel(name = "订单类型")
    @Column(length = 10)
    private String orderFormType;                                       //订单对应类型

    @Excel(name = "交易金额")
    private BigDecimal orderFormFee;                                    //订单交易金额

    @Excel(name = "订单详情")
    @Column(length = 128)
    private String orderFormDetail;                                     //订单详情

    @Column(length = 1024)
    private String productData = "[]";                                  //商品数据Json字符串

    private BigDecimal productFee;                                      //商品总价

    @Excel(name = "订单状态")
    @Column(length = 20)
    private String status;                                              //订单状态

    // @Excel(name = "返佣状态")
    // @Column(length = 20)
    // private String rewardStatus;                                        //奖励状态

    @Excel(name = "付款方式")
    @Column(length = 20)
    private String paymentMode;                                         //付款方式

    @Excel(name = "付款场景")
    @Column(length = 20)
    private String useScenes;                                           //使用场景(JSAPI-微信内支付|NATIVE-原生支付|APP-程序内支付|MWEB-手机网页支付)

    @Excel(name = "配送方式")
    @Column(length = 20)
    private String deliveryMode;                                        //配送方式(自提|配送)

    // @Column(length = 32)
    // private String addressId;                                           //用户地址ID

    @Excel(name = "提货地址")
    private String extractionAddress;                                   //提货地址

    @Excel(name = "提货加密字符串")
    @JSONField(serialize = false)
    private String extractionSign;                                      //提货加密签名

    @Excel(name = "收货地址")
    private String address;                                             //用户地址

    @Excel(name = "收货人")
    private String consignee;                                           //收件人

    @Excel(name = "收货电话")
    private String contactMobile;                                       //联系电话

    // @Column(length = 32)
    // private String logisticsId;                                         //物流ID

    private String logisticsCompany;                                    //物流公司

    private String logisticsNumber;                                     //物流单号

    @Lob
    private String logisticsDetail;                                     //物流详情

    private BigDecimal logisticsFee;                                    //运费

    private Integer invoiceMode;                                        //是否开发票(0-不开发票,1-纸质发票,2-电子发票)

    @Column(length = 32)
    private String invoiceUrl;                                          //电子发票地址

    private BigDecimal packageFee;                                      //订单包装费

    private String useDiscountId;                                       //使用优惠对应实体Id

    private BigDecimal discountPrice;                                   //优惠金额

    private String openId;                                              //用户openId

    @Excel(name = "订单支付时间")
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name="pay_time",length=19)
    private Date payTime;
    //支付时间
    @Excel(name = "订单发货时间")
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    @Column(name="logistics_time",length=19)
    private Date logisticsTime;                                         //发货时间

    private String shopId;                                              //店铺ID

    private String fatherId;                                            //父订单ID

    private String remark;                                              //备注

}
