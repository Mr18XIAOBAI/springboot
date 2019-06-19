package com.revisit.springboot.service.assemblerelation;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.assemblerelation.AssembleRelation;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * AssembleRelation接口类
 * @author Revisit-Moon
 * @date 2019-05-06 10:22:48
 */
public interface AssembleRelationService {

    //新增AssembleRelation
    JSONObject addAssembleRelation(AssembleRelation assembleRelation);

    //根据ID删除AssembleRelation
    JSONObject deleteAssembleRelationById(String id);

    //根据新AssembleRelation更新id已存在的AssembleRelation
    JSONObject updateAssembleRelationById(String id, AssembleRelation newAssembleRelation);

    //根据ID获取AssembleRelation
    JSONObject findAssembleRelationById(String id);

    //根据ids集合批量获取AssembleRelation
    //JSONObject findAssembleRelationListByIds(List<String> ids);

    //分页获取AssembleRelation列表
    JSONObject findAssembleRelationByList(String keyword, String orderBy, Date beginTime, Date endTime, Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword, String orderBy, Date beginTime, Date endTime, HttpServletResponse response);
}