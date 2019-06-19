package com.revisit.springboot.controller.orderform;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.service.shop.ShopService;
import com.revisit.springboot.utils.Result;
import com.revisit.springboot.utils.MoonUtil;
import com.revisit.springboot.utils.AuthorityUtil;
import com.revisit.springboot.service.orderform.OrderFormService;
import com.revisit.springboot.entity.orderform.OrderForm;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
* OrderForm访问控制层
* @author Revisit-Moon
* @date 2019-03-01 15:58:42
*/
@RestController
@RequestMapping("/api/orderForm")
public class OrderFormController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private OrderFormService orderFormService;

    private final static Logger logger = LoggerFactory.getLogger(OrderFormController.class);

    /**
     * 新增OrderForm
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody JSONObject addOrderForm(@RequestBody JSONObject param){
        try {
            String authorization = request.getHeader("Authorization");
            AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.ORDER_FORM_ADD);
            if (token==null){
                return Result.fail(108,"权限认证失败","您没有权限或token过期");
            }
            String addressId = param.getString("addressId");
            JSONArray productData = param.getJSONArray("productData");
            if (productData==null||productData.isEmpty()){
                return Result.fail(102,"参数错误","商品数据不能为空");
            }
            //删除不需要的参数让转换实体参数时为空
            param.remove("addressId");
            param.remove("productData");
            param.remove("extractionSign");
            param.remove("logisticsCompany");
            param.remove("logisticsNumber");
            param.remove("logisticsDetail");
            param.remove("logisticsDetail");
            param.remove("invoiceUrl");
            OrderForm orderForm;
            orderForm = JSON.toJavaObject(param, OrderForm.class);

            if (orderForm==null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }

            String userSendId = orderForm.getUserId();
            if (StringUtils.isBlank(userSendId)){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }

            orderForm.setProductData(productData.toJSONString());

            String userId = token.getUserId();

            if (!userId.equals(userSendId)){
                return Result.fail(102,"参数错误","您不是用户本人");
            }

            orderForm.setStatus("待付款");
            orderForm.setShopId("mall");
            return orderFormService.addOrderForm(orderForm,addressId);
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换对象异常,错误信息: "+e.getCause());
        }
    }

    /**
     * 根据id获取OrderForm
     * @param id
     */
    @GetMapping(value = "/toPay/{id}")
    public @ResponseBody JSONObject toPayOrderFormById(@PathVariable("id") String id){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.ORDER_FORM_READ);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return orderFormService.toPayOrderFormById(id,token.getUserId());
    }

    /**
     * 根据ID删除OrderForm
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject deleteOrderForm(@PathVariable String id){
        if (id==null||StringUtils.isBlank(id)||id.length()<22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.ORDER_FORM_DELETE)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        return orderFormService.deleteOrderFormById(id);
    }

    // /**
    //  * 根据新OrderForm更新id已存在的OrderForm
    //  * @param id,newOrderForm
    //  */
    // @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    // @ResponseBody
    // public JSONObject updateOrderForm(@PathVariable("id") String id,@RequestBody JSONObject param){
    //     String authorization = request.getHeader("Authorization");
    //     if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.ORDER_FORM_UPDATE)==null){
    //         return Result.fail(108,"权限认证失败","您没有权限或token过期");
    //     }
    //     if (id==null||StringUtils.isBlank(id)||id.length()!=22){
    //         return Result.fail(102,"参数错误","必填参数不能为空");
    //     }
    //     OrderForm newOrderForm;
    //     try {
    //         newOrderForm = JSON.toJavaObject(param, OrderForm.class);
    //         if (newOrderForm == null){
    //             return Result.fail(102,"参数错误","必填参数不能为空");
    //         }
    //     }catch (Exception e){
    //         return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getMessage());
    //     }
    //     return orderFormService.updateOrderFormById(id,newOrderForm);
    // }

    /**
     * 根据id获取OrderForm
     * @param id
     */
    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject getOrderFormById(@PathVariable("id") String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.ORDER_FORM_READ)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return orderFormService.findOrderFormById(id);
    }

    /**
     * 分页获取OrderForm列表
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody JSONObject findOrderFormByPager(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String keyword = param.getString("keyword");
        String userId = param.getString("userId");
        String shopId = param.getString("shopId");
        String status = param.getString("status");
        String orderFormType = param.getString("orderFormType");
        String deliveryMode = param.getString("deliveryMode");
        String orderBy = param.getString("orderBy");
        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            return Result.fail(102, "参数错误", "日期格式化异常");
        }

        Integer page = param.getInteger("page");
        Integer rows = param.getInteger("rows");

        return orderFormService.findOrderFormByList(keyword,userId,shopId,status,orderFormType,deliveryMode,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 分页获取OrderForm列表
     * @param param
     */
    @PostMapping(value = "/myList")
    public @ResponseBody JSONObject findMyOrderFormByPager(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.ORDER_FORM_READ);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String keyword = param.getString("keyword");
        String userId = token.getUserId();
        String shopId = param.getString("shopId");
        String status = param.getString("status");
        String orderFormType = param.getString("orderFormType");
        String deliveryMode = param.getString("deliveryMode");
        String orderBy = param.getString("orderBy");
        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            return Result.fail(102, "参数错误", "日期格式化异常");
        }

        Integer page = param.getInteger("page");
        Integer rows = param.getInteger("rows");

        return orderFormService.findOrderFormByList(keyword,userId,shopId,status,orderFormType,deliveryMode,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 分页获取OrderForm列表
     * @param param
     */
    @PostMapping(value = "/shop/myList")
    public @ResponseBody JSONObject findMyShopOrderFormByPager(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.COMMUNITY_PARTNER);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String keyword = param.getString("keyword");
        String userId = token.getUserId();
        String status = param.getString("status");
        String orderFormType = param.getString("orderFormType");
        String deliveryMode = param.getString("deliveryMode");
        String orderBy = param.getString("orderBy");
        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            return Result.fail(102, "参数错误", "日期格式化异常");
        }

        Integer page = param.getInteger("page");
        Integer rows = param.getInteger("rows");

        String shopId = shopService.findShopIdByUserId(userId);
        if (StringUtils.isBlank(shopId)){
            return Result.fail(102,"参数错误","您当前还没有店铺,请先新建店铺");
        }

        return orderFormService.findOrderFormByList(keyword,userId,shopId,status,orderFormType,deliveryMode,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 导出OrderFormExcel表格
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword,String userId,String shopId,String status,String orderFormType,String deliveryMode,
                       String orderBy,String timeRange,String token,HttpServletResponse response){
        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出OrderForm列表时出错,错误原因: "+e.getCause());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);

        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出OrderFormExcel列表时出错" + e.getCause());
                e.printStackTrace();
            }
            return;
        }
        orderFormService.exportExcel(keyword,userId,shopId,status,orderFormType,deliveryMode,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    }


    /**
     * 〈管理员确认退款〉
     *
     * @param param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/14 11:49 PM
     */
    @PostMapping(value = "/refundMoney")
    public @ResponseBody JSONObject weChatPayRefundMoney(@RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization)){
            return Result.fail(108, "权限认证失败", "缺少必填参数");
        }
        if (accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SUPER_ADMIN)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }

        String orderFormId = param.getString("orderFormId");
        if (StringUtils.isBlank(orderFormId)){
            return Result.fail(102, "参数错误", "订单ID不能为空");
        }
        BigDecimal howMuch = param.getBigDecimal("howMuch");
        return orderFormService.refundMoney(orderFormId,howMuch);
    }

    /**
     * 〈订单发货〉
     *
     * @param param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/17 3:10 PM
     */
    @PostMapping(value = "/delivery")
    public @ResponseBody JSONObject orderFromDelivery(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SHOP_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String orderFormId = param.getString("orderFormId");
        String logisticsCompany = param.getString("logisticsCompany");
        String logisticsNumber = param.getString("logisticsNumber");
        if (StringUtils.isBlank(orderFormId)){
            return Result.fail(102,"参数错误","订单ID不能为空");
        }
        if (StringUtils.isBlank(logisticsCompany)){
            return Result.fail(102,"参数错误","物流公司不能为空");
        }

        if (StringUtils.isBlank(logisticsNumber)){
            return Result.fail(102,"参数错误","物流编号不能为空");
        }

        return orderFormService.orderFromDelivery(orderFormId,logisticsCompany,logisticsNumber);
    }

    /**
     * 〈确认收货〉
     *
     * @param orderFormId
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/17 3:10 PM
     */
    @PutMapping(value = "/done/{orderFormId}")
    public @ResponseBody JSONObject orderFromDone(@PathVariable(name = "orderFormId") String orderFormId){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.findAccessTokenByIdAndIsValid(authorization);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (StringUtils.isBlank(orderFormId)||orderFormId.length()<22){
            return Result.fail(102,"参数错误","订单ID不能为空");
        }

        return orderFormService.orderFromDone(token.getUserId(),orderFormId);
    }

    /**
     * 〈取消订单〉
     *
     * @param orderFormId
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/4/17 3:10 PM
     */
    @PutMapping(value = "/cancel/{orderFormId}")
    public @ResponseBody JSONObject orderFromCancel(@PathVariable(name = "orderFormId") String orderFormId){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.findAccessTokenByIdAndIsValid(authorization);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (StringUtils.isBlank(orderFormId)||orderFormId.length()<22){
            return Result.fail(102,"参数错误","订单ID不能为空");
        }

        return orderFormService.orderFromCancel(token.getUserId(),orderFormId);
    }
}