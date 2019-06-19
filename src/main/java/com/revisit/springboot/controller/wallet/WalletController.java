// package com.revisit.springboot.controller.wallet;
//
// import com.alibaba.fastjson.JSON;
// import com.alibaba.fastjson.JSONObject;
// import com.revisit.springboot.entity.token.AccessToken;
// import com.revisit.springboot.service.accesstoken.AccessTokenService;
// import com.revisit.springboot.service.user.UserService;
// import com.revisit.springboot.service.wallet.WalletService;
// import com.revisit.springboot.utils.AuthorityUtil;
// import com.revisit.springboot.utils.MoonUtil;
// import com.revisit.springboot.utils.Result;
// import org.apache.commons.lang3.StringUtils;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;
//
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.util.Date;
// import java.util.List;
//
// /**
// * Wallet访问控制层
// * @author Revisit-Moon
// * @date 2019-04-20 18:57:43
// */
// @RestController
// @RequestMapping("/api/wallet")
// public class WalletController {
//
//     @Autowired
//     private AccessTokenService accessTokenService;
//
//     @Autowired
//     private UserService userService;
//
//     @Autowired
//     private  HttpServletRequest request;
//
//     @Autowired
//     private WalletService walletService;
//
//     private final static Logger logger = LoggerFactory.getLogger(WalletController.class);
//
//     /**
//      * 新增Wallet
//      * @param param
//      */
//     // @PostMapping(value = "/add")
//     // public @ResponseBody JSONObject addWallet(@RequestBody JSONObject param){
//     //     String authorization = request.getHeader("Authorization");
//     //     if (!accessTokenService.isValid(authorization)){
//     //        return Result.fail(108,"权限认证失败","您没有权限或token过期");
//     //     }
//     //
//     //     Wallet wallet;
//     //
//     //     try {
//     //         wallet = JSON.toJavaObject(param, Wallet.class);
//     //         if (wallet == null){
//     //             return Result.fail(102,"参数错误","必填参数不能为空");
//     //         }
//     //     }catch (Exception e){
//     //         return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
//     //     }
//     //
//     //     return walletService.addWallet(wallet);
//     // }
//
//     /**
//      * 根据ID删除Wallet
//      * @param id
//      */
//     // @DeleteMapping(value = "/{id}")
//     // public @ResponseBody JSONObject deleteWallet(@PathVariable String id){
//     //     String authorization = request.getHeader("Authorization");
//     //     if (!accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)){
//     //         return Result.fail(108,"权限认证失败","您没有权限或token过期");
//     //     }
//     //     if (id==null||StringUtils.isBlank(id)||id.length()<22){
//     //         return Result.fail(102,"参数错误","必填参数不能为空");
//     //     }
//     //     return walletService.deleteWalletById(id);
//     // }
//
//     /**
//      * 根据新Wallet更新id已存在的Wallet
//      * @param id,newWallet
//      */
//     // @PutMapping(value = "/{id}")
//     // @ResponseBody
//     // public JSONObject updateWallet(@PathVariable("id") String id,@RequestBody JSONObject param){
//     //     String authorization = request.getHeader("Authorization");
//     //     if (!accessTokenService.isValidTokenAndValidAuthority(authorization,AuthorityUtil.WALLET_UPDATE)){
//     //         return Result.fail(108,"权限认证失败","您没有权限或token过期");
//     //     }
//     //     if (id==null||StringUtils.isBlank(id)||id.length()!=22){
//     //         return Result.fail(102,"参数错误","必填参数不能为空");
//     //     }
//     //     Wallet newWallet;
//     //     try {
//     //         newWallet = JSON.toJavaObject(param, Wallet.class);
//     //         if (newWallet == null){
//     //             return Result.fail(102,"参数错误","必填参数不能为空");
//     //         }
//     //     }catch (Exception e){
//     //         return Result.fail(102,"参数错误","转换实体类失败,错误信息: "+e.getCause());
//     //     }
//     //     return walletService.updateWalletById(id,newWallet);
//     // }
//
//     /**
//      * 根据用户id获取Wallet
//      * @param userId
//      */
//     @GetMapping(value = "/{userId}")
//     public @ResponseBody JSONObject getWalletById(@PathVariable("userId") String userId){
//         String authorization = request.getHeader("Authorization");
//         if (!accessTokenService.isValidTokenAndValidAuthority(authorization, AuthorityUtil.WALLET_READ)){
//             return Result.fail(108,"权限认证失败","您没有权限或token过期");
//         }
//         AccessToken token = accessTokenService.findAccessTokenByIdAndIsValid(authorization);
//         if (token==null){
//             return Result.fail(108,"权限认证失败","您没有权限或token过期");
//         }
//         if (userId==null||StringUtils.isBlank(userId)||userId.length()!=22){
//             return Result.fail(102,"参数错误","必填参数不能为空");
//         }
//         if (!userId.equals(token.getUserId())){
//             return Result.fail(102,"参数错误","非法操作");
//         }
//         return walletService.findWalletByUserId(userId);
//     }
//
//     /**
//      * 分页获取Wallet列表
//      * @param param
//      */
//     @PostMapping(value = "/list")
//     public @ResponseBody JSONObject findWalletByPager(@RequestBody JSONObject param){
//         String authorization = request.getHeader("Authorization");
//         if (!accessTokenService.isValidTokenAndRole(authorization,AuthorityUtil.SUPER_ADMIN)){
//             return Result.fail(108,"权限认证失败","您没有权限或token过期");
//         }
//         String keyword = param.getString("keyword");
//         String orderBy = param.getString("orderBy");
//
//         List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(param.getString("timeRange"));
//         if (timeRangeDate == null || timeRangeDate.isEmpty()) {
//             return Result.fail(102, "参数错误", "日期格式化异常");
//         }
//
//         Integer page = param.getInteger("page");
//         Integer rows = param.getInteger("rows");
//
//         return walletService.findWalletByList(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),page,rows);
//     }
//
//     /**
//      * 导出WalletExcel表格
//      * @param keyword,orderBy,timeRange,response
//      */
//     @GetMapping(value = "/export")
//     public void export(String keyword,String orderBy,String timeRange,String token,HttpServletResponse response){
//         if (!accessTokenService.isValidTokenAndRole(token,AuthorityUtil.SUPER_ADMIN)){
//             try {
//                 JSONObject result = (JSONObject) JSON.toJSON(Result.fail(108,"权限认证失败","您没有权限或token过期"));
//                 response.getWriter().write(result.toJSONString());
//             }catch (Exception e){
//                 logger.info("导出Wallet列表时出错,错误原因: "+e.getCause());
//             }
//             return;
//         }
//
//         List<Date> timeRangeDate = MoonUtil.getTimeRangeDate(timeRange);
//
//         if (timeRangeDate==null||timeRangeDate.isEmpty()) {
//             JSONObject result = (JSONObject) JSON.toJSON(Result.fail(102,"参数错误","日期格式化异常"));
//             try {
//                 response.getWriter().write(result.toJSONString());
//             }catch (Exception e){
//                 logger.info("导出WalletExcel列表时出错" + e.getCause());
//                 e.printStackTrace();
//             }
//             return;
//         }
//         walletService.exportExcel(keyword,orderBy,timeRangeDate.get(0),timeRangeDate.get(1),response);
//     }
//
// }