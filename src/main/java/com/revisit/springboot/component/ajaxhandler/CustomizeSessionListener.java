/**
 * Copyright (C), 2015-2019, 美果科技有限公司
 * FileName: CustomizeSessionContext
 * Author:   Revisit-Moon
 * Date:     2019/2/2 7:03 PM
 * Description: CustomizeSessionContext
 * History:
 * <author>          <time>          <version>          <desc>
 * Revisit       2019/2/2 7:03 PM        1.0              描述
 */

package com.revisit.springboot.component.ajaxhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * 〈CustomizeSessionContext〉
 *
 * @author Revisit-Moon
 * @create 2019/2/2
 * @since 1.0.0
 */
@Component
@WebListener
public class CustomizeSessionListener implements HttpSessionListener {

    private final static Logger logger = LoggerFactory.getLogger(CustomizeSessionListener.class);

    private static String sessionCreateId = "";

    private static String sessionDestroyedId = "";

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        if (!sessionCreateId.equals(se.getSession().getId())) {
            logger.info("session创建: " + se.getSession().getId());
        }
        sessionCreateId = se.getSession().getId();
        // HttpSession session = se.getSession();
        // ServletContext application = session.getServletContext();
        // HashSet<HttpSession> sessions = (HashSet<HttpSession>) application.getAttribute("sessions");
        // if (sessions == null) {
        //     sessions = new HashSet<>();
        //     application.setAttribute("sessions", sessions);
        // }
        // sessions.add(session);
        // CustomizeSessionContext.createSession(se.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        if (!sessionDestroyedId.equals(se.getSession().getId())) {
            logger.info("session销毁: " + se.getSession().getId());
        }
        sessionDestroyedId = se.getSession().getId();

        // HttpSession session = se.getSession();
        // ServletContext application = session.getServletContext();
        // HashSet<?> sessions = (HashSet<?>) application.getAttribute("sessions");
        // // 销毁的session均从HashSet集中移除
        // sessions.remove(session);
        // CustomizeSessionContext.deleteSession(se.getSession());
    }
}
