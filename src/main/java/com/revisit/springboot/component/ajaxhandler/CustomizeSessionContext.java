/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: CustomizeSession
 * Author:   Revisit-Moon
 * Date:     2019/2/2 7:30 PM
 * Description: CustomizeSessionContext
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/2 7:30 PM        1.0              描述
 */

package com.revisit.springboot.component.ajaxhandler;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 〈CustomizeSessionContext〉
 *
 * @author Revisit-Moon
 * @create 2019/2/2
 * @since 1.0.0
 */

public class CustomizeSessionContext {

    private static HashMap<String,HttpSession> sessionMap = new HashMap<>();

    public static synchronized void createSession(HttpSession session) {
        if (session != null) {
            sessionMap.put(session.getId(), session);
        }
    }
    public static synchronized void deleteSession(HttpSession session) {
        if (session != null) {
            sessionMap.remove(session.getId());
            session.invalidate();
        }
    }
    public static synchronized HttpSession getSession(String sessionId) {
        if (sessionId == null)
            return null;
        return sessionMap.get(sessionId);
    }

    public static synchronized HttpSession getSessionAttribute(String key) {
        for (Map.Entry entry : sessionMap.entrySet()) {
            HttpSession session = getSession(entry.getKey().toString());
            if (session.getAttribute(key)!=null){
                return session;
            }
        }
        return null;
    }
}
