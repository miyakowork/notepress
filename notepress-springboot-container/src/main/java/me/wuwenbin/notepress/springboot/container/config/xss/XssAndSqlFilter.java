package me.wuwenbin.notepress.springboot.container.config.xss;


import cn.hutool.core.util.StrUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author wuwenbin
 */
public class XssAndSqlFilter implements Filter {

    /**
     * 此处不要过滤的
     */
    private static final String[] EXCLUDES = new String[]{
            "/admin/content/"
            , "/admin/settings/updateMap"
            , "/admin/theme/update"
            , "/token/sub"
            ,"/payServer"
    };

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String contentType = request.getContentType();
        if (StrUtil.isNotBlank(contentType) && contentType.contains("multipart/form-data")) {
            chain.doFilter(httpRequest, response);
            return;
        }

        //使用包装器
        XssAndSqlHttpServletRequestWrapper xssFilterWrapper = new XssAndSqlHttpServletRequestWrapper(httpRequest);
        String uri = xssFilterWrapper.getRequestURI();
        if (isExcludeUrl(uri)) {
            chain.doFilter(httpRequest, response);
        } else {
            chain.doFilter(xssFilterWrapper, response);
        }
    }

    @Override
    public void init(FilterConfig arg0) {

    }

    private boolean isExcludeUrl(String requestUri) {
        for (String ex : EXCLUDES) {
            requestUri = requestUri.trim();
            ex = ex.trim();
            if (requestUri.contains(ex)) {
                return true;
            }
        }
        return false;
    }

}

