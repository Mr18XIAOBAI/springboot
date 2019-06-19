// /**
//  * Copyright (C), 2015-2019, 美果科技有限公司
//  * FileName: RBACAuthorityServiceImpl
//  * Author:   Revisit-Moon
//  * Date:     2019/1/30 11:20 AM
//  * Description: impl.RBACAuthorityServiceImpl
//  * History:
//  * <author>          <time>          <version>          <desc>
//  * Revisit       2019/1/30 11:20 AM        1.0              描述
//  */
//
// package com.revisit.springboot.component.rbac.impl;
//
// import com.revisit.springboot.component.rbac.RBACAuthorityService;
// import com.revisit.springboot.utils.AuthorityUtil;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Component;
// import org.springframework.util.AntPathMatcher;
//
// import javax.servlet.http.HttpServletRequest;
// import java.util.Collection;
// import java.util.HashSet;
// import java.util.Iterator;
// import java.util.Set;
//
// /**
//  * 〈impl.RBACAuthorityServiceImpl〉
//  *
//  * @author Revisit-Moon
//  * @create 2019/1/30
//  * @since 1.0.0
//  */
// @Component("rbacAuthorityService")
// public class RBACAuthorityServiceImpl implements RBACAuthorityService {
//     public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
//
//         //得到的principal的信息是用户名还是整个用户信息取决于在自定义的authenticationProvider中传参的方式
//         Object userInfo = authentication.getPrincipal();
//
//         boolean hasPermission = false;
//
//         if (userInfo instanceof UserDetails) {
//
//             String username = ((UserDetails) userInfo).getUsername();
//
//             Collection<? extends GrantedAuthority> authorities = ((UserDetails) userInfo).getAuthorities();
//             Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
//             for (GrantedAuthority authority : authorities) {
//                 if (authority.getAuthority().equals(AuthorityUtil.SETTING_READ)) {
//                     //admin 可以访问的资源
//                     Set<String> urls = new HashSet();
//                     urls.add("/api/**");
//                     AntPathMatcher antPathMatcher = new AntPathMatcher();
//                     for (String url : urls) {
//                         if (antPathMatcher.match(url, request.getRequestURI())) {
//                             hasPermission = true;
//                             break;
//                         }
//                     }
//                 }
//             }
//             //user可以访问的资源
//             Set<String> urls = new HashSet();
//             urls.add("/api/user");
//             AntPathMatcher antPathMatcher = new AntPathMatcher();
//             for (String url : urls) {
//                 if (antPathMatcher.match(url, request.getRequestURI())) {
//                     hasPermission = true;
//                     break;
//                 }
//             }
//             return hasPermission;
//         } else {
//             return false;
//         }
//     }
// }
