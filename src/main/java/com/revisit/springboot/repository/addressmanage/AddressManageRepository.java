package com.revisit.springboot.repository.addressmanage;

import com.revisit.springboot.entity.addressmanage.AddressManage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* AddressManage持久层接口类
* @author Revisit-Moon
* @date 2019-04-15 21:11:34
*/
@Transactional(rollbackFor = { Exception.class })
public interface AddressManageRepository extends JpaRepository<AddressManage,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from AddressManage addressManage where addressManage.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Modifying
    @Query("DELETE from AddressManage addressManage where addressManage.userId in :userId")
    int deleteByUserId(@Param("userId") String userId);

    AddressManage findByIdAndUserId(String id, String userId);

    List<AddressManage> findByUserId(String userId);

    @Query(value = "SELECT COUNT(id) FROM address_manage WHERE user_id = :userId AND is_default = false",nativeQuery = true)
    int findDefaultIsALLFalse(@Param("userId") String userId);

    @Modifying
    @Query(value = "UPDATE address_manage SET is_default = FALSE WHERE user_id = :userId",nativeQuery = true)
    int fixAddressManageDefaultIsFalseByUserId(@Param("userId") String userId);

    @Modifying
    @Query(value = "UPDATE address_manage SET is_default = TRUE WHERE id = :id",nativeQuery = true)
    int fixAddressManageDefaultIsTrueById(@Param("id") String id);

    @Query(value = "SELECT addressManage.id FROM address_manage addressManage " +
            "WHERE addressManage.user_id = :userId " +
            "AND addressManage.create_time = (SELECT MAX(am.create_time) FROM address_manage am WHERE am.user_id = :userId)",nativeQuery = true)
    String findAddressManagerByUserIdAndMaxCreateTime(@Param("userId") String userId);
}