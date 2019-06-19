// package com.revisit.springboot.service.wallet;
//
// import com.alibaba.fastjson.JSONObject;
// import com.revisit.springboot.entity.orderform.OrderForm;
// import com.revisit.springboot.entity.user.User;
// import com.revisit.springboot.entity.wallet.Wallet;
//
// import javax.servlet.http.HttpServletResponse;
// import java.util.Date;
//
// /**
//  * Wallet接口类
//  * @author Revisit-Moon
//  * @date 2019-04-20 18:57:43
//  */
// public interface WalletService {
//
//     //新增Wallet
//     JSONObject addWallet(Wallet wallet);
//
//     //根据ID删除Wallet
//     JSONObject deleteWalletById(String id);
//
//     //根据新Wallet更新id已存在的Wallet
//     JSONObject updateWalletById(String id, Wallet newWallet);
//
//     //根据ID获取Wallet
//     JSONObject findWalletByUserId(String userId);
//
//     Wallet findByUserId(String userId);
//
//     //根据ids集合批量获取Wallet
//     //JSONObject findWalletListByIds(List<String> ids);
//
//     //分页获取Wallet列表
//     JSONObject findWalletByList(String keyword, String orderBy, Date beginTime, Date endTime, Integer page, Integer rows);
//     //导出excel表格
//     void exportExcel(String keyword, String orderBy, Date beginTime, Date endTime, HttpServletResponse response);
//
//     //钱包支付
//     JSONObject walletPay(OrderForm orderForm);
//
//     //判断是否要返回充值奖励
//     JSONObject referrerReward(User user, OrderForm orderForm, User referrerUser);
//
//     //判断是否要返回推荐人消费奖励
//     JSONObject buyReward(User user, OrderForm orderForm, User referrerUser);
//
//     //计算返佣奖励
//     JSONObject calculateRebate(User user, OrderForm orderForm, User referrerUser);
// }