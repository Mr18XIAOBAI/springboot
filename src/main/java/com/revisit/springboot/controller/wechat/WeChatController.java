/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: WeChatController
 * Author:   Revisit-Moon
 * Date:     2019/2/12 6:19 PM
 * Description: wechat.WeChatController
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/12 6:19 PM        1.0              描述
 */

package com.revisit.springboot.controller.wechat;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.wechat.WeChatMiniProgramService;
import com.revisit.springboot.service.wechat.WeChatPayService;
import com.revisit.springboot.utils.AuthorityUtil;
import com.revisit.springboot.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 〈wechat.WeChatController〉
 *
 * @author Revisit-Moon
 * @create 2019/2/12
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/wechat")
public class WeChatController {

    @Autowired
    private WeChatMiniProgramService weChatMiniProgramService;

    @Autowired
    private WeChatPayService weChatPayService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private AccessTokenService accessTokenService;

    private final static Logger logger = LoggerFactory.getLogger(WeChatController.class);


    /**
     * 〈小程序进入登录态〉
     *
     * @param param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/13 1:49 AM
     */
    @PostMapping(value = "/miniProgramLoginStatus")
    public @ResponseBody JSONObject miniProgramLoginStatus(@RequestBody JSONObject param) {
        String jsCode = param.getString("jsCode");
        if (StringUtils.isBlank(jsCode)){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return weChatMiniProgramService.miniProgramLoginStatus(jsCode);
    }

    @PostMapping(value = "/createMiniProgramQrCode")
    public @ResponseBody JSONObject createQrCode(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization)){
            return Result.fail(102, "权限认证失败", "缺少必填参数");
        }
        if (accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.TOURIST)!=null) {
            return Result.fail(102, "权限认证失败", "您没有权限或token过期");
        }
        if (accessTokenService.isValid(authorization)==null) {
            return Result.fail(102, "权限认证失败", "您没有权限或token过期");
        }
        String userId = accessTokenService.findAccessTokenByIdAndIsValid(authorization).getUserId();
        String page = param.getString("page");
        String scene = param.getString("scene");
        String type = param.getString("type");
        return weChatMiniProgramService.createQrCode(page,scene,type,userId);
    }

    /**
     * 〈微信支付结果回调〉
     *
     * @param weChatXml
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/13 1:50 AM
     */
    @RequestMapping(value = "/payNotify",method = {RequestMethod.GET, RequestMethod.POST})
    public String weChatPayNotify(@RequestBody String weChatXml){
        logger.info("收到微信支付结果回调: ");
        logger.info("\n"+weChatXml);
        return weChatPayService.weChatPayNotify(weChatXml);
    }

    /**
     * 〈微信退款结果回调〉
     *
     * @param weChatXml
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/13 1:50 AM
     */
    @RequestMapping(value = "/refundMoneyNotify",method = {RequestMethod.GET, RequestMethod.POST})
    public String weChatRefundMoneyNotify(@RequestBody String weChatXml){
        logger.info("收到微信支付申请退款结果回调: ");
        logger.info("\n"+weChatXml);
        return weChatPayService.weChatRefundMoneyNotify(weChatXml);
    }
}
