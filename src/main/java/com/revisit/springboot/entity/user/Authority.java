/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: Authority
 * Author:   Revisit-Moon
 * Date:     2019/1/31 12:45 AM
 * Description: Authority
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/31 12:45 AM        1.0              描述
 */

package com.revisit.springboot.entity.user;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 〈Authority〉
 *
 * @author Revisit-Moon
 * @create 2019/1/31
 * @since 1.0.0
 */
@Entity
@Table(name = "authority")
@Data
public class Authority {
    @Id
    @Column(name = "id", unique = true, nullable = false, length = 32)
    @GenericGenerator(name = "system-uuid", strategy = "com.revisit.springboot.component.uuid.CustomizeUUIDGenerate" )
    @GeneratedValue(generator = "system-uuid")
    private String id;                      //主键ID

    @Column(length = 20)
    private String authorityName;           //角色名称

    @Column(length = 30)
    private String description;             //备注信息
}
