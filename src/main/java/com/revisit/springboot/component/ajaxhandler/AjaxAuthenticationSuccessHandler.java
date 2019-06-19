// /**
//  * Copyright (C), 2015-2019, 美果科技有限公司
//  * FileName: AjaxAuthenticationSuccessHandler
//  * Author:   Revisit-Moon
//  * Date:     2019/1/29 4:08 PM
//  * Description: AjaxAuthenticationSuccessHandler
//  * History:
//  * <author>          <time>          <version>          <desc>
//  * Revisit       2019/1/29 4:08 PM        1.0              描述
//  */
//
// package com.revisit.springboot.component.ajaxhandler;
//
// // import com.revisit.springboot.utils.JwtTokenUtil;
// import com.revisit.springboot.utils.JwtTokenUtil;
// import com.revisit.springboot.utils.Result;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
// import org.springframework.stereotype.Component;
//
// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.io.IOException;
//
// /**
//  * 〈AjaxAuthenticationSuccessHandler〉
//  *
//  * @author Revisit-Moon
//  * @create 2019/1/29
//  * @since 1.0.0
//  */
// @Component
// public class AjaxAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
//     @Override
//     public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//         // response.setContentType("application/json;charset=UTF-8");
//         CustomizeUserDetail userDetails = (CustomizeUserDetail) authentication.getPrincipal();
//         String jwtToken = JwtTokenUtil.generateToken(userDetails.getUsername(), 300);
//         userDetails.setPassword("");
//         response.getWriter().write(Result.tokenSuccess(200,"登录成功",userDetails,jwtToken).toJSONString());
//     }
// }
