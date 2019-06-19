package com.revisit.springboot.controller.assembleproductsku;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.assembleproductsku.AssembleProductSku;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.assembleproductsku.AssembleProductSkuService;
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
 * ProductSku访问控制层
 * @author Revisit-Moon
 * @date 2019-03-03 15:28:24
 */
@RestController
@RequestMapping("/api/assembleProductSku")
public class AssembleProductSkuController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserService userService;
    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private AssembleProductSkuService assembleProductSkuService;

    private final static Logger logger = LoggerFactory.getLogger(AssembleProductSkuController.class);

    /**
     * 新增ProductSku
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody JSONObject addProductSku(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValid(authorization)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        AssembleProductSku assembleProductSku;

        try {
            assembleProductSku = JSON.toJavaObject(param, AssembleProductSku.class);
            if (assembleProductSku == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
            String assembleProductId = assembleProductSku.getAssembleProductId();
            if (StringUtils.isBlank(assembleProductId)){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
            if (StringUtils.isBlank(assembleProductSku.getSkuName())){
                return Result.fail(102,"参数错误","sku名称不能为空");
            }

            if (StringUtils.isBlank(assembleProductSku.getSkuUnit())){
                return Result.fail(102,"参数错误","sku单位不能为空");
            }

            if (assembleProductSku.getSkuPrice()==null){
                return Result.fail(102,"参数错误","sku价格不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }

        return assembleProductSkuService.addAssembleProductSku(assembleProductSku);
    }

    /**
     * 根据ID删除ProductSku
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject deleteProductSku(@PathVariable String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization, AuthorityUtil.SUPER_ADMIN)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()<22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return assembleProductSkuService.deleteAssembleProductSkuById(id);
    }

    /**
     * 根据新ProductSku更新id已存在的ProductSku
     * @param id,newAssembleProductSku
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public JSONObject updateProductSku(@PathVariable("id") String id,@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.PRODUCT_UPDATE)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        AssembleProductSku newAssembleProductSku;
        try {
            newAssembleProductSku = JSON.toJavaObject(param, AssembleProductSku.class);
            if (newAssembleProductSku == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }
        return assembleProductSkuService.updateAssembleProductSkuById(id,newAssembleProductSku);
    }

    /**
     * 根据id获取ProductSku
     * @param id
     */
    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject getProductSkuById(@PathVariable("id") String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.PRODUCT_READ)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return assembleProductSkuService.findAssembleProductSkuById(id);
    }

    /**
     * 分页获取ProductSku列表
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody JSONObject findProductSkuByPager(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null){
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

        return assembleProductSkuService.findAssembleProductSkuByList(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 导出ProductSkuExcel表格
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){
        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出ProductSku列表时出错,错误原因: "+e.getCause());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);

        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出ProductSkuExcel列表时出错" + e.getCause());
                e.printStackTrace();
            }
            return;
        }
        assembleProductSkuService.exportExcel(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    }

    /**
     * 〈商品Sku上下移〉
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
        if (accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)==null) {
            return Result.fail(108, "权限认证失败", "您没有权限或token过期");
        }
        String upOrDown = param.getString("upOrDown");
        if (StringUtils.isBlank(id)||id.length()!=22||StringUtils.isBlank(upOrDown)||(!upOrDown.equals("上移")&&!upOrDown.equals("下移"))){
            return Result.fail(102,"参数错误","必填参数不能为空,或不正确的参数");
        }
        return assembleProductSkuService.assembleProductSkuSortUpOrDown(id, upOrDown);
    }
}