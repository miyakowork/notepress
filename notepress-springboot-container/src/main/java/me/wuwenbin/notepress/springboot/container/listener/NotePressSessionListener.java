package me.wuwenbin.notepress.springboot.container.listener;

import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.constants.NotePressConstants;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.mapper.SysSessionMapper;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @author wuwen
 */
@Slf4j
public class NotePressSessionListener implements HttpSessionListener, HttpSessionAttributeListener {

    private static final SysSessionMapper SYS_SESSION_MAPPER = NotePressUtils.getBean(SysSessionMapper.class);

    /**
     * Notification that a session was created.
     *
     * @param se the notification event
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        log.info("会话创建，用户：{}", se.getSession().getAttribute(NotePressConstants.SESSION_USER_KEY));
    }

    /**
     * Notification that a session is about to be invalidated.
     *
     * @param se the notification event
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        SysUser sessionUser = (SysUser) se.getSession().getAttribute(NotePressConstants.SESSION_USER_KEY);
        if (sessionUser != null) {
            log.info("会话销毁，用户：{}", sessionUser);
            try {
                SYS_SESSION_MAPPER.deleteBySessionUserId(sessionUser.getId());
            } catch (Exception ignored) {
            }
        } else {
            log.info("会话销毁，用户为 null，不需要清除！");
        }
    }


    /**
     * Notification that an attribute has been added to a session. Called after
     * the attribute is added.
     *
     * @param se Information about the added attribute
     */
    @Override
    public void attributeAdded(HttpSessionBindingEvent se) {
        log.info("source:{}", se.getSource());
        log.info("add --> {}:{}", se.getName(), se.getValue());
    }

    /**
     * Notification that an attribute has been removed from a session. Called
     * after the attribute is removed.
     *
     * @param se Information about the removed attribute
     */
    @Override
    public void attributeRemoved(HttpSessionBindingEvent se) {
        log.info("source:{}", se.getSource());
        log.info("remove --> {}:{}", se.getName(), se.getValue());
    }

    /**
     * Notification that an attribute has been replaced in a session. Called
     * after the attribute is replaced.
     *
     * @param se Information about the replaced attribute
     */
    @Override
    public void attributeReplaced(HttpSessionBindingEvent se) {
        log.info("source:{}", se.getSource());
        log.info("replace --> {}:{}", se.getName(), se.getValue());
    }
}
