/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: SMSServiceImpl
 * Author:   Revisit-Moon
 * Date:     2019/2/2 10:18 AM
 * Description:
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/2 10:18 AM        1.0              描述
 */

package com.revisit.springboot.service.sms.impl;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.component.ajaxhandler.CustomizeSessionContext;
import com.revisit.springboot.component.sms.SMSCodeType;
import com.revisit.springboot.service.sms.SMSService;
import com.revisit.springboot.utils.Result;
import com.revisit.springboot.utils.SMSUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 〈〉
 *
 * @author Revisit-Moon
 * @create 2019/2/2
 * @since 1.0.0
 */
@Service
public class SMSServiceImpl implements SMSService {

    private final static Logger logger = LoggerFactory.getLogger(SMSService.class);

    @Autowired
    private HttpServletRequest request;

    @Override
    public JSONObject sendSMS(String mobile, String type) {

        SMSCodeType codeType = SMSCodeType.valueOf(type);

        if(codeType == null){
            return Result.fail(102,"参数错误","不正确的短信类型");
        }
        String verificationCode = SMSUtil.sendCode(mobile, codeType);
        if(StringUtils.isNotBlank(verificationCode)){
            HttpSession session = request.getSession();
            session.setAttribute("verificationCode" + mobile, verificationCode);
            session.setMaxInactiveInterval(900);	//有效时间
            logger.info("发送短信的sessionId:" + session.getId());
            logger.info("发送短信的验证码:" + verificationCode);
            logger.info("发送短信的手机:" + mobile);
            // JSONObject resultBean = new JSONObject();
            // resultBean.put("JSESSIONID",session.getId());
            CustomizeSessionContext.createSession(session);
            return Result.success(200,"发送成功","");
        }else{
            return Result.fail(110,"系统错误","发送短信失败,请联系开发人员");
        }
    }

    @Override
    public boolean verifySMS(String mobile, String code,String sessionId) {
        HttpSession session = CustomizeSessionContext.getSession(sessionId);
        if (session==null){
            return false;
        }
        String verificationCode = (String) session.getAttribute("verificationCode" + mobile);	//获取之前发送到手机上的验证码
        if(code!=null && code.equals(verificationCode)){
            session.removeAttribute("verificationCode" + mobile);						//销毁验证码
            CustomizeSessionContext.deleteSession(session);
            return true;
        }
        return false;
    }
}
