package com.revisit.springboot.service.orderform;
import com.revisit.springboot.entity.orderform.OrderForm;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.Date;

/**
 * OrderForm接口类
 * @author Revisit-Moon
 * @date 2019-03-01 15:58:42
 */
public interface OrderFormService {

    //新增OrderForm
    JSONObject addOrderForm(OrderForm orderForm,String addressId);

    //根据ID删除OrderForm
    JSONObject deleteOrderFormById(String id);

    //根据新OrderForm更新id已存在的OrderForm
    JSONObject updateOrderFormById(String id,OrderForm newOrderForm);

    //根据ID获取OrderForm
    JSONObject findOrderFormById(String id);

    //根据ids集合批量获取OrderForm
    //JSONObject findOrderFormListByIds(List<String> ids);

    //分页获取OrderForm列表
    JSONObject findOrderFormByList(String keyword,String userId,String shopId,String status,String orderFormType,String deliveryMode,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword,String userId,String shopId,String status,String orderFormType,String deliveryMode,String orderBy,Date beginTime,Date endTime,HttpServletResponse response);

    //去支付订单
    JSONObject toPayOrderFormById(String id,String userId);

    //订单退款
    JSONObject refundMoney(String orderFormId, BigDecimal howMuch);

    //订单发货
    JSONObject orderFromDelivery(String orderFormId,String logisticsCompany,String logisticsNumber);

    //确认收货
    JSONObject orderFromDone(String userId,String orderFormId);

    //取消订单
    JSONObject orderFromCancel(String userId,String orderFormId);
}