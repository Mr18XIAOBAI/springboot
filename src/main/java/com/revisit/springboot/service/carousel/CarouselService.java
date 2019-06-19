package com.revisit.springboot.service.carousel;

import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.carousel.Carousel;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * Carousel接口类
 * @author Revisit-Moon
 * @date 2019-03-05 17:43:20
 */
public interface CarouselService {

    //新增Carousel
    JSONObject addCarousel(Carousel carousel);

    //根据ID删除Carousel
    JSONObject deleteCarouselById(String id);

    //根据新Carousel更新id已存在的Carousel
    JSONObject updateCarouselById(String id, Carousel newCarousel);

    //根据ID获取Carousel
    JSONObject findCarouselById(String id);

    //根据ids集合批量获取Carousel
    //JSONObject findCarouselListByIds(List<String> ids);
    JSONObject carouselSortUpOrDown(String id, String upOrDown);

    //分页获取Carousel列表
    JSONObject findCarouselByList(String keyword, String orderBy, Date beginTime, Date endTime, Integer page, Integer rows);
    //导出excel表格
    void exportExcel(String keyword, String orderBy, Date beginTime, Date endTime, HttpServletResponse response);
}