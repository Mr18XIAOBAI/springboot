package com.revisit.springboot;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.component.codegenerator.CustomizeCodeGenerator;
import com.revisit.springboot.component.uuid.CustomizeUUIDGenerate;
import com.revisit.springboot.entity.orderform.OrderForm;
import com.revisit.springboot.entity.user.Role;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.service.user.UserService;
import com.revisit.springboot.service.wechat.WeChatPayService;
import com.revisit.springboot.utils.JavaBeanUtil;
import com.revisit.springboot.utils.MoonUtil;
import com.revisit.springboot.utils.WeChatUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// @RunWith(SpringRunner.class)
@SpringBootTest
public class RevisitSpringBootApplicationTests{


    @Autowired
    UserService userService;

    @Autowired
    WeChatPayService weChatPayService;

    @Test
    public void contextLoads() {
        codeGenerateTest();
    }

    public void codeGenerateTest(){
        String dbName = "root";
        String dbPwd = "root";
        String dbUrl = "jdbc:mysql://192.168.0.114:3306/dianzhi?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull&serverTimezone=GMT%2b8";
        String dbDriver = "com.mysql.cj.jdbc.Driver";
        String dbTable = "party_info";
        CustomizeCodeGenerator customizeCodeGenerator = new CustomizeCodeGenerator();
        customizeCodeGenerator.beginGenerator(dbName,dbPwd,dbUrl,dbDriver,dbTable);
    }

    public void beanCopyTest() {
        //模拟数据库中取出对象
        User oldUser = new User();
        oldUser.setId("旧ID");
        oldUser.setRealName("旧用户");
        oldUser.setEmail("qq");
        oldUser.setMobile("150");
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setRoleName("超级管理员");
        role.setId(CustomizeUUIDGenerate.generateBase64UUID(UUID.randomUUID().toString()));
        role.setDescription("拥有最高权限");
        roles.add(role);
        oldUser.setRoles(roles);

        //模拟controller获取对象
        User newUser = new User();
        newUser.setId("新ID");
        newUser.setRealName("新用户");
        newUser.setEmail("163");
        newUser.setMobile(null);

        JSONObject oldUserBean = new JSONObject();
        oldUserBean.put("oldUser", oldUser);
        JSONObject newUserBean = new JSONObject();
        newUserBean.put("newUser", newUser);

        System.out.println("替换前");
        System.out.println(oldUserBean.toJSONString());
        System.out.println(newUserBean.toJSONString());

        //开始替换
        JavaBeanUtil.copyProperties(oldUser, newUser, true);
        oldUserBean.put("oldUser", oldUser);
        newUserBean.put("newUser", newUser);

        System.out.println("替换后");
        System.out.println(newUserBean.toJSONString());
    }

    public void generateUUIDTest() {
        String encryptedData = "8VeBHDX6WtNFs2k52sYRGcByrVOG1f0dFQRIterA6sErFDzHK/AjGEw4AYddt2V0P20T2TwVpXz3RHPkcvQLcxC0sgJSvrtXWvhfSgDVZgl1kFHC+9b1TGPtYYFhBvqf7OQXWwJkNRXxdGdj47dW+5k5YlOY2EfAz5KKUHQ9f1HR5eBQaLq3EtvbhWgU3TKWdDP+ErsJzrvhNC5+v4tfHNo60iUt5nQ0KlEnW+rXtIN5F47CLUz8M1yevSyQEStfAd+BMZsOKXPaodiNESBun3d1kGA4wFaA5xSbVP4w/42Xt2jGDknvIl6A0FUGjb5/YO40BRvgPSo+AlmMtZ6TSi1poaQQZ2LGWYe6BX8JUrKgkaIXWE2pkZjHXAjGL+WXUJB4UoPiHsu9ynJ1MPtxPrITN26PQqzHQStuS4VQtroJJC+CSws/YVmq+1z5nXrpF2QYkPsNov7Pjdsb+aSbalQzHb1J73nISx5gW7+flEM=";
        String iv = "n27xaa3sxZDptJejubn9Kw==";
        String sessionKey = "OE9/vGzddJAIhHNmcMQndQ==";
        System.out.println(WeChatUtil.decryptWeChatMiniProgramSessionKey(encryptedData, iv, sessionKey));
        System.out.println(WeChatUtil.getWeChatToken());
        System.out.println(WeChatUtil.getWeChatToken());
        CustomizeUUIDGenerate customizeUUIDGenerate = new CustomizeUUIDGenerate();
        String id = "Jt1IU3NnQZilM678Yz3Ebw";
        String uuidFix = CustomizeUUIDGenerate.Base64UUIDToUUID("Jt1IU3NnQZilM678Yz3Ebw");
        String base64uuid = customizeUUIDGenerate.generateBase64UUID(uuidFix);
        System.out.println("当前ID: " + id);
        System.out.println("解码后的ID: " + uuidFix);
        System.out.println("解码后的ID转为短ID: " + base64uuid);
    }

    public void weChatPayTest() {
        OrderForm orderForm = new OrderForm();
        orderForm.setOrderFormDetail("[玉兰油护肤套装 1 套 999 元],[深海泥 3 个 297 元],[棉垫 2 个 18 元]");
        // orderForm.setOrderFormDetail("[test],[test2],[test3]");
        String productData =
                "[" +
                    "{\"productId\":\"测试ID\",\"buyNumber\":\"购买数量1\",\"unitPrice\":\"单价999\"}," +
                    "{\"productId\":\"测试ID2\",\"buyNumber\":\"购买数量3\",\"unitPrice\":\"单价99\"}," +
                    "{\"productId\":\"测试ID3\",\"buyNumber\":\"购买数量2\",\"unitPrice\":\"单价9\"}" +
                 "]";
        JSONArray productDataJsonArray = JSONArray.parseArray(productData);
        // String productData =
        //         "[" +
        //             "{\"productId\":\"ID\",\"buyNumber\":\"1\",\"unitPrice\":\"999\"}," +
        //             "{\"productId\":\"ID2\",\"buyNumber\":\"3\",\"unitPrice\":\"99\"}," +
        //             "{\"productId\":\"ID3\",\"buyNumber\":\"2\",\"unitPrice\":\"9\"}" +
        //          "]";
        // JSONArray productDataJsonArray = JSONArray.parseArray(productData);
        orderForm.setProductData(productDataJsonArray.toJSONString());
        orderForm.setDeliveryMode("自提");
        orderForm.setInvoiceMode(0);
        orderForm.setUserId("f6ndoQ2qQH6fHI8vGyQKAA");
        orderForm.setOpenId("o8iRs5Qey4wWx2XZnV4KJlMJjIBg");
        orderForm.setUseScenes("MWEB");
        orderForm.setStatus("待付款");
        orderForm.setPaymentMode("微信支付");
        orderForm.setLogisticsFee(new BigDecimal(0));
        orderForm.setProductFee(new BigDecimal(1314));
        orderForm.setOrderFormFee(MoonUtil.mathematical(orderForm.getLogisticsFee(),"+",orderForm.getProductFee(),0));
        System.out.println(weChatPayService.weChatPay(orderForm).toJSONString());
    }
}




