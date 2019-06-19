/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: FreeMarkerTemplateUtil
 * Author:   Revisit-Moon
 * Date:     2019/2/25 5:26 PM
 * Description: FreeMarkerTemplateUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/25 5:26 PM        1.0              描述
 */

package com.revisit.springboot.utils;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;

/**
 * 〈FreeMarkerTemplateUtil〉
 *
 * @author Revisit-Moon
 * @create 2019/2/25
 * @since 1.0.0
 */

public class FreeMarkerTemplateUtil {
    private FreeMarkerTemplateUtil(){

    }
    private static final Configuration CONFIGURATION = new Configuration();

    static{
        //这里比较重要，用来指定加载模板所在的路径
        CONFIGURATION.setTemplateLoader(new ClassTemplateLoader(FreeMarkerTemplateUtils.class, "/codeGeneratorTemplates"));
        CONFIGURATION.setDefaultEncoding("UTF-8");
        CONFIGURATION.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        // CONFIGURATION.setCacheStorage(NullCacheStorage.INSTANCE);
    }

    public static Template getTemplate(String templateName) throws IOException {
        try {
            return CONFIGURATION.getTemplate(templateName);
        } catch (IOException e) {
            throw e;
        }
    }

    public static void clearCache() {
        CONFIGURATION.clearTemplateCache();
    }
}
