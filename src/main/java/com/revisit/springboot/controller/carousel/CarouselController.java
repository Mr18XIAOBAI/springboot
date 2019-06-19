package com.revisit.springboot.controller.carousel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.carousel.Carousel;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.carousel.CarouselService;
import com.revisit.springboot.service.user.UserService;
import com.revisit.springboot.utils.AuthorityUtil;
import com.revisit.springboot.utils.MoonUtil;
import com.revisit.springboot.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
* Carousel访问控制层
* @author Revisit-Moon
* @date 2019-03-05 17:43:20
*/
@RestController
@RequestMapping("/api/carousel")
public class CarouselController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CarouselService carouselService;

    private final static Logger logger = LoggerFactory.getLogger(CarouselController.class);

    /**
     * 新增Carousel
     *
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody
    JSONObject addCarousel(@RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SUPER_ADMIN)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }

        Carousel carousel;

        try {
            carousel = JSON.toJavaObject(param, Carousel.class);
            if (carousel == null) {
                return Result.fail(102, "参数错误", "必填参数不能为空");
            }
        } catch (Exception e) {
            return Result.fail(102, "参数错误", "转换实体类失败,错误信息: " + e.getCause());
        }

        return carouselService.addCarousel(carousel);
    }

    /**
     * 根据ID删除Carousel
     *
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public @ResponseBody
    JSONObject deleteCarousel(@PathVariable String id) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SUPER_ADMIN)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        if (id == null || StringUtils.isBlank(id) || id.length() < 22) {
            return Result.fail(102, "参数错误", "必填参数不能为空");
        }
        return carouselService.deleteCarouselById(id);
    }

    /**
     * 根据新Carousel更新id已存在的Carousel
     *
     * @param id,newCarousel
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public JSONObject updateCarousel(@PathVariable("id") String id, @RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.RESOURCES_UPDATE)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        if (id == null || StringUtils.isBlank(id) || id.length() != 22) {
            return Result.fail(102, "参数错误", "必填参数不能为空");
        }
        Carousel newCarousel;
        try {
            newCarousel = JSON.toJavaObject(param, Carousel.class);
            if (newCarousel == null) {
                return Result.fail(102, "参数错误", "必填参数不能为空");
            }
        } catch (Exception e) {
            return Result.fail(102, "参数错误", "转换实体类失败,错误信息: " + e.getCause());
        }
        return carouselService.updateCarouselById(id, newCarousel);
    }

    /**
     * 根据id获取Carousel
     *
     * @param id
     */
    @GetMapping(value = "/{id}")
    public @ResponseBody
    JSONObject getCarouselById(@PathVariable("id") String id) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.RESOURCES_READ)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        if (id == null || StringUtils.isBlank(id) || id.length() != 22) {
            return Result.fail(102, "参数错误", "必填参数不能为空");
        }
        return carouselService.findCarouselById(id);
    }

    /**
     * 分页获取Carousel列表
     *
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody
    JSONObject findCarouselByPager(@RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.RESOURCES_READ)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        String keyword = param.getString("keyword");
        String orderBy = param.getString("orderBy");

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            return Result.fail(102, "参数错误", "日期格式化异常");
        }

        Integer page = param.getInteger("page");
        Integer rows = param.getInteger("rows");

        return carouselService.findCarouselByList(keyword, orderBy, timeRangeDate.get(0), timeRangeDate.get(1), page, rows);
    }

    /**
     * 导出CarouselExcel表格
     *
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword, String orderBy, String timeRange, String token, HttpServletResponse response) {
        if (accessTokenService.isValidTokenAndRole(token, AuthorityUtil.SUPER_ADMIN)==null) {
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102, "权限认证失败", "您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            } catch (Exception e) {
                logger.info("导出Carousel列表时出错,错误原因: " + e.getCause());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);

        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102, "参数错误", "日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            } catch (Exception e) {
                logger.info("导出CarouselExcel列表时出错" + e.getCause());
                e.printStackTrace();
            }
            return;
        }
        carouselService.exportExcel(keyword, orderBy, timeRangeDate.get(0), timeRangeDate.get(1), response);
    }

    /**
     * 轮播图上下移
     *
     * @param id,param
     */
    @PutMapping(value = "/sort/{id}")
    public @ResponseBody JSONObject carouselSortUpOrDown(@PathVariable String id,@RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        String upOrDown = param.getString("upOrDown");
        if (StringUtils.isBlank(id)||id.length()!=22||StringUtils.isBlank(upOrDown)||(!upOrDown.equals("上移")&&!upOrDown.equals("下移"))){
            return Result.fail(102,"参数错误","必填参数不能为空,或不正确的参数");
        }
        return carouselService.carouselSortUpOrDown(id, upOrDown);
    }
}