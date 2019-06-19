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

import com.revisit.springboot.entity.user.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * 〈repository.UserRepository〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */
public interface AuthorityRepository extends JpaRepository<Authority,String>,JpaSpecificationExecutor {
    @Query(value = "FROM Authority authority " +
            "WHERE (authority.authorityName = :authorityName OR authority.description = :authorityName)")
    Authority findByAuthorityNameOrDescription(@Param("authorityName") String authorityName);
}
