package com.revisit.springboot.service.partyinfo;
import com.revisit.springboot.entity.partyinfo.PartyInfo;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSONObject;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * PartyInfo接口类
 * @author Revisit-Zhang
 * @date 2019-06-19 11:54:16
 */
public interface PartyInfoService {

    //新增PartyInfo
    JSONObject addPartyInfo(PartyInfo partyInfo);

    //根据ID删除PartyInfo
    JSONObject deletePartyInfoById(String id);

    //根据新PartyInfo更新id已存在的PartyInfo
    JSONObject updatePartyInfoById(String id,PartyInfo newPartyInfo);

    //根据ID获取PartyInfo
    JSONObject findPartyInfoById(String id);

    //根据ids集合批量获取PartyInfo
    //JSONObject findPartyInfoListByIds(List<String> ids);

    //分页获取PartyInfo列表
    JSONObject findPartyInfoByList(String keyword,String orderBy,Date beginTime,Date endTime,Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword,String orderBy,Date beginTime,Date endTime,HttpServletResponse response);
}