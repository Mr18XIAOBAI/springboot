package com.revisit.springboot.service.shop;
import com.revisit.springboot.entity.shop.Shop;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSONObject;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Shop接口类
 * @author Revisit-Moon
 * @date 2019-06-10 16:14:48
 */
public interface ShopService {

    //新增Shop
    JSONObject addShop(Shop shop);

    //根据ID删除Shop
    JSONObject deleteShopById(String id);

    //根据新Shop更新id已存在的Shop
    JSONObject updateShopById(String id,Shop newShop);

    //根据ID获取Shop
    JSONObject findShopById(String id);

    //根据用户ID获取店铺
    JSONObject findShopByUserId(String userId);

    //根据用户ID获取店铺ID
    String findShopIdByUserId(String userId);

    //根据ids集合批量获取Shop
    //JSONObject findShopListByIds(List<String> ids);

    //分页获取Shop列表
    JSONObject findShopByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response);
}