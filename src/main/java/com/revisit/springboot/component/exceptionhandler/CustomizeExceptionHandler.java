/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: CustiomizeExceptionHandler
 * Author:   Revisit-Moon
 * Date:     2019/3/1 10:19 AM
 * Description: exceptionhandler.CustiomizeExceptionHandler
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/3/1 10:19 AM        1.0              描述
 */

package com.revisit.springboot.component.exceptionhandler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Stopwatch;
import com.revisit.springboot.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.Map;

/**
 * 〈exceptionhandler.CustiomizeExceptionHandler〉
 *
 * @author Revisit-Moon
 * @create 2019/3/1
 * @since 1.0.0
 */
@RestControllerAdvice
public class CustomizeExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomizeExceptionHandler.class);
    @ExceptionHandler(value = Exception.class)
    public Object defaultErrorHandler(HttpServletRequest request, Exception e) throws Exception {
        String queryString = request.getQueryString();
        Map<String, String[]> parameterMap = request.getParameterMap();
        boolean ajax = isAjax(request);
        if (ajax) {
            JSONObject errorBean = new JSONObject();
            errorBean.put("请求地址: ",request.getRequestURI());
            errorBean.put("错误信息: ",e.getMessage());
            if(StringUtils.isNotBlank(queryString)){
                errorBean.put("地址参数: ",URLDecoder.decode(queryString,"utf-8"));
            }
            if(StringUtils.isNotBlank(queryString)){
                errorBean.put("body参数: ",parameterMap);
            }
            logger.info("系统错误: "+ e);
            return Result.fail(110, "系统错误", errorBean.toJSONString());
        }else{
            ModelAndView mav = new ModelAndView();
            mav.addObject("code", 110);
            mav.addObject("msg", "系统错误");
            mav.addObject("requestUrl", request.getRequestURI());
            if(StringUtils.isNotBlank(queryString)) {
                mav.addObject("urlData", URLDecoder.decode(queryString, "utf-8"));
            }
            if (parameterMap!=null&&!parameterMap.isEmpty()) {
                mav.addObject("bodyData", JSON.toJSONString(parameterMap));
            }
            mav.addObject("errorMsg", "错误信息: "+e.getMessage());
            mav.setViewName("error");
            return mav;
        }
    }

    // 判断是否是ajax请求
    public static boolean isAjax(HttpServletRequest request) {
        String requestType = request.getHeader("X-Requested-With");
        //如果requestType能拿到值，并且值为 XMLHttpRequest ,表示客户端的请求为异步请求，那自然是ajax请求了，反之如果为null,则是普通的请求
        if(requestType == null){
            return false;
        }
        return true;
    }
}
