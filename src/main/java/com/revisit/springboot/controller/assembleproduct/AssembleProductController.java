package com.revisit.springboot.controller.assembleproduct;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.assembleproduct.AssembleProduct;
import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.assembleproduct.AssembleProductService;
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
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* AssembleProduct访问控制层
* @author Revisit-Moon
* @date 2019-05-04 22:45:06
*/
@RestController
@RequestMapping("/api/assembleProduct")
public class AssembleProductController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private AssembleProductService assembleProductService;

    private final static Logger logger = LoggerFactory.getLogger(AssembleProductController.class);

    /**
     * 新增AssembleProduct
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody JSONObject addAssembleProduct(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        String productId = param.getString("productId");
        if (StringUtils.isBlank(productId)||productId.length()<22){
            return Result.fail(102,"参数错误","商品Id不能为空或长度不正确");
        }
        param.remove("productId");


        String skuIds = param.getString("skuIds");
        if (StringUtils.isBlank(skuIds)){
            return Result.fail(102,"参数错误","skuId不能为空");
        }
        Map<String,Object> skuIdMap = new HashMap<>();
        if (skuIds.contains(",")){
            String[] split = StringUtils.split(skuIds,",");
            for (String s :split) {
                String[] idAndPrice = StringUtils.split(s, "~");
                if (idAndPrice[0].length()<22){
                    return Result.fail(102,"参数错误","skuId长度不正确");
                }
                BigDecimal price;
                try {
                    price = new BigDecimal(idAndPrice[1]);
                }catch (Exception e){
                    return Result.fail(102,"参数错误","转换拼团商品sku出错,错误原因: "+e.getCause());
                }
                if (price==null||price.compareTo(new BigDecimal(0))<=0){
                    return Result.fail(102,"参数错误","skuId价格不能小于0");
                }
                skuIdMap.put(idAndPrice[0],price);
            }
        }else{
            String[] idAndPrice = StringUtils.split(skuIds, "~");
            if (idAndPrice[0].length()!=22){
                return Result.fail(102,"参数错误","skuId长度不正确");
            }
            BigDecimal price;
            try {
                price = new BigDecimal(idAndPrice[1]);
            }catch (Exception e){
                return Result.fail(102,"参数错误","转换拼团商品sku出错,错误原因: "+e.getCause());
            }
            if (price==null||price.compareTo(new BigDecimal(0))<=0){
                return Result.fail(102,"参数错误","skuId价格不能小于0");
            }
            skuIdMap.put(idAndPrice[0],price);
        }

        if (skuIdMap==null||skuIdMap.isEmpty()){
            return Result.fail(102,"参数错误","sku不能为空");
        }

        param.remove("skuIds");

        AssembleProduct assembleProduct;

        try {
            assembleProduct = JSON.toJavaObject(param, AssembleProduct.class);
            if (assembleProduct == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
            if (assembleProduct.getFullSize()<2){
                return Result.fail(102,"参数错误","拼团人数不能小于2");
            }
            Date endTime = assembleProduct.getEndTime();
            if (endTime==null){
                return Result.fail(102,"参数错误","拼团商品结束时间不能为空");
            }
            Date nowTime = new Date();
            if (nowTime == MoonUtil.contrastTime(nowTime,endTime)){
                return Result.fail(102,"参数错误","拼团商品结束时间不能小于当前时间");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }

        return assembleProductService.addAssembleProduct(assembleProduct,productId,skuIdMap);
    }

    /**
     * 根据ID删除AssembleProduct
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject deleteAssembleProduct(@PathVariable String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()<22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return assembleProductService.deleteAssembleProductById(id);
    }

    /**
     * 根据新AssembleProduct更新id已存在的AssembleProduct
     * @param id,newAssembleProduct
     */
    @PutMapping(value = "/{id}")
    @ResponseBody
    public JSONObject updateAssembleProduct(@PathVariable("id") String id,@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.PRODUCT_UPDATE)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        AssembleProduct newAssembleProduct;
        try {
            newAssembleProduct = JSON.toJavaObject(param, AssembleProduct.class);
            if (newAssembleProduct == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }
        return assembleProductService.updateAssembleProductById(id,newAssembleProduct);
    }

    /**
     * 根据id获取AssembleProduct
     * @param id
     */
    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject getAssembleProductById(@PathVariable("id") String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.PRODUCT_READ)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        AccessToken token = accessTokenService.findAccessTokenByIdAndIsValid(authorization);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        return assembleProductService.findAssembleProductById(token.getUserId(),id);
    }

    /**
     * 分页获取AssembleProduct列表
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody JSONObject findAssembleProductByPager(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValid(authorization)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        AccessToken token = accessTokenService.findAccessTokenByIdAndIsValid(authorization);
        if (token==null){
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

        return assembleProductService.findAssembleProductByList(keyword,token.getUserId(),orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 导出AssembleProductExcel表格
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){
        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出AssembleProduct列表时出错,错误原因: "+e.getCause());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);

        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出AssembleProductExcel列表时出错" + e.getCause());
                e.printStackTrace();
            }
            return;
        }
        assembleProductService.exportExcel(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    }

    /**
     * 〈拼团商品上下移〉
     *
     * @param param
     * @return:
     * @since: 1.0.0
     * @Author: Revisit-Moon
     * @Date: 2019/3/4 4:49 PM
     */
    @PutMapping(value = "/sort/{id}")
    public @ResponseBody JSONObject assembleProductSortUpOrDown(@PathVariable String id,@RequestBody JSONObject param) {
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        String upOrDown = param.getString("upOrDown");
        if (StringUtils.isBlank(id)||id.length()!=22||StringUtils.isBlank(upOrDown)||(!upOrDown.equals("上移")&&!upOrDown.equals("下移"))){
            return Result.fail(102,"参数错误","必填参数不能为空,或不正确的参数");
        }
        return assembleProductService.assembleProductSortUpOrDown(id, upOrDown);
    }
}