/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: User
 * Author:   Revisit-Moon
 * Date:     2019/1/28 2:30 PM
 * Description: entity.User
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/28 2:30 PM        1.0              描述
 */

package com.revisit.springboot.entity.user;
import cn.afterturn.easypoi.excel.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.revisit.springboot.entity.BasicEntity;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * 〈用户实体类〉
 *
 * @author Revisit-Moon
 * @create 2019/1/28
 * @since 1.0.0
 */
@Entity
@Table(name = "user")
@Data
public class User extends BasicEntity {

    @Excel(name = "用户名")
    @Column(length = 20,unique = true)
    private String userName;                        //用户名

    @Excel(name = "性别")
    private Integer gender;                         //性别(0:女,1:男)

    @Excel(name = "手机")
    @Column(length = 15,unique = true)
    private String mobile;                          //手机

    @Excel(name = "邮箱")
    @Column(length = 50,unique = true)
    private String email;                           //邮箱

    @Column(length = 100)
    private String password;                        //密码

    @Excel(name = "真实姓名")
    @Column(length = 20)
    private String realName;                        //真实姓名

    @Column(length = 100,unique = true)
    private String weChatOpenId;                    //微信openId;

    @Excel(name = "微信名")
    @Column(length = 100)
    private String weChatName;                      //微信名

    @Column(length = 255)
    private String weChatAvatar;                    //微信头像

    @Column(length = 255)
    private String weChatQrCode;                   //微信二维码图片

    @Column(length = 100)
    private String referrerId;                      //推荐人id

    @Column(length = 100)
    private String referrerCode;                    //自身推荐码

    private String weChatCity;                      //微信号所在城市

    // , "userAuthorities"
    // @JsonIgnoreProperties(value={"users"})
    @ManyToMany(fetch = FetchType.LAZY,cascade={CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(name="user_role_relation", joinColumns={@JoinColumn(name="user_id",nullable=false,updatable=false)}, inverseJoinColumns={@JoinColumn(name="role_id",nullable=false,updatable=false)})
    private Set<Role> roles = new HashSet<>();

    @Column(precision = 19, scale = 2)
    @ColumnDefault("0")
    private BigDecimal creditScore;                 //信用积分

    // @ManyToMany(fetch = FetchType.LAZY,cascade=CascadeType.ALL)
    // @JoinTable(name="user_group_relation", inverseJoinColumns={ @JoinColumn(name="user_id",nullable=false,updatable=false)}, joinColumns={@JoinColumn(name="user_group_id",nullable=false,updatable=false)})
    // private Set<UserGroup> userGroups = new HashSet<>();
}
