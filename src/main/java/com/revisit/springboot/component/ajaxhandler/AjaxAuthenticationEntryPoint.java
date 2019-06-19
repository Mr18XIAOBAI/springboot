// /**
//  * Copyright (C), 2015-2019, 美果科技有限公司
//  * FileName: AjaxAuthenticationEntryPoint
//  * Author:   Revisit-Moon
//  * Date:     2019/1/29 4:06 PM
//  * Description: AjaxAuthenticationEntryPoint
//  * History:
//  * <author>          <time>          <version>          <desc>
//  * Revisit       2019/1/29 4:06 PM        1.0              描述
//  */
//
// package com.revisit.springboot.component.ajaxhandler;
//
// import com.alibaba.fastjson.JSON;
// import com.revisit.springboot.utils.Result;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.web.AuthenticationEntryPoint;
// import org.springframework.stereotype.Component;
//
// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.io.IOException;
//
// /**
//  * 〈AjaxAuthenticationEntryPoint〉
//  *
//  * @author Revisit-Moon
//  * @create 2019/1/29
//  * @since 1.0.0
//  */
// @Component
// public class AjaxAuthenticationEntryPoint implements AuthenticationEntryPoint {
//     @Override
//     public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
//         // response.setContentType("application/json;charset=UTF-8");
//         response.getWriter().write(Result.fail(102,"参数错误","登录失败").toJSONString());
//     }
// }
