package com.revisit.springboot.repository.assemblerelation;

import com.revisit.springboot.entity.assemblerelation.AssembleRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
* AssembleRelation持久层接口类
* @author Revisit-Moon
* @date 2019-05-06 10:22:48
*/
@Transactional(rollbackFor = { Exception.class })
public interface AssembleRelationRepository extends JpaRepository<AssembleRelation,String>,JpaSpecificationExecutor {
    @Modifying
    @Query("DELETE from AssembleRelation assembleRelation where assembleRelation.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Modifying
    @Query("DELETE from AssembleRelation assembleRelation where assembleRelation.userId in :userId")
    int deleteByUserId(@Param("userId") String userId);

    @Query(value = "SELECT ar.* FROM assemble_relation ar WHERE ar.user_id = :userId AND ar.assemble_product_id = :assembleProductId AND status = '等待成团' ",nativeQuery = true)
    AssembleRelation findByUserIdAndAssembleProductIdAndStatusIsWait(@Param("userId") String userId, @Param("assembleProductId") String assembleProductId);

    List<AssembleRelation> findByTeamId(String teamId);

    @Query(value = "SELECT COUNT(DISTINCT(ar.team_id)) FROM assemble_relation ar WHERE ar.status = '已成团' AND ar.assemble_product_id = :assembleProductId ",nativeQuery = true)
    int findDoneTeamTotalByAssembleProductId(@Param("assembleProductId") String assembleProductId);

    @Query(value = "SELECT a.* FROM assemble_relation a WHERE a.team_id = (SELECT ar.team_id FROM assemble_relation ar WHERE ar.user_id = :userId AND ar.order_form_id = :orderFormId) ",nativeQuery = true)
    List<AssembleRelation> findIsDoneTeamByUserIdAndOrderFormId(@Param("userId") String userId, @Param("orderFormId") String orderFormId);

    @Query(value = "FROM AssembleRelation assembleRelation WHERE  assembleRelation.status = '等待成团' AND assembleRelation.endTime < CURRENT_TIME")
    List<AssembleRelation> findByNotDoneTeamTotal();
}