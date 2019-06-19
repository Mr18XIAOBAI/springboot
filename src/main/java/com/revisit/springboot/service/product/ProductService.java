package com.revisit.springboot.service.product;
import com.revisit.springboot.entity.product.Product;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.productsku.ProductSku;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Product接口类
 * @author Revisit-Moon
 * @date 2019-03-01 10:00:53
 */
public interface ProductService {

    //新增Product
    JSONObject addProduct(String userId,Product product, List<ProductSku> skuList);

    //根据ID删除Product
    JSONObject deleteProductById(String userId,String id);

    //根据新Product更新id已存在的Product
    JSONObject updateProductById(String userId,String id,Product newProduct);

    //根据ID获取Product
    JSONObject findProductById(String id,List<Integer> priceLevelList);

    //根据ids集合批量获取Product
    //JSONObject findProductListByIds(List<String> ids);

    //分页获取Product列表
    JSONObject findProductByList(List<Integer> priceLevelList,String keyword,String productClassId,String shopId,Boolean online,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows);

    //导出excel表格
    void exportExcel(String keyword,String productClassId,String shopId,Boolean online,String orderBy,Date beginTime,Date endTime,HttpServletResponse response);

    //商品上下移
    JSONObject productSortUpOrDown(String id, String upOrDown);

    //审核商品
    JSONObject confirmProduct(String id, Boolean confirm);
}