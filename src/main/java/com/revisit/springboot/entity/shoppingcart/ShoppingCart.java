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

/**
 * 〈shoppingcart.ShoppingCart〉
 *
 * @author Revisit-Moon
 * @create 2019/4/15
 * @since 1.0.0
 */
@Data
@Entity
@Table(name = "shopping_cart")
public class ShoppingCart extends BasicEntity {

    @Column(nullable = false)
    private String userId;                                   //购物车所属用户ID

    private int totalQuantity;                               //购物车内总数量
}
