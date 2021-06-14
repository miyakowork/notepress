package me.wuwenbin.notepress.service.utils;

import cn.hutool.core.util.StrUtil;
import me.wuwenbin.notepress.api.constants.NotePressConstants;
import me.wuwenbin.notepress.api.model.entity.system.SysSession;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.query.BaseQuery;
import me.wuwenbin.notepress.api.utils.NotePressServletUtils;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.mapper.SysSessionMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author wuwen
 */
public class NotePressSessionUtils {

    private static final SysSessionMapper SYS_SESSION_MAPPER = NotePressUtils.getBean(SysSessionMapper.class);

    public static SysUser getSessionUser() {
        if (isAdminReq()) {
            String token = NotePressJwtUtils.getJwtToken();
            if (StrUtil.isNotEmpty(token)) {
                int npSessionCnt = SYS_SESSION_MAPPER.selectCount(BaseQuery.build("jwt_token", token));
                if (npSessionCnt > 0) {
                    String username = NotePressJwtUtils.getUsername();
                    if (StrUtil.isNotEmpty(username)) {
                        return NotePressJwtUtils.getUser();
                    }
                }
            }
            return null;
        } else {
            HttpSession session = NotePressServletUtils.getSession();
            return (SysUser) session.getAttribute(NotePressConstants.SESSION_USER_KEY);
        }
    }

    /**
     * 如果确定是前台的请求，那么可以使用此方法获取session user对象
     *
     * @return
     */
    public static SysUser getFrontSessionUser() {
        HttpSession session = NotePressServletUtils.getSession();
        return (SysUser) session.getAttribute(NotePressConstants.SESSION_USER_KEY);
    }

    public static void setSessionUser(SysUser sessionUser, String jwtToken) {
        if (isAdminReq() && StrUtil.isNotEmpty(jwtToken)) {
            SYS_SESSION_MAPPER.insert(SysSession.admin(jwtToken));
        } else {
            HttpSession session = NotePressServletUtils.getSession();
            session.setAttribute(NotePressConstants.SESSION_USER_KEY, sessionUser);
            session.setMaxInactiveInterval(7200);
        }
    }


    public static void invalidSessionUser() {
        if (isAdminReq()) {
            String token = NotePressJwtUtils.getJwtToken();
            SYS_SESSION_MAPPER.delete(BaseQuery.build("jwt_token", token));
        } else {
            HttpSession session = NotePressServletUtils.getSession();
            SysUser sessionUser = (SysUser) session.getAttribute(NotePressConstants.SESSION_USER_KEY);
            session.removeAttribute(NotePressConstants.SESSION_USER_KEY);
            session.invalidate();
            try {
                SYS_SESSION_MAPPER.deleteBySessionUserId(sessionUser.getId());
            } catch (Exception ignored) {
            }
        }
    }

    private static boolean isAdminReq() {
        HttpServletRequest request = NotePressServletUtils.getRequest();
        return request.getRequestURI().startsWith("/admin/");
    }

}
