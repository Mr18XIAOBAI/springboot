/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: SMSCodeType
 * Author:   Revisit-Moon
 * Date:     2019/2/2 7:10 AM
 * Description: sms.SMSCodeType
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/2 7:10 AM        1.0              描述
 */

package com.revisit.springboot.component.sms;

/**
 * 〈sms.SMSCodeType〉
 *
 * @author Revisit-Moon
 * @create 2019/2/2
 * @since 1.0.0
 */

public enum SMSCodeType {
    register,       //注册验证码
    fixPassword,    //重置密码验证码
    binding,        //绑定手机验证码
    login,          //登录验证码
    confirm;        //确认验证码

}
