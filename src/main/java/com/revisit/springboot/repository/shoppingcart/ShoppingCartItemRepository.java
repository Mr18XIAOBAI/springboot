package com.revisit.springboot.repository.shoppingcart;

import com.revisit.springboot.entity.shoppingcart.ShoppingCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* ShoppingCart持久层接口类
* @author Revisit-Moon
* @date 2019-04-15 11:05:45
*/
@Transactional(rollbackFor = { Exception.class })
public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from ShoppingCartItem shoppingCart where shoppingCart.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Modifying
    @Query(value = "DELETE from ShoppingCartItem shoppingCartItem where shoppingCartItem.productSkuId " +
            "in (SELECT S.id FROM ProductSku S WHERE S.productId = :productIds)")
    int deleteByProductIds(@Param("productIds") List<String> productIds);

    //获取购物车列表
    List<ShoppingCartItem> findByShoppingCartId(String shoppingCartId);

    ShoppingCartItem findByShoppingCartIdAndProductSkuId(String ShoppingCartId, String productSkuId);
}