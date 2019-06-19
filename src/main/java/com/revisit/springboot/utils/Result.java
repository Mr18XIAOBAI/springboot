/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: Result
 * Author:   Revisit-Moon
 * Date:     2019/1/29 12:19 PM
 * Description: utils.Result
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/29 12:19 PM        1.0              描述
 */

package com.revisit.springboot.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * 〈utils.Result〉
 *
 * @author Revisit-Moon
 * @create 2019/1/29
 * @since 1.0.0
 */
@Data
public class Result {
    private final static Logger logger = LoggerFactory.getLogger(Result.class);

    private Integer code;               //返回码
    private String msg;                 //返回消息
    private Object data;                //返回实体
    private String errorMsg;            //错误信息
    private String token;               //会话token

    public Result(Integer code, String msg, Object data,String token) {
        this.msg = msg;
        this.code = code;
        this.data = data;
        this.token = token;
    }

    public Result(Integer code, String msg, String errorMsg,String token) {
        this.msg = msg;
        this.code = code;
        this.errorMsg = errorMsg;
        this.token = token;
    }

    public Result(Integer code, String msg, Object data) {
        this.msg = msg;
        this.code = code;
        this.data = data;
    }

    public Result(Integer code, String msg, String errorMsg) {
        this.msg = msg;
        this.code = code;
        this.errorMsg = errorMsg;
    }

    public static JSONObject success(Integer code, String msg, Object data) {
        JSONObject result = new JSONObject();
        result.put("code",code);
        result.put("msg",msg);
        result.put("data",data);
        return result;
    }

    public static JSONObject fail(Integer code,String msg,String errorMsg){
        JSONObject result = new JSONObject();
        result.put("code",code);
        result.put("msg",msg);
        result.put("errorMsg",errorMsg);
        try {
            TransactionStatus transactionStatus = TransactionAspectSupport.currentTransactionStatus();
            if (transactionStatus!=null) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }catch (Exception e){
            logger.info("回滚事务异常: "+e.getCause());
        }
        logger.info(result.toJSONString());
        return result;
    }

    public static JSONObject tokenFail(Integer code,String msg,String errorMsg,String token) {
        JSONObject result = new JSONObject();
        result.put("accessToken",token);
        result.put("code",code);
        result.put("msg",msg);
        result.put("errorMsg",errorMsg);
        return result;
    }

    public static JSONObject tokenSuccess(Integer code,String msg,Object data,String token) {
        JSONObject result = new JSONObject();
        result.put("accessToken",token);
        result.put("code",code);
        result.put("msg",msg);
        result.put("data",data);
        return result;
    }
}
