package com.revisit.springboot.repository.shop;
import com.revisit.springboot.entity.shop.Shop;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
/**
* Shop持久层接口类
* @author Revisit-Moon
* @date 2019-06-10 16:14:48
*/
@Transactional
public interface ShopRepository extends JpaRepository<Shop,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from Shop shop where shop.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    Shop findByUserId(String userId);

    Shop findByName(String name);
}