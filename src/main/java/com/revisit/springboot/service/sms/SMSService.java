/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: TencentSMS
 * Author:   Revisit-Moon
 * Date:     2019/2/2 10:15 AM
 * Description: sms.TencentSMS
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/2 10:15 AM        1.0              描述
 */

package com.revisit.springboot.service.sms;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpSession;

/**
 * 〈sms.TencentSMS〉
 *
 * @author Revisit-Moon
 * @create 2019/2/2
 * @since 1.0.0
 */

public interface SMSService {
    JSONObject sendSMS(String mobile, String type);
    boolean verifySMS(String mobile, String code,String sessionId);
}
