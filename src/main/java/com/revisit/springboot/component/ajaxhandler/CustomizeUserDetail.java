// /**
//  * Copyright (C), 2015-2019, 美果科技有限公司
//  * FileName: CustomizeUserDetail
//  * Author:   Revisit-Moon
//  * Date:     2019/1/29 4:21 PM
//  * Description: CustomizeUserDetail
//  * History:
//  * <author>          <time>          <version>          <desc>
//  * Revisit       2019/1/29 4:21 PM        1.0              描述
//  */
//
// package com.revisit.springboot.component.ajaxhandler;
//
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Component;
//
// import java.io.Serializable;
// import java.util.Collection;
// import java.util.Set;
//
// /**
//  * 〈CustomizeUserDetail〉
//  *
//  * @author Revisit-Moon
//  * @create 2019/1/29
//  * @since 1.0.0
//  */
// public class CustomizeUserDetail implements UserDetails, Serializable {
//     private String username;
//     private String password;
//     private Set<? extends GrantedAuthority> authorities;
//
//     @Override
//     public Collection<? extends GrantedAuthority> getAuthorities() {
//         return this.authorities;
//     }
//
//     public void setAuthorities(Set<? extends GrantedAuthority> authorities) {
//         this.authorities = authorities;
//     }
//
//     @Override
//     public String getPassword() { // 最重点Ⅰ
//         return this.password;
//     }
//
//     @Override
//     public String getUsername() { // 最重点Ⅱ
//         return this.username;
//     }
//
//     public void setUsername(String username) {
//         this.username = username;
//     }
//
//     public void setPassword(String password) {
//         this.password = password;
//     }
//
//     @Override
//     public boolean isAccountNonExpired() {
//         return true;
//     }
//
//     @Override
//     public boolean isAccountNonLocked() {
//         return true;
//     }
//
//     @Override
//     public boolean isCredentialsNonExpired() {
//         return true;
//     }
//
//     @Override
//     public boolean isEnabled() {
//         return true;
//     }
// }