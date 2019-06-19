package com.revisit.springboot.service.productclass;
import com.revisit.springboot.entity.productclass.ProductClass;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSONObject;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ProductClass接口类
 * @author Revisit-Moon
 * @date 2019-03-03 19:32:16
 */
public interface ProductClassService {

    //新增ProductClass
    JSONObject addProductClass(ProductClass productClass);

    //根据ID删除ProductClass
    JSONObject deleteProductClassById(String id);

    //根据新ProductClass更新id已存在的ProductClass
    JSONObject updateProductClassById(String id,ProductClass newProductClass);

    //根据ID获取ProductClass
    JSONObject findProductClassById(String id);

    //根据ids集合批量获取ProductClass
    //JSONObject findProductClassListByIds(List<String> ids);

    //获取树形ProductClass列表
    JSONObject findProductTreeList(String productClassId,String productClassName);

    JSONObject productClassSortUpOrDown(String id,String upOrDown);

    //分页获取ProductClass列表
    JSONObject findProductClassByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response);
}