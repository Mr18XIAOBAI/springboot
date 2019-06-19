/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: BasicController
 * Author:   Revisit-Moon
 * Date:     2019/1/28 5:52 PM
 * Description: BasicController
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/28 5:52 PM        1.0              描述
 */

package com.revisit.springboot.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 〈BasicController〉
 *
 * @author Revisit-Moon
 * @create 2019/1/28
 * @since 1.0.0
 */
@Controller
public class BasicController {

    @GetMapping(value = "/")
    public String welcome(){
        return "index";
    }

    @PostMapping(value = "/test")
    public @ResponseBody JSONObject testCORS(){
        JSONObject result = new JSONObject();
        result.put("code",200);
        result.put("msg","成功");
        return result;
    }
}
