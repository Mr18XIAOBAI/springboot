/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: ShoppingCart
 * Author:   Revisit-Moon
 * Date:     2019/4/15 10:19 AM
 * Description: shoppingcart.ShoppingCart
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/4/15 10:19 AM        1.0              描述
 */

package com.revisit.springboot.entity.shoppingcart;

import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 〈shoppingcart.ShoppingCart〉
 *
 * @author Revisit-Moon
 * @create 2019/4/15
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "shopping_cart_item")
public class ShoppingCartItem extends BasicEntity {

    @Column(nullable = false)
    private String shoppingCartId;                      //所属购物车ID

    private String shopId;                              //商品sku所属店铺ID
    private String productId;                           //商品sku所属商品ID

    @Column(nullable = false)
    private String productSkuId;                        //skuId

    private int quantity;                               //数量

    private String productSkuName;                      //sku名
    private String productSkuIcon;                      //sku主图
    private String productSkuAlbum;                     //sku相册
    private String productSkuVideo;                     //sku视频
    private boolean productSkuOnline;                   //sku在线状态

    private BigDecimal price;                           //总价
}
