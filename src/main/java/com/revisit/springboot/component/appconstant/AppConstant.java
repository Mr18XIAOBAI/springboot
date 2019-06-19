/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: AppConstant
 * Author:   Revisit-Moon
 * Date:     2019/5/8 3:43 PM
 * Description: appconstant.AppConstant
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/5/8 3:43 PM        1.0              描述
 */

package com.revisit.springboot.component.appconstant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.io.File;

/**
 * 〈appconstant.AppConstant〉
 *
 * @author Revisit-Moon
 * @create 2019/5/8
 * @since 1.0.0
 */
@Component
public class AppConstant {

    private final Logger logger = LoggerFactory.getLogger(AppConstant.class);

    public static String FILE_SAVE_PATH;

    @Value("${FILE_SAVE_PATH}")
    public void setFILE_SAVE_PATH(String fileSavePath){
        this.FILE_SAVE_PATH = fileSavePath;
    }
    public static String FILE_FIND_PATH;

    public AppConstant() {
        try {
            FILE_FIND_PATH = new File(ClassUtils.getDefaultClassLoader().getResource("").getPath()).getParentFile().getParentFile().getCanonicalPath();
            logger.info("获取项目地址成功: "+FILE_FIND_PATH);
        }catch (Exception e){
            logger.error("获取项目地址失败",e);
            FILE_FIND_PATH = "";
        }
    }
}
