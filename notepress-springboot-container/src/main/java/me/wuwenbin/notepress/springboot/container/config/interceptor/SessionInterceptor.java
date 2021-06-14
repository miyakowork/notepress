package me.wuwenbin.notepress.springboot.container.config.interceptor;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.utils.NotePressServletUtils;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * created by Wuwenbin on 2019/11/26 at 5:29 下午
 * 其实此方法对于后台的jwt有点多余，因为这强行把jwt又变成了有状态的session管理了
 * 但是此处的目前意义是考虑到前端的会话管理，以及后续可能会针对spring-session或者jwt做出一些更改的铺垫吧
 * 同时强行让jwt的token变为可控的状态（因为我们可以控制session）
 * <p>
 * 服务端的有状态几乎是必然的，无非是把session从内存改到数据库或者redis缓存中，
 * 可是token被盗用又确实要防御,那么所谓的无状态原则就是理想很美好但是现实很骨感
 *
 * @author wuwenbin
 */
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        if (sessionUser == null) {
            if (NotePressServletUtils.isAjaxRequest(request) || request.getRequestURI().startsWith("/admin/")) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                NotePressResult notePressResult = NotePressResult.createError(NotePressErrorCode.NotLogin, "未登录或会话过期，请重新登录！");
                response.getWriter().write(JSONUtil.toJsonStr(notePressResult));
            } else {
                response.sendRedirect("/np-login?redirectUrl=" + Base64Encoder.encode(URLUtil.encodeAll(request.getRequestURL().toString())));
            }
            return false;
        }
        return true;
    }
}
