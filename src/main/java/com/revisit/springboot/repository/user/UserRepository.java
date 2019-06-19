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

import com.revisit.springboot.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 〈repository.UserRepository〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */
public interface UserRepository extends JpaRepository<User,String>,JpaSpecificationExecutor {
    User findByMobile(String mobile);
    User findByReferrerId(String referrerId);
    User findByReferrerCode(String referrerCode);
    User findByUserName(String userName);
    User findByEmail(String email);
    User findByWeChatOpenId(String openId);

    @Modifying
    @Query("DELETE from User user where user.id in :ids")
    int deleteByIds(@Param("ids") List<String> ids);

    @Query(value = "FROM User WHERE createTime > :beginTime AND createTime < :endTime")
    List<User> findUsersByTimeRange(@Param("beginTime") Date beginTime,
                                    @Param("endTime") Date endTime);

    @Query(value = "FROM User WHERE (userName = :userName AND password = :password) " +
            "OR (email = :email AND password = :password)  " +
            "OR (mobile = :mobile AND password = :password)")
    User findBySomeOneAccountAndLogin(@Param("userName") String userName,
                                      @Param("email")String email,
                                      @Param("mobile")String mobile,
                                      @Param("password")String password);

    @Query(value = "FROM User WHERE userName = :userName OR email = :email OR mobile = :mobile")
    User findBySomeOneAccount(@Param("userName") String userName,
                              @Param("email")String email,
                              @Param("mobile")String mobile);

    void deleteById(String id);
}
