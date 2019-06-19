package com.revisit.springboot.repository.partyinfo;
import com.revisit.springboot.entity.partyinfo.PartyInfo;

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
* PartyInfo持久层接口类
* @author Revisit-Zhang
* @date 2019-06-19 11:54:16
*/
@Transactional
public interface PartyInfoRepository extends JpaRepository<PartyInfo,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from PartyInfo partyInfo where partyInfo.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);
}