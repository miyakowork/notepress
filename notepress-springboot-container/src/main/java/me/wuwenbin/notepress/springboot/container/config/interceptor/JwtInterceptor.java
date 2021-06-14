package me.wuwenbin.notepress.springboot.container.config.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.annotation.JwtIgnore;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.jwt.JwtHelper;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.service.utils.NotePressJwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wuwenbin
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    private JwtHelper jwtHelper = NotePressUtils.getBean(JwtHelper.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        log.info("请求地址：{}", request.getRequestURL());
        // 忽略带JwtIgnore注解的请求, 不做后续token认证校验
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            JwtIgnore jwtIgnore = handlerMethod.getMethodAnnotation(JwtIgnore.class);
            if (jwtIgnore != null) {
                return true;
            }
        }

        // 获取请求头信息authorization信息
        String authHeader = request.getHeader(NotePressJwtUtils.AUTH_HEADER_KEY);
        if (StrUtil.isEmpty(authHeader)) {
            authHeader = request.getParameter(NotePressJwtUtils.AUTH_HEADER_KEY);
        }
        log.info("==> authHeader : {}", authHeader);

        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith(NotePressJwtUtils.TOKEN_PREFIX)) {
            log.info("==> 用户未登录，请先登录！");
            request.setAttribute("errorCode", NotePressErrorCode.NotLogin.getCode());
            throw new NotePressException(NotePressErrorCode.NotLogin);
        }

        // 获取token
        final String token = authHeader.substring(7);

        // 验证token是否有效&无效已做异常抛出，由全局异常处理后返回对应信息
        if (jwtHelper != null) {
            if (uri.startsWith("/admin/")) {
                boolean res = NotePressJwtUtils.userIsAdmin(token, jwtHelper.getBase64Secret());
                if (!res) {
                    JSONObject jsonObject = JSONUtil.createObj();
                    jsonObject.putAll(NotePressResult.createError(NotePressErrorCode.IllegalRequestError, "非法操作！"));
                    response.setHeader("Content-Type", "application/json");
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(jsonObject.toString());
                    NotePressSessionUtils.invalidSessionUser();
                }
                return res;
            } else {
                NotePressJwtUtils.parseJwt(token, jwtHelper.getBase64Secret());
            }
        } else {
            JwtHelper jc = NotePressUtils.getBean(JwtHelper.class);
            if (uri.startsWith("/admin/")) {
                return NotePressJwtUtils.userIsAdmin(token, jc.getBase64Secret());
            } else {
                NotePressJwtUtils.parseJwt(token, jc.getBase64Secret());
            }
        }
        return true;
    }


}
