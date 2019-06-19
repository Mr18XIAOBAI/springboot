package com.revisit.springboot.controller.shop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.utils.Result;
import com.revisit.springboot.utils.MoonUtil;
import com.revisit.springboot.utils.AuthorityUtil;
import com.revisit.springboot.service.shop.ShopService;
import com.revisit.springboot.entity.shop.Shop;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* Shop访问控制层
* @author Revisit-Moon
* @date 2019-06-10 16:14:48
*/
@RestController
@RequestMapping("/api/shop")
public class ShopController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private ShopService shopService;

    private final static Logger logger = LoggerFactory.getLogger(ShopController.class);

    /**
     * 新增Shop
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody JSONObject addShop(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.SHOP_ADD);
        if (token == null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        Shop shop;

        try {
            shop = JSON.toJavaObject(param, Shop.class);
            if (shop == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }

            if (StringUtils.isBlank(shop.getName())){
                return Result.fail(102,"参数错误","店铺名称不能为空");
            }
            shop.setUserId(token.getUserId());

        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getMessage());
        }

        return shopService.addShop(shop);
    }

    /**
     * 根据ID删除Shop
     * @param id
     */
    @DeleteMapping(value = "/{id}")
    public @ResponseBody JSONObject deleteShop(@PathVariable String id,@RequestBody JSONObject param){
        if (id==null||StringUtils.isBlank(id)||id.length()<22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.SUPER_ADMIN);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        return shopService.deleteShopById(id);
    }

    /**
     * 根据新Shop更新id已存在的Shop
     * @param id,newShop
     */
    @PutMapping(value = "/{id}")
    public @ResponseBody JSONObject updateShop(@PathVariable("id") String id,@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.SHOP_UPDATE);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        Shop newShop;
        try {
            newShop = JSON.toJavaObject(param, Shop.class);
            String userSendId = newShop.getUserId();
            if (StringUtils.isBlank(userSendId)){
                return Result.fail(102,"参数错误","必填参数不能为空");

            }
            if (!token.getUserId().equals(userSendId)){
                if (accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.SHOP_UPDATE)==null){
                    newShop.setUserId(token.getUserId());
                }
            }

        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getMessage());
        }
        return shopService.updateShopById(id,newShop);
    }

    /**
     * 根据id获取Shop
     * @param id
     */
    @GetMapping(value = "/{id}")
    public @ResponseBody JSONObject getShopById(@PathVariable("id") String id){
        String authorization = request.getHeader("Authorization");
        if (accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.SHOP_READ)==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        if (id==null||StringUtils.isBlank(id)||id.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return shopService.findShopById(id);
    }

    /**
     * 根据token获取我的店铺
     * @param
     */
    @GetMapping(value = "/myShop")
    public @ResponseBody JSONObject getMyShop(){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.findAccessTokenByIdAndIsValid(authorization);
        if (token == null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }
        return shopService.findShopByUserId(token.getUserId());
    }

    /**
     * 分页获取Shop列表
     * @param param
     */
    @PostMapping(value = "/list")
    public @ResponseBody JSONObject findShopByPager(@RequestBody JSONObject param){
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

        return shopService.findShopByList(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    }

    /**
     * 导出ShopExcel表格
     * @param keyword,orderBy,timeRange,response
     */
    @GetMapping(value = "/export")
    public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){
        if (accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)==null){
            try {
                JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出Shop列表时出错,错误原因: "+e.getMessage());
            }
            return;
        }

        List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);

        if (timeRangeDate==null||timeRangeDate.isEmpty()) {
            JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
            try {
                response.getWriter().write(result.toJSONString());
            }catch (Exception e){
                logger.info("导出ShopExcel列表时出错" + e.getMessage());
                e.printStackTrace();
            }
            return;
        }
        shopService.exportExcel(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    }

}