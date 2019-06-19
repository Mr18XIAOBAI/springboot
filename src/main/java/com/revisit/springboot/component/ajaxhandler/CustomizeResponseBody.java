/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: CostomizeResponBody
 * Author:   Revisit-Moon
 * Date:     2019/2/2 2:12 AM
 * Description: CostomizeResponBody
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/2 2:12 AM        1.0              描述
 */

package com.revisit.springboot.component.ajaxhandler;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 〈自定义返回控制〉
 *
 * @author Revisit-Moon
 * @create 2019/2/2
 * @since 1.0.0
 */
@Component
public class CustomizeResponseBody implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
