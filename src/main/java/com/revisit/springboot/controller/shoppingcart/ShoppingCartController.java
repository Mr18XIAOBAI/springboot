package com.revisit.springboot.controller.shoppingcart;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.shoppingcart.ShoppingCartItem;
import com.revisit.springboot.entity.token.AccessToken;
import com.revisit.springboot.service.accesstoken.AccessTokenService;
import com.revisit.springboot.service.shoppingcart.ShoppingCartService;
import com.revisit.springboot.service.user.UserService;
import com.revisit.springboot.utils.AuthorityUtil;
import com.revisit.springboot.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
* ShoppingCart访问控制层
* @author Revisit-Moon
* @date 2019-04-15 11:05:45
*/
@RestController
@RequestMapping("/api/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private ShoppingCartService shoppingCartService;

    private final static Logger logger = LoggerFactory.getLogger(ShoppingCartController.class);

    /**
     * 新增ShoppingCart
     * @param param
     */
    @PostMapping(value = "/add")
    public @ResponseBody JSONObject addShoppingCart(@RequestBody JSONObject param){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.SHOPPING_CART_ADD);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        ShoppingCartItem shoppingCartItem;

        String userId = "";
        try {
            shoppingCartItem = JSON.toJavaObject(param, ShoppingCartItem.class);
            if (shoppingCartItem == null){
                return Result.fail(102,"参数错误","必填参数不能为空");
            }

            String skuId = shoppingCartItem.getProductSkuId();
            if (StringUtils.isBlank(skuId)){
                return Result.fail(102,"参数错误","skuId不能为空");
            }
            int quantity = shoppingCartItem.getQuantity();
            if (quantity==0){
                return Result.fail(102,"参数错误","数量不能为0");
            }


            userId = token.getUserId();
        }catch (Exception e){
            return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
        }

        return shoppingCartService.addShoppingCart(userId,shoppingCartItem);
    }

    /**
     * 根据productSkuId删除ShoppingCartItem
     * @param productSkuId
     */
    @DeleteMapping(value = "/{productSkuId}")
    public @ResponseBody JSONObject deleteShoppingCartByProductSkuId(@PathVariable("productSkuId") String productSkuId){
        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndAuthorities(authorization,AuthorityUtil.SHOPPING_CART_DELETE);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");

        }
        String userId = token.getUserId();
        if (userId==null||StringUtils.isBlank(userId)||userId.length()<22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }
        return shoppingCartService.deleteShoppingCartItemByProductSkuId(userId,productSkuId);
    }

    // /**
    //  * 根据新ShoppingCart更新id已存在的ShoppingCart
    //  * @param id,newShoppingCart
    //  */
    // @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    // @ResponseBody
    // public JSONObject updateShoppingCart(@PathVariable("id") String id,@RequestBody JSONObject param){
    //     String authorization = request.getHeader("Authorization");
    //     if (!accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.SHOPPING_CART_UPDATE)){
    //         return Result.fail(108,"权限认证失败","您没有权限或token过期");
    //     }
    //     if (id==null||StringUtils.isBlank(id)||id.length()!=22){
    //         return Result.fail(102,"参数错误","必填参数不能为空");
    //     }
    //     ShoppingCart newShoppingCart;
    //     try {
    //         newShoppingCart = JSON.toJavaObject(param, ShoppingCart.class);
    //         if (newShoppingCart == null){
    //             return Result.fail(102,"参数错误","必填参数不能为空");
    //         }
    //     }catch (Exception e){
    //         return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
    //     }
    //     return shoppingCartService.updateShoppingCartById(id,newShoppingCart);
    // }

    /**
     * 增加或减少购物车商品
     * @param param
     */
    @PostMapping(value = "/andSubtract")
    @ResponseBody
    public JSONObject andSubtractShoppingCart(@RequestBody JSONObject param){
        String productSkuId = param.getString("productSkuId");
        if(StringUtils.isBlank(productSkuId)||productSkuId.length()<22){
            return Result.fail(102,"参数错误","商品skuId不能为空");
        }
        Integer quantity = param.getInteger("quantity");

        Boolean isAdd = param.getBoolean("isAdd");
        if (isAdd!=null&&quantity!=null){
            return Result.fail(102,"参数错误","只能输入其中一项参数");
        }

        String authorization = request.getHeader("Authorization");
        AccessToken token = accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.SHOPPING_CART_ADD);
        if (token==null){
            return Result.fail(108,"权限认证失败","您没有权限或token过期");
        }

        return shoppingCartService.andSubtractShoppingCart(token.getUserId(),productSkuId,isAdd,quantity);
    }

    /**
     * 根据id获取ShoppingCart
     * @param userId
     */
    @GetMapping(value = "/{userId}")
    public @ResponseBody JSONObject getShoppingCartById(@PathVariable("userId") String userId){
        String authorization = request.getHeader("Authorization");
        if (userId==null||StringUtils.isBlank(userId)||userId.length()!=22){
            return Result.fail(102,"参数错误","必填参数不能为空");
        }

        AccessToken token = accessTokenService.isValidTokenAndAuthorities(authorization,AuthorityUtil.SHOPPING_CART_READ);
        if (!token.getUserId().equals(userId)){
            return Result.fail(102,"参数错误","非法操作");
        }
        return shoppingCartService.findShoppingCartByUserId(userId);
    }

    /**
     * 分页获取ShoppingCart列表
     * @param param
     */
    // @PostMapping(value = "/list")
    // public @ResponseBody JSONObject findShoppingCartByPager(@RequestBody JSONObject param){
    //     String authorization = request.getHeader("Authorization");
    //     if (!accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)){
    //         return Result.fail(108,"权限认证失败","您没有权限或token过期");
    //     }
    //     String keyword = param.getString("keyword");
    //     String orderBy = param.getString("orderBy");
    //
    //     List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
    //     if (timeRangeDate == null || timeRangeDate.isEmpty()) {
    //         return Result.fail(102, "参数错误", "日期格式化异常");
    //     }
    //
    //     Integer page = param.getInteger("page");
    //     Integer rows = param.getInteger("rows");
    //
    //     return shoppingCartService.findShoppingCartByList(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
    // }

    /**
     * 导出ShoppingCartExcel表格
     * @param keyword,orderBy,timeRange,response
     */
    // @GetMapping(value = "/export")
    // public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){
    //     if (!accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)){
    //         try {
    //             JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
    //             response.getWriter().write(result.toJSONString());
    //         }catch (Exception e){
    //             logger.info("导出ShoppingCart列表时出错,错误原因: "+e.getCause());
    //         }
    //         return;
    //     }
    //
    //     List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);
    //
    //     if (timeRangeDate==null||timeRangeDate.isEmpty()) {
    //         JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
    //         try {
    //             response.getWriter().write(result.toJSONString());
    //         }catch (Exception e){
    //             logger.info("导出ShoppingCartExcel列表时出错" + e.getCause());
    //             e.printStackTrace();
    //         }
    //         return;
    //     }
    //     shoppingCartService.exportExcel(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
    // }

}