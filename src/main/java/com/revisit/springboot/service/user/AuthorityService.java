package com.revisit.springboot.service.user;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Authority接口类
 * @author Revisit-Moon
 * @date 2019-04-19 14:13:48
 */
public interface AuthorityService {

    //新增Authority
    // JSONObject addAuthority(Authority authority);

    //根据ID删除Authority
    // JSONObject deleteAuthorityById(String id);

    //根据新Authority更新id已存在的Authority
    // JSONObject updateAuthorityById(String id, Authority newAuthority);

    //根据ID获取Authority
    JSONObject findAuthorityById(String id);

    //根据ids集合批量获取Authority
    //JSONObject findAuthorityListByIds(List<String> ids);

    //分页获取Authority列表
    JSONObject findAuthorityByList(String keyword, String orderBy, Date beginTime, Date endTime, Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword, String orderBy, Date beginTime, Date endTime, HttpServletResponse response);
}