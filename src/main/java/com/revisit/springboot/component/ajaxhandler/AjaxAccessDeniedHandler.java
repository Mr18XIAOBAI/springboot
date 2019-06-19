// /**
//  * Copyright (C), 2015-2019, 美果科技有限公司
//  * FileName: AjaxAccessDeniedHandler
//  * Author:   Revisit-Moon
//  * Date:     2019/1/29 3:59 PM
//  * Description: AjaxAccessDeniedHandler
//  * History:
//  * <author>          <time>          <version>          <desc>
//  * Revisit       2019/1/29 3:59 PM        1.0              描述
//  */
//
// package com.revisit.springboot.component.ajaxhandler;
// import com.alibaba.fastjson.JSON;
// import com.revisit.springboot.utils.Result;
// import org.springframework.security.access.AccessDeniedException;
// import org.springframework.security.web.access.AccessDeniedHandler;
// import org.springframework.stereotype.Component;
//
// import javax.servlet.ServletException;
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.io.IOException;
// import com.alibaba.fastjson.JSON;
//
// /**
//  * 〈AjaxAccessDeniedHandler〉
//  *
//  * @author Revisit-Moon
//  * @create 2019/1/29
//  * @since 1.0.0
//  */
// @Component
// public class AjaxAccessDeniedHandler implements AccessDeniedHandler {
//     @Override
//     public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
//         // response.setContentType("application/json;charset=UTF-8");
//         response.getWriter().write(Result.fail(102,"参数错误","登录失败").toJSONString());
//     }
// }