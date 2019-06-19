/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: CookiceUtil
 * Author:   Revisit-Moon
 * Date:     2019/2/20 4:22 PM
 * Description: CookiceUtil
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/20 4:22 PM        1.0              描述
 */

package com.revisit.springboot.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 〈CookiceUtil〉
 *
 * @author Revisit-Moon
 * @create 2019/2/20
 * @since 1.0.0
 */

public class CookiesUtil {

    public static String getJSessionId(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if (cookies!=null&&cookies.length>0){
            for (int i = 0; i <cookies.length ; i++) {
                if (cookies[i].getName().equals("JSESSIONID"));
                return cookies[i].getValue();
            }
        }
        return null;

    }
}
