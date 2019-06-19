package com.revisit.springboot.repository.orderform;
import com.revisit.springboot.entity.orderform.OrderForm;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
* OrderForm持久层接口类
* @author Revisit-Moon
* @date 2019-03-01 15:58:42
*/
public interface OrderFormRepository extends JpaRepository<OrderForm,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from OrderForm orderForm where orderForm.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Query("FROM OrderForm WHERE userId = :userId AND productData = :productData AND status='待付款'")
    OrderForm findByUserIdAndProductData(@Param("userId") String userId,
                                         @Param("productData") String productData);
    
    OrderForm findByThirdPartyOrderNumber(String thirdPartyOrderNumber);

}