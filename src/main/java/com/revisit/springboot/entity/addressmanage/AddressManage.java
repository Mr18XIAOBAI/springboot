/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: AddressManage
 * Author:   Revisit-Moon
 * Date:     2019/4/15 4:40 PM
 * Description: addressmanage.AddressManage
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/4/15 4:40 PM        1.0              描述
 */

package com.revisit.springboot.entity.addressmanage;

import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 〈addressmanage.AddressManage〉
 *
 * @author Revisit-Moon
 * @create 2019/4/15
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "address_manage")
public class AddressManage extends BasicEntity {

    private String userId;                                              //用户ID

    private String province;                                            //省

    private String city;                                                //市

    private String district;                                            //区县

    private String address;                                             //详细地址

    private String consignee;                                           //收件人

    private String contactMobile;                                       //联系电话

    private boolean isDefault = false;                                  //是否默认
}
