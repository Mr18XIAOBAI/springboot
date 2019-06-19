/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: SMSUtil
 * Author:   Revisit-Moon
 * Date:     2019/2/2 7:05 AM
 * Description: SMSUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/2 7:05 AM        1.0              描述
 */

package com.revisit.springboot.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.revisit.springboot.component.sms.SMSCodeType;
import com.revisit.springboot.component.sms.TencentSMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;


/**
 * 〈SMSUtil〉
 *
 * @author Revisit-Moon
 * @create 2019/2/2
 * @since 1.0.0
 */
@Component
public class SMSUtil {

    static TencentSMS tencentSMS;

    @Autowired
    TencentSMS sms;

    @PostConstruct
    public void init() {
        tencentSMS = sms;
    }
    private final static Logger logger = LoggerFactory.getLogger(SMSUtil.class);

    public static String sendCode(String phone, SMSCodeType type){
        String code = MoonUtil.getRandomNumber(4);
        String message = "";

        switch(type){
            case register:
                message = "用户注册验证码：" + code;
                //return null;				//暂时屏蔽验证码注册
                break;
            case fixPassword:
                message = "密码重置验证码：" + code;
                break;
            case binding:
                message = "绑定手机验证码：" + code;
                break;
            case login:
                message = "登录验证码：" + code;
                break;
            case confirm:
                message = "确认验证码：" + code;
                break;
        }

        message += "。十五分钟内有效，请勿将此验证码告知任何人。如非本人操作，请忽略本短信。";

        if(sendMessageByTenCent(phone, message)){
            return code;
        }else{
            return null;
        }
    }

    public static boolean sendMessage(String phone, String content, String time){
        return sendMessageByTenCent(phone, content);
    }

    private static boolean sendMessageByTenCent(String phone, String content) {

        try {
            SmsSingleSender sender = new SmsSingleSender(new BigDecimal(tencentSMS.getSmsAppId()).intValue(), tencentSMS.getSmsAppKey());
            SmsSingleSenderResult result = sender.send(0, "86", phone, content, "", "");
            logger.info(result+"");
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
