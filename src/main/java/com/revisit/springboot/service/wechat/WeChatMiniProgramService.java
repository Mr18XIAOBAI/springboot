/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: weChatMiniProgramService
 * Author:   Revisit-Moon
 * Date:     2019/2/12 5:38 PM
 * Description: weChatMiniProgramService
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/12 5:38 PM        1.0              描述
 */


package com.revisit.springboot.service.wechat;

import com.alibaba.fastjson.JSONObject;

/**
 * 〈weChatMiniProgramService〉
 *
 * @author Revisit-Moon
 * @create 2019/2/12
 * @since 1.0.0
 */

public interface WeChatMiniProgramService {
    JSONObject miniProgramLoginStatus(String jsCode);   //微信小程序进入登录态
    JSONObject createQrCode(String page,String scene,String type,String userId);
}
