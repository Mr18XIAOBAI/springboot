/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: WeChatMiniProgramService
 * Author:   Revisit-Moon
 * Date:     2019/2/12 5:27 PM
 * Description: wechat.WeChatMiniProgramService
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/12 5:27 PM        1.0              描述
 */

package com.revisit.springboot.service.wechat.impl;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.component.ajaxhandler.CustomizeSessionContext;
import com.revisit.springboot.component.uuid.CustomizeUUIDGenerate;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.repository.user.UserRepository;
import com.revisit.springboot.service.wechat.WeChatMiniProgramService;
import com.revisit.springboot.utils.MoonUtil;
import com.revisit.springboot.utils.Result;
import com.revisit.springboot.utils.WeChatUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 〈wechat.WeChatMiniProgramService〉
 *
 * @author Revisit-Moon
 * @create 2019/2/12
 * @since 1.0.0
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class WeChatMiniProgramServiceImpl implements WeChatMiniProgramService {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserRepository userRepository;

    private static Logger logger = LoggerFactory.getLogger(WeChatMiniProgramServiceImpl.class);

    @Override
    public JSONObject miniProgramLoginStatus(String jsCode){
        try{
            String miniProgramUrl = WeChatUtil.getMiniProgramUrl(jsCode);

            logger.info("小程序登录url:" + miniProgramUrl);

            //获取返回值
            JSONObject rest = MoonUtil.httpClientGet(miniProgramUrl);
            logger.info("微信服务器返回值: " + rest.toJSONString());
            if(rest == null){
                return Result.fail(102,"参数错误","从微信服务器获取信息失败");
            }

            String sessionKey = rest.getString("session_key");
            if(sessionKey==null || sessionKey.isEmpty()){
                return Result.fail(102,"参数错误","小程序进入登录态失败,缺失sessionKey");
            }
            String openid = rest.getString("openid");
            if (StringUtils.isBlank(openid)){
                return Result.fail(102,"参数错误","小程序进入登录态失败,缺失openId");
            }

            //生成第三方session
            String session3rd = CustomizeUUIDGenerate.generateBase64UUID(UUID.randomUUID().toString());
            HttpSession session = request.getSession();
            session.setAttribute(session3rd, rest.toJSONString());
            CustomizeSessionContext.createSession(session);
            JSONObject result = new JSONObject();
            result.put("session3rd",session3rd);
            return Result.success(200,"小程序进入登录态成功",result);
        }catch(JSONException e){
            Result.fail(102,"参数错误","小程序进入登录态失败,错误信息: "+e.getCause());
        }
        return Result.fail(102,"参数错误","小程序进入登录态失败");
    }

    @Override
    public JSONObject createQrCode(String page, String scene, String type, String userId) {
        logger.info("微信生成二维码: " + type);
        //定义图片最终目录
        String imagePath = "/images/";
        //判断类型是否正确,只能是(临时|永久)
        if (!type.equals("临时")&&!type.equals("永久")){
            return Result.fail(102,"参数错误","生成二维码类型不正确");
        }
        User user = null;
        //如果是生成永久二维码
        if (type.equals("永久")){
            //是否本系统用户
            user = userRepository.findById(userId).orElse(null);
            if (user == null){
                return Result.fail(102,"参数错误","用户不存在");
            }
            //是否已经生成过推广二维码
            String weChatQRCode = user.getWeChatQrCode();
            if(StringUtils.isNotBlank(weChatQRCode)){
                return Result.fail(102,"参数错误","用户已生成推广二维码");
            }
            //是否是否有推广码
            String referrerCode = user.getReferrerCode();
            //如果没有推广码
            if (StringUtils.isBlank(referrerCode)||referrerCode.length()!=32){
                //根据短Base64UUID解压长的唯一UUID并设置用户推广码字段
                user.setReferrerCode(CustomizeUUIDGenerate.Base64UUIDToUUID(user.getId()));
                //二维码带参设置成32位推广码
                scene = user.getReferrerCode();
            }else{
                //如果有推广码,则判断推广码是否与用户短Base64UUID解压后一致
                if (CustomizeUUIDGenerate.generateBase64UUID(referrerCode).equals(CustomizeUUIDGenerate.Base64UUIDToUUID(user.getId()))){
                    scene = referrerCode;
                }else{
                    //如果不一致,则重新生成推广码
                    user.setReferrerCode(CustomizeUUIDGenerate.Base64UUIDToUUID(user.getId()));
                    //二维码带参设置成32位推广码
                    scene = user.getReferrerCode();
                }
            }
            //调用生成二维码工具
            imagePath = WeChatUtil.generateQRCode(page, scene, type);
            if (StringUtils.isBlank(imagePath)){
                return Result.fail(102,"参数错误","生成二维码失败");
            }
            //生成成功则把生成后的二维码图片路径保存
            user.setWeChatQrCode(imagePath);
            //保存用户
            userRepository.save(user);
        }
        //如果生成临时二维码
        if (type.equals("临时")) {
            //直接调用工具生成
            imagePath = WeChatUtil.generateQRCode(page, scene, type);
            if (StringUtils.isBlank(imagePath)){
                return Result.fail(102,"参数错误","生成二维码失败");
            }
        }
        return Result.success(200,"成功二维码成功",imagePath);
    }
}
