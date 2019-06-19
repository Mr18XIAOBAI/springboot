package com.revisit.springboot.repository.productclass;

import com.revisit.springboot.entity.productclass.ProductClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
* ProductClass持久层接口类
* @author Revisit-Moon
* @date 2019-03-03 19:32:16
*/
@Transactional(rollbackFor = { Exception.class })
public interface ProductClassRepository extends JpaRepository<ProductClass,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from ProductClass productClass where productClass.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Modifying
    @Query(value = "DELETE from product_class where father_class_id = :fatherClassId",nativeQuery = true)
    int deleteByFatherClassId(@Param("fatherClassId") String fatherClassId);

    List<ProductClass> findByFatherClassId(String fatherClassId);

    @Query(value = "SELECT C.* FROM product_class C WHERE C.id IN (SELECT P.product_class_id FROM product P WHERE P.id IN (SELECT S.product_id FROM product_sku S WHERE S.id IN :productSkuIds ))",nativeQuery = true)
    List<ProductClass> findByProductSkuIds(@Param("productSkuIds") Set<String> productSkuIds);

    @Modifying
    @Query(value = "UPDATE product_class SET sort_number = sort_number -1 WHERE sort_number > :sortNumber AND father_class_id = :fatherClassId ",nativeQuery = true)
    int allSortNumberMinusOneByFatherId(@Param("sortNumber") int sortNumber, @Param("fatherClassId") String fatherClassId);

    @Modifying
    @Query(value = "UPDATE product_class SET sort_number = sort_number +1 WHERE sort_number > :sortNumber AND father_class_id = :fatherClassId ",nativeQuery = true)
    int allSortNumberPlusOneByFatherId(@Param("sortNumber") int sortNumber, @Param("fatherClassId") String fatherClassId);

    List<ProductClass> findByClassLevel(int classLevel);

    ProductClass findByClassName(String className);

    ProductClass findByFatherClassIdAndSortNumber(String fatherClassId, int sortNumber);

    @Query("SELECT MAX(sortNumber) FROM ProductClass WHERE fatherClassId = :fatherClassId")
    Integer findByClassMaxSortNumber(@Param("fatherClassId") String fatherClassId);
}