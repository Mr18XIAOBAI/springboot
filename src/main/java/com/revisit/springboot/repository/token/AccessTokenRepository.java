/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: AccessTokenRepository
 * Author:   Revisit-Moon
 * Date:     2019/1/31 10:52 AM
 * Description: accesstoken.AccessTokenRepository
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/31 10:52 AM        1.0              描述
 */

package com.revisit.springboot.repository.token;

import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.entity.user.User;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 〈accesstoken.AccessTokenRepository〉
 *
 * @author Revisit-Moon
 * @create 2019/1/31
 * @since 1.0.0
 */
public interface AccessTokenRepository extends JpaRepository<AccessToken,String>,JpaSpecificationExecutor {
    AccessToken findByUserId(String userId);

    @Modifying
    @Query(value = "DELETE FROM access_token WHERE user_id NOT IN (SELECT id FROM user)", nativeQuery = true)
    Integer findNotUserToken();

    @Query(value =
            "SELECT A.* FROM `user` U " +
                    "LEFT JOIN user_role_relation URR ON U.id = URR.user_id " +
                    "LEFT JOIN role R ON URR.role_id = R.id " +
                    "LEFT JOIN access_token A ON A.user_id = U.id " +
                    "WHERE FIND_IN_SET(R.role_name,:roleNames) AND A.id = :token " ,nativeQuery = true)
    AccessToken isValidTokenAndRoles(@Param("token") String token,@Param("roleNames") String roleNames);

    @Query(value =
            "SELECT DISTINCT A.* FROM `user` U  " +
                    "LEFT JOIN user_role_relation URR ON U.id = URR.user_id  " +
                    "LEFT JOIN role R ON URR.role_id = R.id " +
                    "LEFT JOIN role_authorities_relation RAR ON R.id = RAR.role_id " +
                    "LEFT JOIN authority AU ON  RAR.authorities_id = AU.id " +
                    "LEFT JOIN access_token A ON A.user_id = U.id " +
                    "WHERE (FIND_IN_SET(AU.authority_name,:authorities) OR FIND_IN_SET(AU.description,:authorities)) AND A.id = :token " ,nativeQuery = true)
    AccessToken isValidTokenAndAuthorities(@Param("token") String token,@Param("authorities") String authorities);
}
