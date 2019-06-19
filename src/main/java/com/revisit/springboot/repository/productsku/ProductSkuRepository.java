package com.revisit.springboot.repository.productsku;

import com.revisit.springboot.entity.productsku.ProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* ProductSku持久层接口类
* @author Revisit-Moon
* @date 2019-03-03 15:28:24
*/
@Transactional(rollbackFor = { Exception.class })
public interface ProductSkuRepository extends JpaRepository<ProductSku,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from ProductSku productSku where productSku.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Modifying
    @Query("DELETE from ProductSku productSku where productSku.productId in :productIds")
    int deleteByProductIds(@Param("productIds") List<String> productIds);

    //根据关联ID获取最大的排序号
    @Query("SELECT COALESCE(MAX(sortNumber),0) FROM ProductSku WHERE productId = :productId")
    Integer findMaxSortNumberByProductId(@Param("productId") String productId);

    //根据排序号获取对象
    ProductSku findByProductIdAndSortNumber(String productId, int sortNumber);

    //根据sku名称获取对象
    ProductSku findBySkuName(String skuName);

    //根据排序号和关联ID把比排序号大的所有列的排序号-1
    @Modifying
    @Query(value = "UPDATE product_sku SET sort_number = sort_number-1 WHERE sort_number > :sortNumber AND product_id = :relationId",nativeQuery = true)
    int allSortNumberMinusOneBySortNumberAndRelationId(@Param("sortNumber") int sortNumber, @Param("relationId") String relationId);

    //根据商品ID获取商品sku列表
    List<ProductSku> findByProductId(String productId);

    //根据店铺ID删除商品sku
    @Modifying
    @Query("DELETE from ProductSku productSku WHERE productSku.productId IN (SELECT P.id FROM Product P WHERE P.shopId IN :shopId)")
    int deleteByProductSkuShopId(@Param("shopId") List<String> shopId);

    //根据ID审核商品sku
    @Modifying
    @Query("UPDATE ProductSku productSku SET productSku.online = :confirm where productSku.productId IN :ids")
    int confirmProductSkuByProductIds(@Param("ids") List<String> ids,@Param("confirm") boolean confirm);

    //根据ID审核商品sku
    @Modifying
    @Query("UPDATE ProductSku productSku SET productSku.online = :confirm where productSku.id = :id")
    int confirmProductSkuById(@Param("id") String id,@Param("confirm") boolean confirm);
}