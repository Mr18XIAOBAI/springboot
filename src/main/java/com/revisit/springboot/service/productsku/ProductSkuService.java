package com.revisit.springboot.service.productsku;
import com.revisit.springboot.entity.productsku.ProductSku;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ProductSku接口类
 * @author Revisit-Moon
 * @date 2019-03-03 15:28:24
 */
public interface ProductSkuService {

    //新增ProductSku
    JSONObject addProductSku(String userId,ProductSku productSku);

    //根据ID删除ProductSku
    JSONObject deleteProductSkuById(String userId,String id);

    //根据新ProductSku更新id已存在的ProductSku
    JSONObject updateProductSkuById(String userId,String id,ProductSku newProductSku);

    //根据ID获取ProductSku
    JSONObject findProductSkuById(String id);

    //根据ids集合批量获取ProductSku
    //JSONObject findProductSkuListByIds(List<String> ids);

    //分页获取ProductSku列表
    JSONObject findProductSkuByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response);

    //扣除库存
    boolean deductStock(Map<String,Object> skuIdAndNumber);

    BigDecimal whatPriceByRoleNameAndSku(int level, ProductSku sku);

    //商品sku上下移
    JSONObject productSkuSortUpOrDown(String id, String upOrDown);

    //批量商品上下架
    JSONObject confirmProductSku(String id, Boolean confirm);
}