/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: UserRole
 * Author:   Revisit-Moon
 * Date:     2019/1/29 2:05 PM
 * Description: UserRole
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/29 2:05 PM        1.0              描述
 */

package com.revisit.springboot.entity.user;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 〈UserRole〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */
@Entity
@Table(name = "role")
@Data
public class Role {
    @Id
    @Column(name = "id", unique = true, nullable = false, length = 32)
    @GenericGenerator(name = "system-uuid", strategy = "com.revisit.springboot.component.uuid.CustomizeUUIDGenerate" )
    @GeneratedValue(generator = "system-uuid")
    private String id;                      //主键ID

    @Column(length = 20)
    private String roleName;                //角色名称

    @Column(length = 30)
    private String description;             //备注信息

    private int level;                      //会员等级

    private boolean inherent;               //是否内置

    @ManyToMany(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(name="role_authorities_relation", joinColumns={ @JoinColumn(name="role_id",nullable=false,updatable=false)}, inverseJoinColumns={@JoinColumn(name="authorities_id",nullable=false,updatable=false)})
    private Set<Authority> authorities = new HashSet<>();
}
