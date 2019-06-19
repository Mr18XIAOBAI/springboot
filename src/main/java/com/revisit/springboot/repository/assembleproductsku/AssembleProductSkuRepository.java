/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: AssembleProductSkuRepository
 * Author:   Revisit-Moon
 * Date:     2019/5/5 11:43 AM
 * Description: assembleproductsku.AssembleProductSkuRepository
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/5/5 11:43 AM        1.0              描述
 */

package com.revisit.springboot.repository.assembleproductsku;


import com.revisit.springboot.entity.assembleproductsku.AssembleProductSku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 〈拼团商品SKU持久层接口〉
 *
 * @author Revisit-Moon
 * @create 2019/5/5
 * @since 1.0.0
 */
@Transactional(rollbackFor = { Exception.class })
public interface AssembleProductSkuRepository extends JpaRepository<AssembleProductSku,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from AssembleProductSku assembleProductSku where assembleProductSku.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Modifying
    @Query("DELETE from AssembleProductSku assembleProductSku where assembleProductSku.assembleProductId in :assembleProductProductIds")
    int deleteByAssembleProductIds(@Param("assembleProductProductIds") List<String> assembleProductProductIds);

    //根据关联ID获取最大的排序号
    @Query("SELECT COALESCE(MAX(sortNumber),0) FROM AssembleProductSku WHERE assembleProductId = :assembleProductId")
    Integer findMaxSortNumberByAssembleProductId(@Param("assembleProductId") String assembleProductId);

    //根据排序号获取对象
    AssembleProductSku findByAssembleProductIdAndSortNumber(@Param("assembleProductId") String assembleProductId, @Param("sortNumber") int sortNumber);

    //根据排序号和关联ID把比排序号大的所有列的排序号-1
    @Modifying
    @Query(value = "UPDATE assemble_product_sku SET sort_number = sort_number-1 WHERE sort_number > :sortNumber AND assemble_product_id = :relationId",nativeQuery = true)
    int allSortNumberMinusOneBySortNumberAndRelationId(@Param("sortNumber") int sortNumber, @Param("relationId") String relationId);

    List<AssembleProductSku> findByAssembleProductId(String assembleProductId);
}
