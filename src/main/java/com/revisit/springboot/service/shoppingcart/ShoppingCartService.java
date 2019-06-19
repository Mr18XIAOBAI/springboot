package com.revisit.springboot.service.shoppingcart;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.shoppingcart.ShoppingCart;
import com.revisit.springboot.entity.shoppingcart.ShoppingCartItem;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * ShoppingCart接口类
 * @author Revisit-Moon
 * @date 2019-04-15 11:05:45
 */
public interface ShoppingCartService {

    //新增ShoppingCart
    JSONObject addShoppingCart(String userId, ShoppingCartItem shoppingCartItem);

    //根据ID删除ShoppingCart
    JSONObject deleteShoppingCartItemByProductSkuId(String userId, String productSkuId);

    JSONObject andSubtractShoppingCart(String userId, String skuId, Boolean isAdd, Integer quantity);

    //根据新ShoppingCart更新id已存在的ShoppingCart
    JSONObject updateShoppingCartById(String id, ShoppingCart newShoppingCart);

    //根据用户ID获取ShoppingCart
    JSONObject findShoppingCartByUserId(String userId);

    //根据ids集合批量获取ShoppingCart
    //JSONObject findShoppingCartListByIds(List<String> ids);

    //分页获取ShoppingCart列表
    JSONObject findShoppingCartByList(String keyword, String orderBy, Date beginTime, Date endTime, Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword, String orderBy, Date beginTime, Date endTime, HttpServletResponse response);
}