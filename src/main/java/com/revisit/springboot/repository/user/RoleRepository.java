/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: UserRepository
 * Author:   Revisit-Moon
 * Date:     2019/1/29 9:45 AM
 * Description: repository.UserRepository
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/29 9:45 AM        1.0              描述
 */

package com.revisit.springboot.repository.user;

import com.revisit.springboot.entity.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 〈repository.UserRepository〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */
public interface RoleRepository extends JpaRepository<Role,String>,JpaSpecificationExecutor {
    Role findByRoleName(String roleName);

    Role findByLevel(int level);

    @Query("FROM Role R WHERE R.level > :level order by R.level ASC")
    List<Role> findByNextLevel(@Param("level") int level);

    @Query("from Role where roleName = '系统管理员'")
    Role findSystemAdmin();

    @Query("from Role where roleName = '超级管理员'")
    Role findSuperAdmin();

    @Modifying
    @Query(value = "UPDATE role SET `level` = `level` + 1 WHERE `level` >= :level AND `level` < 900",nativeQuery = true)
    int allRoleLevelAddOneByLevel(@Param("level") int level);

    @Modifying
    @Query(value = "UPDATE role SET `level` = `level` - 1 WHERE `level` >= :level AND `level` < 900",nativeQuery = true)
    int allRoleLevelMinusOneByLevel(@Param("level") int level);
}
