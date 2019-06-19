package com.revisit.springboot.service.addressmanage;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.addressmanage.AddressManage;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * AddressManage接口类
 * @author Revisit-Moon
 * @date 2019-04-15 21:11:34
 */
public interface AddressManageService {

    //新增AddressManage
    JSONObject addAddressManage(AddressManage addressManage);

    //根据ID删除AddressManage
    JSONObject deleteAddressManageById(String id, String userId);

    //根据新AddressManage更新id已存在的AddressManage
    JSONObject updateAddressManageById(String id, AddressManage newAddressManage);

    //根据userId获取AddressManage列表
    JSONObject findAddressManageByUserId(String userId);

    //根据ID获取AddressManage
    JSONObject findAddressManageById(String id, String userId);

    //根据ids集合批量获取AddressManage
    //JSONObject findAddressManageListByIds(List<String> ids);

    //分页获取AddressManage列表
    JSONObject findAddressManageByList(String keyword, String userId, String orderBy, Date beginTime, Date endTime, Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword, String userId, String orderBy, Date beginTime, Date endTime, HttpServletResponse response);
}