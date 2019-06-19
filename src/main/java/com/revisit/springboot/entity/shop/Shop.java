/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: Shop
 * Author:   Revisit-Moon
 * Date:     2019-06-08 12:21
 * Description: Shop
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019-06-08 12:21        1.0              描述
 */

package com.revisit.springboot.entity.shop;

import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 〈店铺实体〉
 *
 * @author Revisit-Moon
 * @create 2019-06-08
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "shop")
public class Shop extends BasicEntity {
    private String userId;                                      //所属用户ID
    private String userRealName;                                //所属用户名称
    private String mobile;                                      //所属用户手机
    private String address;                                     //店铺地址
    private String name;                                        //店铺名称
    private String info;                                        //店铺介绍
    private String icon;                                        //店铺图标
    private String backgroundImage;                             //背景图片
    private String album;                                       //相册
    private String remark;                                      //备注
    private BigDecimal price;                                   //店铺金额
}
