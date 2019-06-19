/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: SMSController
 * Author:   Revisit-Moon
 * Date:     2019/2/2 6:59 AM
 * Description: sms.SMSController
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/2 6:59 AM        1.0              描述
 */

package com.revisit.springboot.controller.sms;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.service.sms.SMSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 〈sms.SMSController〉
 *
 * @author Revisit-Moon
 * @create 2019/2/2
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/sms")
public class SMSController {

    @Autowired
    private SMSService smsService;

    @PostMapping("/send")
    public @ResponseBody JSONObject sendSMS(@RequestBody JSONObject param){
        String mobile = param.getString("mobile");
        String type = param.getString("type");
        return smsService.sendSMS(mobile,type);
    }
}
