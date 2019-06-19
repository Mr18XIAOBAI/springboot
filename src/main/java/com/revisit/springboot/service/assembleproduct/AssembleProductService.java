package com.revisit.springboot.service.assembleproduct;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.assembleproduct.AssembleProduct;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AssembleProduct接口类
 * @author Revisit-Moon
 * @date 2019-05-04 22:45:06
 */
public interface AssembleProductService {

    //新增AssembleProduct
    JSONObject addAssembleProduct(AssembleProduct assembleProduct, String productId, Map<String, Object> skuIdMap);

    //根据ID删除AssembleProduct
    JSONObject deleteAssembleProductById(String id);

    //根据新AssembleProduct更新id已存在的AssembleProduct
    JSONObject updateAssembleProductById(String id, AssembleProduct newAssembleProduct);

    //根据ID获取AssembleProduct
    JSONObject findAssembleProductById(String userId, String id);

    //根据ids集合批量获取AssembleProduct
    //JSONObject findAssembleProductListByIds(List<String> ids);

    //分页获取AssembleProduct列表
    JSONObject findAssembleProductByList(String keyword, String userId, String orderBy, Date beginTime, Date endTime, Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword, String orderBy, Date beginTime, Date endTime, HttpServletResponse response);

    JSONObject assembleProductSortUpOrDown(String id, String upOrDown);

    JSONObject assembleProductAutomaticRefund();


}