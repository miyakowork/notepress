package me.wuwenbin.notepress.springboot.container.config.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.Browser;
import cn.hutool.http.useragent.UserAgentInfo;
import cn.hutool.http.useragent.UserAgentUtil;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.system.SysLog;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.service.ISysLogService;
import me.wuwenbin.notepress.api.utils.NotePressIpUtils;
import me.wuwenbin.notepress.api.utils.NotePressServletUtils;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 用于记录访问日志以及设置sessionUser的filter，没有任何URL的过滤作用
 * created by Wuwenbin on 2019-08-09 at 10:03
 *
 * @author wuwenbin
 */
public class LogInterceptor extends NotePressBaseController implements HandlerInterceptor {

    private final IParamService paramService = NotePressUtils.getBean(IParamService.class);
    private final ISysLogService sysLogService = NotePressUtils.getBean(ISysLogService.class);

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) {
        String statisticsMethodType = toRNull(paramService.fetchParamByName(ParamKeyConstant.STATISTICS_METHOD), Param.class, Param::getValue);
        String statistics = toRNull(paramService.fetchParamByName(ParamKeyConstant.SWITCH_VISIT_LOG), Param.class, Param::getValue);
        if (statistics != null && statisticsMethodType != null) {
            boolean openAnalysis = Integer.parseInt(statistics) == 1;
            if (StrUtil.isNotEmpty(statisticsMethodType)) {
                if (openAnalysis && reqUriInDbSet(request, statisticsMethodType)) {
                    SysUser sessionUser = NotePressSessionUtils.getSessionUser();
                    String ipAddr = NotePressIpUtils.getRemoteAddress(request);
                    SysLog logger = SysLog.builder()
                            .ipAddr(ipAddr)
                            .ipInfo(getIpInfo(ipAddr))
                            .time(LocalDateTime.now())
                            .url(request.getRequestURI())
                            .userAgent(request.getHeader("User-Agent"))
                            .requestMethod(request.getMethod())
                            .contentType(request.getContentType())
                            .build();
                    try {
                        HttpSession session = request.getSession();
                        logger.setSessionId(session.getId());
                    } catch (Exception e) {
                        if (session != null) {
                            logger.setSessionId(session.getId());
                        }
                    }
                    if (sessionUser != null) {
                        logger.setUserId(sessionUser.getId());
                        logger.setUsername(sessionUser.getUsername());
                    }
                    Browser browser = UserAgentUtil.parse(logger.getUserAgent()).getBrowser();
                    logger.setBrowser(UserAgentInfo.NameUnknown.equals(browser.getName()) ? "脚本/搜索引擎/爬虫等" : browser.getName());
                    sysLogService.save(logger);
                }
            }
        }
    }

    //==============================私有方法================================
    private boolean reqUriInDbSet(HttpServletRequest request, String dbSet) {
        List<String> dbSetList = Arrays.asList(dbSet.split("\\|"));
        String reqUri = request.getRequestURI();
        if (!NotePressServletUtils.isAjaxRequest(request)) {
            if (dbSetList.contains("admin") && reqUri.contains("/admin/")) {
                return true;
            }
            if (dbSetList.contains("home_index")
                    && (reqUri.contains("/") || reqUri.contains("/index"))) {
                return true;
            }
            if (dbSetList.contains("content") && reqUri.contains("/content/")) {
                return true;
            }
            return dbSetList.contains("other")
                    && (reqUri.contains("/purchase") || reqUri.contains("/note")
                    || reqUri.contains("/message")) || reqUri.contains("/token/ubs")
                    || reqUri.contains("/res");
        }
        return false;
    }

    private String getIpInfo(String ipAddr) {
        NotePressIpUtils.IpInfo ipInfo = NotePressIpUtils.getIpInfo(ipAddr);
        String address = ipInfo.getAddress();
        if (StrUtil.isEmpty(StrUtil.trim(address))) {
            return ipInfo.getLine();
        }
        return address;
    }
}
