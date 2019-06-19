package com.revisit.springboot.service.assembleproductsku;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.assembleproductsku.AssembleProductSku;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * AssembleProductSku接口类
 * @author Revisit-Moon
 * @date 2019-03-03 15:28:24
 */
public interface AssembleProductSkuService {

    //新增ProductSku
    JSONObject addAssembleProductSku(AssembleProductSku assembleProductSku);

    //根据ID删除ProductSku
    JSONObject deleteAssembleProductSkuById(String id);

    //根据新ProductSku更新id已存在的ProductSku
    JSONObject updateAssembleProductSkuById(String id, AssembleProductSku newAssembleProductSku);

    //根据ID获取ProductSku
    JSONObject findAssembleProductSkuById(String id);

    //根据ids集合批量获取ProductSku
    //JSONObject findAssembleProductSkuListByIds(List<String> ids);

    //分页获取ProductSku列表
    JSONObject findAssembleProductSkuByList(String keyword, String orderBy, Date beginTime, Date endTime, Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword, String orderBy, Date beginTime, Date endTime, HttpServletResponse response);

    //扣除库存
    // boolean deductStock(Map<String,Object> skuIdAndNumber);

    // BigDecimal whatPriceByRoleNameAndSku(int priceLevel, ProductSku sku);

    //商品sku上下移
    JSONObject assembleProductSkuSortUpOrDown(String id, String upOrDown);
}