package com.revisit.springboot.controller.product;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.product.Product;
import com.revisit.springboot.entity.productsku.ProductSku;
import com.revisit.springboot.entity.shop.Shop;
import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.product.ProductService;
import com.revisit.springboot.service.shop.ShopService;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* Product访问控制层
* @author Revisit-Moon
* @date 2019-03-01 10:00:53
*/
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserService userService;

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private ProductService productService;

    private final static Logger logger = LoggerFactory.getLogger(ProductController.class);

    /**
     * 新增Product
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody JSONObject addProduct(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.PRODUCT_ADD);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        JSONArray skuArray = JSONArray.parseArray(param.getString("skuList"));
        List<ProductSku> skuList = new ArrayList<>();
        if (skuArray == null|| skuArray.isEmpty()){
            return Result.fail(102,"参数错误","sku不能为空");
        }else {
            skuList = skuArray.toJavaList(ProductSku.class);
            if (skuList==null||skuList.isEmpty()){
                return Result.fail(102,"参数错误","sku不能为空");
            }
        }

        param.remove("skuList");
        String userId = token.getUserId();
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)!=null){
            userId = param.getString("userId");
        }
        param.remove("userId");
        Product product;

        try {
            product = JSON.toJavaObject(param, Product.class);
            if (product == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }

        return productService.addProduct(userId,product,skuList);
    }

    /**
     * 根据ID删除Product
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject deleteProduct(@PathVariable String id,@RequestBody JSONObject param){
        if (id==null||StringUtils.isBlank(id)||id.length()<22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }

        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.PRODUCT_DELETE);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String userId = token.getUserId();
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)!=null){
            userId = param.getString("userId");
        }

        return productService.deleteProductById(userId,id);
    }

    /**
     * 根据新Product更新id已存在的Product
     * @param id,newProduct
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public JSONObject updateProduct(@PathVariable("id") String id,@RequestBody JSONObject param){
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.PRODUCT_UPDATE);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        String userId = token.getUserId();
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)!=null){
            userId = param.getString("userId");
        }
        Product newProduct;
        try {
            newProduct = JSON.toJavaObject(param, Product.class);
            if (newProduct == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }
        return productService.updateProductById(userId,id,newProduct);
    }

    /**
     * 根据id获取Product
     * @param id
     */
    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject getProductById(@PathVariable("id") String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.PRODUCT_READ)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        List<Integer> priceLevelList = accessTokenService.findPriceLevelByAccessTokenAndIsValid(authorization);
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return productService.findProductById(id,priceLevelList);
    }

    /**
     * 分页获取Product列表
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody JSONObject findProductByPager(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValid(authorization);
        if (token == null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        List<Integer> priceLevelList = accessTokenService.findPriceLevelByAccessTokenAndIsValid(authorization);
        String keyword = param.getString("keyword");
        String orderBy = param.getString("orderBy");
        String productClassId = param.getString("productClassId");
        String shopId = param.getString("shopId");
        Boolean online;
        if (accessTokenService.isValidTokenAndRoles(authorization,AuthorityUtil.SUPER_ADMIN,AuthorityUtil.SHOP_ADMIN)!=null){
            online = param.getBoolean("online");
        }else{
            online = true;
        }
        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            return Result.fail(102, "参数错误", "日期格式化异常");
        }
        Integer page = param.getInteger("page");
        Integer rows = param.getInteger("rows");

        return productService.findProductByList(priceLevelList,keyword,productClassId,shopId,online,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 分页获取我的Product列表
     * @param param
     */
    @PostMapping(value = "/myList")
    public @ResponseBody JSONObject findMyProductByPager(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndRoles(authorization, AuthorityUtil.SHOP_ADMIN);
        if (token == null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        List<Integer> priceLevelList = accessTokenService.findPriceLevelByAccessTokenAndIsValid(authorization);
        logger.info("token:" + authorization);
        String keyword = param.getString("keyword");
        String orderBy = param.getString("orderBy");
        String productClassId = param.getString("productClassId");
        Boolean online = param.getBoolean("online");
        String shopId = shopService.findShopIdByUserId(token.getUserId());
        if (StringUtils.isBlank(shopId)){
            return Result.fail(102, "参数错误", "您还没有店铺");
        }
        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
        if (timeRangeDate == null || timeRangeDate.isEmpty()) {
            return Result.fail(102, "参数错误", "日期格式化异常");
        }
        Integer page = param.getInteger("page");
        Integer rows = param.getInteger("rows");

        return productService.findProductByList(priceLevelList,keyword,productClassId,shopId,online,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 导出ProductExcel表格
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword,String productClassId,String shopId,Boolean online,String orderBy,String timeRange,String token,HttpServletResponse response){

        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出Product列表时出错,错误原因: "+e.getCause());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);
        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出ProductExcel列表时出错,错误原因: " + e.getCause());
                e.printStackTrace();
            }
            return;
        }
        productService.exportExcel(keyword,productClassId,shopId,online,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    }

    /**
     * 〈商品上下移〉
     *
     * @param param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/3/4 4:49 PM
     */
    @PutMapping(value = "/sort/{id}")
    public @ResponseBody JSONObject productSortUpOrDown(@PathVariable String id,@RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SHOP_ADMIN)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        String upOrDown = param.getString("upOrDown");
        if (StringUtils.isBlank(id)||id.length()!=22||StringUtils.isBlank(upOrDown)||(!upOrDown.equals("上移")&&!upOrDown.equals("下移"))){
            return Result.fail(102,"参数错误","必填参数不能为空,或不正确的参数");
        }
        return productService.productSortUpOrDown(id, upOrDown);
    }

    /**
     * 〈批量审核商品〉
     *
     * @param param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019-06-17 16:45
     */
    @PostMapping(value = "/confirm")
    public @ResponseBody JSONObject confirmProduct(@RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        String id = param.getString("id");
        Boolean confirm = param.getBoolean("confirm");
        if (StringUtils.isBlank(id)||id.length()<22||confirm==null){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return productService.confirmProduct(id, confirm);
    }
}