/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: TencentSMS
 * Author:   Revisit-Moon
 * Date:     2019/2/2 10:10 AM
 * Description: TencentSMS
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/2 10:10 AM        1.0              描述
 */

package com.revisit.springboot.component.sms;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 〈TencentSMS〉
 *
 * @author Revisit-Moon
 * @create 2019/2/2
 * @since 1.0.0
 */
@Component
public class TencentSMS {

    private static String smsAppId;

    private static String smsAppKey;

    public TencentSMS() {

    }

    public String getSmsAppId() {
        return smsAppId;
    }

    @Value("${SMS_APPID}")
    public void setSmsAppId(String smsAppId) {
        TencentSMS.smsAppId = smsAppId;
    }

    public String getSmsAppKey() {
        return smsAppKey;
    }

    @Value("${SMS_APPKEY}")
    public void setSmsAppKey(String smsAppKey) {
        TencentSMS.smsAppKey = smsAppKey;
    }
}
