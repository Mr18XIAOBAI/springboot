// /**
//  * Copyright (C), 2015-2019, 美果科技有限公司
//  * FileName: JwtAuthenticationTokenFilter
//  * Author:   Revisit-Moon
//  * Date:     2019/1/30 10:10 AM
//  * Description: JwtAuthenticationTokenFilter
//  * History:
//  * <author>          <time>          <version>          <desc>
//  * Revisit       2019/1/30 10:10 AM        1.0              描述
//  */
//
// package com.revisit.springboot.component.jwtfilter;
//
// import com.revisit.springboot.component.ajaxhandler.CustomizeUserDetailsService;
// import com.revisit.springboot.utils.JwtTokenUtil;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.MediaType;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;
//
// import javax.servlet.FilterChain;
// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.io.IOException;
//
// /**
//  * 〈JwtAuthenticationTokenFilter〉
//  *
//  * @author Revisit-Moon
//  * @create 2019/1/30
//  * @since 1.0.0
//  */
// @Component("rbacauthorityservice")
// public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
//
//     @Autowired
//     CustomizeUserDetailsService userDetailsService;
//
//     @Override
//     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//         //请求头为 Authorization
//         //请求体为 Bearer accesstoken
//
//         String authHeader = request.getHeader("Authorization");
//
//         if (authHeader != null && authHeader.startsWith("Bearer ")) {
//
//             final String authToken = authHeader.substring("Bearer ".length());
//
//             String username = JwtTokenUtil.parseToken(authToken);
//             if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                 UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//                 if (userDetails != null) {
//                     UsernamePasswordAuthenticationToken authentication =
//                             new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
//                     authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                     SecurityContextHolder.getContext().setAuthentication(authentication);
//                 }
//             }
//         }
//         response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
//         filterChain.doFilter(request, response);
//     }
// }
