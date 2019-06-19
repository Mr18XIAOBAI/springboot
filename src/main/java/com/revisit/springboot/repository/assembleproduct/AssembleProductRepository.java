package com.revisit.springboot.repository.assembleproduct;

import com.revisit.springboot.entity.assembleproduct.AssembleProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
* AssembleProduct持久层接口类
* @author Revisit-Moon
* @date 2019-05-04 22:45:06
*/
@Transactional(rollbackFor = { Exception.class })
public interface AssembleProductRepository extends JpaRepository<AssembleProduct,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from AssembleProduct assembleProduct where assembleProduct.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Modifying
    @Query(value = "UPDATE assemble_product AS p SET " +
            "p.total_views = (SELECT sum(s.sku_click_rate) FROM assemble_product_sku AS s WHERE s.assemble_product_id = p.id)," +
            "p.total_sales = (SELECT sum(s.sku_sales) FROM assemble_product_sku AS s WHERE s.assemble_product_id = p.id)",nativeQuery = true)
    int updateTotalViewAndSales();

    //根据关联ID获取最大的排序号
    @Query("SELECT COALESCE(MAX(sortNumber),0) FROM AssembleProduct")
    Integer findMaxSortNumber();

    //根据排序号获取对象
    AssembleProduct findBySortNumber(int sortNumber);

    //根据排序号和关联ID把比排序号大的所有列的排序号-1
    @Modifying
    @Query(value = "UPDATE assemble_product SET sort_number = sort_number-1 WHERE sort_number > :sortNumber ",nativeQuery = true)
    int allSortNumberMinusOneBySortNumber(@Param("sortNumber") int sortNumber);


    AssembleProduct findByProductId(String productId);

    AssembleProduct findByName(String name);
}