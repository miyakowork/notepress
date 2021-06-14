package me.wuwenbin.notepress.springboot.container.config.interceptor;

import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 加上主题文件夹的名字，要不然找不到主题html页面
 *
 * @author wuwenbin
 */
@Component
public class ThemeInterceptor implements HandlerInterceptor {

    private final IParamService paramService = NotePressUtils.getBean(IParamService.class);

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            ResponseBody responseBody = handlerMethod.getMethodAnnotation(ResponseBody.class);
            //排除ajax请求（仅仅是前台的，后台的不包括）
            if (responseBody == null) {
                Param param = paramService.getOne(ParamQuery.build(ParamKeyConstant.THEME_NAME));
                if (modelAndView != null) {
                    String viewName = modelAndView.getViewName();
                    if (!StringUtils.isEmpty(viewName)) {
                        modelAndView.setViewName("templates/" + param.getValue().concat("/").concat(viewName));
                    }
                }
            }
        }
    }
}
