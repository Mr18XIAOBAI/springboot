package com.revisit.springboot.repository.wallet;

import com.revisit.springboot.entity.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* Wallet持久层接口类
* @author Revisit-Moon
* @date 2019-04-20 18:57:43
*/
@Transactional(rollbackFor = { Exception.class })
public interface WalletRepository extends JpaRepository<Wallet,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from Wallet wallet where wallet.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Modifying
    @Query("DELETE from Wallet wallet where wallet.userId in :userId")
    int deleteByUserId(@Param("userId") String userId);

    Wallet findByUserId(String userId);
}