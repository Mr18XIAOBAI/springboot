/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: PageableUtil
 * Author:   Revisit-Moon
 * Date:     2019/2/7 12:13 AM
 * Description: PageableUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/7 12:13 AM        1.0              描述
 */

package com.revisit.springboot.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

/**
 * 〈PageableUtil〉
 *
 * @author Revisit-Moon
 * @create 2019/2/7
 * @since 1.0.0
 */

public class PageableUtil {
    // private Integer totalPage;                          //总页数
    // private Integer totalRecord;                        //总条数
    // private List<?> list;                               //数据集
    private Integer rows;                               //查询条数
    private Integer nowPage;                            //当前页
    private Pageable pageable;                          //jpa分页插件构造器

    public PageableUtil(Integer nowPage,Integer rows, String orderBy) {
        if (nowPage == null || nowPage < 1) {
            this.nowPage = 1;
        }else {
            this.nowPage = nowPage;
        }
        if (rows == null || rows <= 0) {
            this.rows = 10;
        }else {
            this.rows = rows;
        }
        this.nowPage = (this.nowPage - 1);
        Map<String, Object> needOrder = isNeedOrder(orderBy);
        Pageable pageable = null;
        for (Map.Entry entry :needOrder.entrySet()) {
            if (Boolean.valueOf(entry.getValue().toString()) == null) {
                pageable = PageRequest.of(this.nowPage, this.rows);
            }
            if (Boolean.valueOf(entry.getValue().toString())) {
                pageable = PageRequest.of(this.nowPage, this.rows, Sort.Direction.DESC, entry.getKey().toString());
            } else {
                pageable = PageRequest.of(this.nowPage, this.rows, Sort.Direction.ASC, entry.getKey().toString());
            }
        }

        if (pageable!=null) {
            this.pageable = pageable;
        }
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    // public Integer getTotalPage() {
    //     return totalPage;
    // }
    //
    // public void setTotalPage(Integer totalPage) {
    //     this.totalPage = totalPage;
    // }
    //
    // public Integer getTotalRecord() {
    //     return totalRecord;
    // }
    //
    // public void setTotalRecord(Integer totalRecord) {
    //     this.totalRecord = totalRecord;
    //     totalPage = totalRecord % rows == 0? totalRecord/rows : ((totalRecord/rows)+1);
    // }

    public Integer getNowPage() {
        return nowPage;
    }

    public void setNowPage(Integer nowPage) {
        this.nowPage = nowPage;
    }

    // public List<?> getList() {
    //     return list;
    // }
    //
    // public void setList(List<?> list) {
    //     this.list = list;
    // }

    public Pageable getPageable() {
        return pageable;
    }

    public void setPageable(String orderBy) {
        this.pageable = pageable;
    }

    private Map<String,Object> isNeedOrder(String orderBy){
        Boolean isDesc = null;

        if (StringUtils.isBlank(orderBy)) {
            orderBy = "createTime_DESC";
        }

        if (StringUtils.isNotBlank(orderBy)){
            if (orderBy.contains("_")) {
                String[] split = StringUtils.split(orderBy, "_");
                orderBy = split[0];
                switch (split[1].toUpperCase()) {
                    case "DESC": {
                        isDesc = true;
                        break;
                    }
                    case "ASC": {
                        isDesc = false;
                        break;
                    }
                }
            } else {
                isDesc = false;
            }
        }

        Map<String,Object> map = new HashMap<>();
        map.put(orderBy,isDesc);
        return map;
    }
}
