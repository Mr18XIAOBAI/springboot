package com.revisit.springboot.repository.product;

import com.revisit.springboot.entity.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
* Product持久层接口类
* @author Revisit-Moon
* @date 2019-03-01 10:00:53
*/
@Transactional(rollbackFor = { Exception.class })
public interface ProductRepository extends JpaRepository<Product,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from Product product where product.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Modifying
    @Query("DELETE from Product product where product.productClassId = :productClassId")
    int deleteByProductClassId(@Param("productClassId") String productClassId);

    Product findByProductName(String productName);

    @Query("SELECT P.shopId FROM Product P WHERE P.id = (SELECT S.productId FROM ProductSku S WHERE S.id = :productSkuId)")
    String findShopIdByProductSkuId(String productSkuId);

    @Modifying
    @Query(value = "UPDATE product AS p SET " +
            "p.product_total_views = (SELECT IFNULL(sum(s.sku_click_rate),0) FROM product_sku AS s WHERE s.product_id = p.id)," +
            "p.product_total_sales = (SELECT IFNULL(sum(s.sku_sales),0) FROM product_sku AS s WHERE s.product_id = p.id)",nativeQuery = true)
    int updateTotalViewAndSales();

    List<Product> findByProductClassId(String productClassId);

    @Query(value = "SELECT P FROM Product P WHERE P.id = (SELECT S.productId FROM ProductSku S WHERE S.id = :productSkuId )")
    Product findByProductSkuId(@Param("productSkuId") String productSkuId);

    //根据关联ID获取最大的排序号
    @Query("SELECT COALESCE(MAX(sortNumber),0) FROM Product WHERE productClassId = :productClassId")
    Integer findMaxSortNumberByProductClassId(@Param("productClassId") String productClassId);

    //根据排序号获取对象
    Product findByProductClassIdAndSortNumber(String productClassId, int sortNumber);

    //根据排序号和关联ID把比排序号大的所有列的排序号-1
    @Modifying
    @Query(value = "UPDATE product SET sort_number = sort_number-1 WHERE sort_number > :sortNumber AND product_class_id = :relationId",nativeQuery = true)
    int allSortNumberMinusOneBySortNumberAndRelationId(@Param("sortNumber") int sortNumber, @Param("relationId") String relationId);

    //根据商品ID和用户ID获取商品
    @Query(value = "FROM Product P WHERE P.id = :productId AND shopId = (SELECT S.id FROM Shop S WHERE S.userId = :userId)")
    Product findShopIdByProductIdAndUserId(@Param("productId")String productId,@Param("userId")String userId);

    //根据店铺ID删除商品
    @Modifying
    @Query("DELETE from Product product where product.shopId = :shopId")
    int deleteByProductShopId(@Param("shopId") List<String> shopId);

    //根据店铺ID获取商品
    List<Product> findByShopId(String shopId);

    //根据ID审核商品
    @Modifying
    @Query("UPDATE Product product SET product.online = :confirm where product.id IN :ids")
    int confirmProductByIds(@Param("ids") List<String> ids,@Param("confirm") boolean confirm);

    @Query("SELECT P.shopId FROM Product P WHERE P.id IN (SELECT S.productId FROM ProductSku S WHERE S.id IN :productSkuIdList)")
    Set<String> findShopIdByProductSkuId(@Param("productSkuIdList") List<String> productSkuIdList);
}