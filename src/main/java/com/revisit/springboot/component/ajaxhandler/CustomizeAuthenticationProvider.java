// /**
//  * Copyright (C), 2015-2019, 美果科技有限公司
//  * FileName: CustomizeAuthenticationProvider
//  * Author:   Revisit-Moon
//  * Date:     2019/1/29 4:12 PM
//  * Description: CustomizeAuthenticationProvider
//  * History:
//  * <author>          <time>          <version>          <desc>
//  * Revisit       2019/1/29 4:12 PM        1.0              描述
//  */
//
// package com.revisit.springboot.component.ajaxhandler;
//
// import com.revisit.springboot.utils.JwtTokenUtil;
// import org.apache.commons.codec.digest.DigestUtils;
// import org.apache.tomcat.util.security.MD5Encoder;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.authentication.AuthenticationProvider;
// import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.stereotype.Component;
//
// /**
//  * 〈CustomizeAuthenticationProvider〉
//  *
//  * @author Revisit-Moon
//  * @create 2019/1/29
//  * @since 1.0.0
//  */
// @Component
// public class CustomizeAuthenticationProvider implements AuthenticationProvider {
//
//     @Autowired
//     private CustomizeUserDetailsService userDetailsService;
//
//     @Override
//     public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//         String userName = (String) authentication.getPrincipal(); // 这个获取表单输入中返回的用户名;
//         String password = (String) authentication.getCredentials(); // 这个是表单中输入的密码；
//         // BCryptPasswordEncoder md5PasswordEncoder = new BCryptPasswordEncoder();
//         // password = md5PasswordEncoder.encode(password); // 模拟从数据库中获取的密码原为 123
//         password = DigestUtils.md5Hex(password.trim());
//         UserDetails userInfo = userDetailsService.loadUserByUsername(userName);
//         if (!userInfo.getPassword().equals(password)) {
//             throw new BadCredentialsException("用户名密码不正确，请重新登陆！");
//         }
//         return new UsernamePasswordAuthenticationToken(userInfo, userInfo.getPassword(), userInfo.getAuthorities());
//     }
//
//     @Override
//     public boolean supports(Class<?> authentication) {
//         return true;
//     }
// }
