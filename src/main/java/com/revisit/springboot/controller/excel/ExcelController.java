/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: ExcelController
 * Author:   Revisit-Moon
 * Date:     2019/1/30 3:07 PM
 * Description: excel.ExcelController
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/1/30 3:07 PM        1.0              描述
 */

package com.revisit.springboot.controller.excel;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.revisit.springboot.entity.user.User;
import com.revisit.springboot.service.user.UserService;
import com.revisit.springboot.utils.ExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 〈excel.ExcelController〉
 *
 * @author Revisit-Moon
 * @create 2019/1/30
 * @since 1.0.0
 */
@Controller
@RequestMapping("/api/export")
public class ExcelController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserService userService;

    @RequestMapping("importExcel")
    public void importExcel(){
        String filePath = "F:\\用户.xls";
        //解析excel，
        List<User> userList = ExcelUtil.importExcel(filePath,1,1,User.class);
        //也可以使用MultipartFile,使用 FileUtil.importExcel(MultipartFile file, Integer titleRows, Integer headerRows, Class<T> pojoClass)导入
        System.out.println("导入数据一共【"+userList.size()+"】行");

        //TODO 保存数据库
    }
}
