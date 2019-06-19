// /**
//  * Copyright (C), 2015-2019, 美果科技有限公司
//  * FileName: SecurityConfig
//  * Author:   Revisit-Moon
//  * Date:     2019/1/28 3:26 PM
//  * Description: security.config.SecurityConfig
//  * History:
//  * <author>          <time>          <version>          <desc>
//  * Revisit       2019/1/28 3:26 PM        1.0              描述
//  */
//
// package com.revisit.springboot.config.security;
//
// import com.revisit.springboot.component.ajaxhandler.*;
// import com.revisit.springboot.component.jwtfilter.JwtAuthenticationTokenFilter;
// import com.revisit.springboot.utils.AuthorityUtil;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpMethod;
// import org.springframework.http.MediaType;
// import org.springframework.security.authentication.AuthenticationProvider;
// import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
// import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
// import org.springframework.security.web.header.HeaderWriter;
// import org.springframework.web.filter.CharacterEncodingFilter;
//
// import javax.servlet.FilterRegistration;
// import javax.servlet.ServletContext;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
//
// /**
//  * 〈security.config.SecurityConfig〉
//  *
//  * @author Revisit-Moon
//  * @create 2019/1/28
//  * @since 1.0.0
//  */
// @EnableWebSecurity
// public class SecurityConfig extends WebSecurityConfigurerAdapter implements HeaderWriter {
//     @Autowired
//     private AjaxAuthenticationEntryPoint authenticationEntryPoint;  //  未登陆时返回 JSON 格式的数据给前端（否则为 html）
//
//     @Autowired
//     private AjaxAuthenticationSuccessHandler authenticationSuccessHandler;  // 登录成功返回的 JSON 格式数据给前端（否则为 html）
//
//     @Autowired
//     private AjaxAuthenticationFailureHandler authenticationFailureHandler;  //  登录失败返回的 JSON 格式数据给前端（否则为 html）
//
//     @Autowired
//     private AjaxLogoutSuccessHandler logoutSuccessHandler;  // 注销成功返回的 JSON 格式数据给前端（否则为 登录时的 html）
//
//     @Autowired
//     private AjaxAccessDeniedHandler accessDeniedHandler;    // 无权访问返回的 JSON 格式数据给前端（否则为 403 html 页面）
//
//     @Autowired
//     private CustomizeAuthenticationProvider authenticationProvider; // 自定义安全认证
//
//     @Autowired
//     private CustomizeUserDetailsService customizeUserDetailsService;
//
//     @Autowired
//     private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
//
//
//     @Override
//     protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//         // 加入自定义的安全认证
//         // auth.userDetailsService(customizeUserDetailsService).passwordEncoder(new BCryptPasswordEncoder());
//         auth.authenticationProvider(authenticationProvider);
//     }
//
//     @Override
//     protected void configure(HttpSecurity http) throws Exception {
//         //取消session
//         http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//             .and()
//             .httpBasic().authenticationEntryPoint(authenticationEntryPoint)
//             .and()
//             .authorizeRequests().antMatchers(HttpMethod.OPTIONS).permitAll()
//             .antMatchers("/").permitAll()
//             // 需要USER角色才可访问
//             .antMatchers("/api/**").hasAuthority(AuthorityUtil.SETTING_READ)
//             .anyRequest()
//             //使用rbac 角色绑定资源的方式
//             .access("@rbacAuthorityService.hasPermission(request,authentication)")
//             //.authenticated()
//             .and()
//             //该url比较特殊,需要和login.html的form的action的的url一致
//             .formLogin().successHandler(authenticationSuccessHandler).failureHandler(authenticationFailureHandler).permitAll()
//             .and()
//             .logout().logoutSuccessHandler(logoutSuccessHandler).permitAll()
//             .and()
//             .csrf().disable();
//         http.rememberMe().rememberMeParameter("remember-me").userDetailsService(customizeUserDetailsService).tokenValiditySeconds(300);
//         http.exceptionHandling().accessDeniedHandler(accessDeniedHandler);
//         //使用jwt的Authentication
//         http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
//         // 禁用headers缓存
//         http.headers().cacheControl();
//     }
//
//     @Override
//     public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
//         response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
//     }
// }
