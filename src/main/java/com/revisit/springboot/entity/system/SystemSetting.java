/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: SystemSetting
 * Author:   Revisit-Moon
 * Date:     2019/1/31 11:03 AM
 * Description: system.SystemSetting
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/31 11:03 AM        1.0              描述
 */

package com.revisit.springboot.entity.system;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 〈system.SystemSetting〉
 *
 * @author Revisit-Moon
 * @create 2019/1/31
 * @since 1.0.0
 */
@Entity
@Table(name = "system_setting")
@Data
public class SystemSetting {

    @Id
    @Column(name = "id", unique = true, nullable = false, length = 32)
    @GenericGenerator(name = "system-uuid", strategy = "com.revisit.springboot.component.uuid.CustomizeUUIDGenerate" )
    @GeneratedValue(generator = "system-uuid")
    private String id;                                                      //主键

    @Column(nullable = false)
    private long tokenValid = 604800000;                                    //token有限时间(毫秒)

    private String unifiedExtractionAddress;                                //统一提货地址

    private BigDecimal systemDeliveryFee = new BigDecimal(0);           //系统运费

    private int autoConfirmOrderFormDay = 7;                                //自动确认收货天数

    private String systemBankCarNumber;                                     //系统提现银行卡号

    private String systemBankCarUserName;                                   //系统提现银行卡户名

    private String systemBankName;                                          //系统提现银行名

    private boolean needWithdrawConfirm = true;                             //提现需要审核

    private String icon;                                                    //小程序图标

    private String slogan;                                                  //小程序标语

    private String officialWeChat;                                          //官方微信

    private String officialWebsite;                                         //官方网站

    private String customerServicePhone;                                    //客服电话

    private String companyMail;                                             //公司邮箱


}
