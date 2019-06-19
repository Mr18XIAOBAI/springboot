package com.revisit.springboot.controller.productclass;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.productclass.ProductClass;
import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.productclass.ProductClassService;
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
* ProductClass访问控制层
* @author Revisit-Moon
* @date 2019-03-03 19:32:16
*/
@RestController
@RequestMapping("/api/productClass")
public class ProductClassController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserService userService;
    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private ProductClassService productClassService;

    private final static Logger logger = LoggerFactory.getLogger(ProductClassController.class);

    /**
     * 新增ProductClass
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody JSONObject addProductClass(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SHOP_ADMIN);
        if (token==null){
           return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        ProductClass productClass;

        try {
            productClass = JSON.toJavaObject(param, ProductClass.class);
            if (productClass == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
            if (StringUtils.isBlank(productClass.getClassName())){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }

        return productClassService.addProductClass(productClass);
    }

    /**
     * 根据ID删除ProductClass
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject deleteProductClass(@PathVariable String id){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SHOP_ADMIN);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()<22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return productClassService.deleteProductClassById(id);
    }

    /**
     * 根据新ProductClass更新id已存在的ProductClass
     * @param id,newProductClass
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public JSONObject updateProductClass(@PathVariable("id") String id,@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SHOP_ADMIN);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        ProductClass newProductClass;
        try {
            newProductClass = JSON.toJavaObject(param, ProductClass.class);
            if (newProductClass == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }
        return productClassService.updateProductClassById(id,newProductClass);
    }

    /**
     * 根据id获取ProductClass
     * @param id
     */
    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject getProductClassById(@PathVariable("id") String id){
        String authorization = request.getHeader("Authorization");
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        AccessToken token = accessTokenService.isValid(authorization);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        return productClassService.findProductClassById(id);
    }

    /**
     * 分页获取ProductClass列表
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody JSONObject findProductClassByPager(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValid(authorization)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String keyword = param.getString("keyword");
        String orderBy = param.getString("orderBy");

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            return Result.fail(102, "参数错误", "日期格式化异常");
        }

        Integer page = param.getInteger("page");
        Integer rows = param.getInteger("rows");

        return productClassService.findProductClassByList(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 导出ProductClassExcel表格
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){
        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出ProductClass列表时出错,错误原因: "+e.getCause());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);

        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出ProductClassExcel列表时出错" + e.getCause());
                e.printStackTrace();
            }
            return;
        }
        productClassService.exportExcel(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    }

    /**
     * 〈获取树形结构分类列表〉
     *
     * @param param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/3/4 11:27 AM
     */
    @PostMapping(value = "/tree")
    public @ResponseBody JSONObject findProductClassByTree(@RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValid(authorization)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        String productClassId = param.getString("productClassId");
        String productClassName = param.getString("productClassName");
        return productClassService.findProductTreeList(productClassId, productClassName);
    }
    
    /**
     * 〈分类上移下移〉
     *
     * @param param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/3/4 4:49 PM
     */
    @PutMapping(value = "/sort/{id}")
    public @ResponseBody JSONObject productClassSortUpOrDown(@PathVariable String id,@RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        String upOrDown = param.getString("upOrDown");
        if (StringUtils.isBlank(id)||id.length()!=22||StringUtils.isBlank(upOrDown)||(!upOrDown.equals("上移")&&!upOrDown.equals("下移"))){
            return Result.fail(102,"参数错误","必填参数不能为空,或不正确的参数");
        }
        return productClassService.productClassSortUpOrDown(id, upOrDown);
    }
}