/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: AccessToken
 * Author:   Revisit-Moon
 * Date:     2019/1/31 10:33 AM
 * Description: accesstoken.AccessToken
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/31 10:33 AM        1.0              描述
 */

package com.revisit.springboot.entity.token;

import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * 〈accesstoken.AccessToken〉
 *
 * @author Revisit-Moon
 * @create 2019/1/31
 * @since 1.0.0
 */
@Entity
@Table(name = "access_token")
@Data
public class AccessToken extends BasicEntity {

    @Column(nullable=false)
    private long ttl;                       //有效时间

    @Column(length = 32,nullable=false)
    private String userId;                  //对应的用户ID
}
