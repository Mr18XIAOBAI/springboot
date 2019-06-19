package com.revisit.springboot.repository.shoppingcart;

import com.revisit.springboot.entity.shoppingcart.ShoppingCart;
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
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from ShoppingCart shoppingCart where shoppingCart.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Modifying
    @Query("DELETE from ShoppingCart shoppingCart where shoppingCart.userId in :userId")
    int deleteByUserId(@Param("userId") String userId);

    //获取购物车列表
    ShoppingCart findByUserId(String userId);
}