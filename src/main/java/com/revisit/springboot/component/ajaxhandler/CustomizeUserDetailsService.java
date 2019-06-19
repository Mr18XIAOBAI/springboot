// /**
//  * Copyright (C), 2015-2019, 美果科技有限公司
//  * FileName: CustomizeUserDetailsService
//  * Author:   Revisit-Moon
//  * Date:     2019/1/29 4:15 PM
//  * Description: CustomizeUserDetailsService
//  * History:
//  * <author>          <time>          <version>          <desc>
//  * Revisit       2019/1/29 4:15 PM        1.0              描述
//  */
//
// package com.revisit.springboot.component.ajaxhandler;
//
// import com.revisit.springboot.entity.user.Authority;
// import com.revisit.springboot.entity.user.Role;
// import com.revisit.springboot.entity.user.User;
// import com.revisit.springboot.repository.user.UserRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;
//
// import java.util.HashSet;
// import java.util.Set;
//
// /**
//  * 〈CustomizeUserDetailsService〉
//  *
//  * @author Revisit-Moon
//  * @create 2019/1/29
//  * @since 1.0.0
//  */
// @Component
// @Transactional
// public class CustomizeUserDetailsService implements UserDetailsService {
//
//     @Autowired
//     private UserRepository userRepository;
//
//     @Override
//     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//         //构建用户信息的逻辑(取数据库/LDAP等用户信息)
//         CustomizeUserDetail customizeUser = new CustomizeUserDetail();
//         User user = userRepository.findBySomeOneAccount(username, username, username);
//
//         if (user!=null) {
//             // BCryptPasswordEncoder md5PasswordEncoder = new BCryptPasswordEncoder();
//             // String encodePassword = md5PasswordEncoder.encode(user.getPassword()); // 模拟从数据库中获取的密码原为 123
//             customizeUser.setUsername(username);
//             customizeUser.setPassword(user.getPassword());
//         }
//         Set authoritiesSet = new HashSet();
//         Set<Role> roles = user.getRoles();
//         for (Role role :roles) {
//             Set<Authority> authorities = role.getAuthorities();
//             for (Authority authority :authorities) {
//                 authoritiesSet.add(new SimpleGrantedAuthority(authority.getAuthorityName()));
//             }
//         }
//
//         // GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_ADMIN"); // 模拟从数据库中获取用户角色
//         // authoritiesSet.add(authority);
//         customizeUser.setAuthorities(authoritiesSet);
//         return customizeUser;
//     }
// }
