package me.wuwenbin.notepress.springboot.container.config.mvc;

import cn.hutool.setting.Setting;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.NotePressConstants;
import me.wuwenbin.notepress.api.constants.UploadConstant;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.springboot.container.config.interceptor.*;
import me.wuwenbin.notepress.springboot.container.config.xss.XssAndSqlFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mobile.device.DeviceResolverHandlerInterceptor;
import org.springframework.mobile.device.site.SitePreferenceHandlerInterceptor;
import org.springframework.mobile.device.site.SitePreferenceHandlerMethodArgumentResolver;
import org.springframework.util.StringUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Arrays;
import java.util.List;

/**
 * created by Wuwenbin on 2019/11/22 at 3:58 下午
 *
 * @author wuwenbin
 */
@Configuration
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class NotePressWebMvcConfig implements WebMvcConfigurer {

    @Qualifier("notePressSetting")
    private final Setting notePressSetting;

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(NotePressUtils.getApplicationContext());
        String prefix = NotePressUtils.getPageRootPath();
        templateResolver.setPrefix("file:///" + prefix);
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setForceTemplateMode(true);
        templateResolver.setCheckExistence(true);
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        return viewResolver;
    }

    /**
     * xss和sql注入过滤器，在interceptor之前
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean<XssAndSqlFilter> xssFilter() {
        FilterRegistrationBean<XssAndSqlFilter> filterRegistration = new FilterRegistrationBean<>();
        XssAndSqlFilter xssFilter = new XssAndSqlFilter();
        filterRegistration.setFilter(xssFilter);
        filterRegistration.setEnabled(true);
        filterRegistration.addUrlPatterns("/*");
        filterRegistration.setOrder(1);
        return filterRegistration;
    }

    /**
     * 添加一些虚拟路径的映射
     * 上传文件的路径
     * 如果配置了七牛云上传，则上传路径无效
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = NotePressUtils.getPageRootPath().concat("static/");
        registry.addResourceHandler("/static/**").addResourceLocations("file:///" + path);
        String uploadPath = notePressSetting.get("app", "uploadPath");
        if (StringUtils.isEmpty(uploadPath)) {
            uploadPath = NotePressUtils.rootPath().concat("/").concat(NotePressConstants.DEFAULT_UPLOAD_PATH).concat("/");
        }
        registry.addResourceHandler(UploadConstant.PATH_PREFIX_VISIT + "/**").addResourceLocations(uploadPath);
    }

    /**
     * 全局拦截器
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePaths = Arrays.asList(
                "/error/**", "/init/**", "/image/code"
                , UploadConstant.PATH_PREFIX_VISIT + "/**"
                , "/user/login", "/admin/login", "/payServer/**", "/static/**");
        registry.addInterceptor(new CorsInterceptor()).addPathPatterns("/**").excludePathPatterns(excludePaths).order(1);
        registry.addInterceptor(new InitInterceptor()).addPathPatterns("/**").excludePathPatterns(excludePaths).order(2);
        //排除alipay
        registry.addInterceptor(new ThemeInterceptor()).addPathPatterns("/**").excludePathPatterns("/admin/**")
                .excludePathPatterns(excludePaths).excludePathPatterns("/alipay").order(3);
        registry.addInterceptor(new JwtInterceptor()).addPathPatterns("/admin/**").excludePathPatterns(excludePaths).order(4);
        registry.addInterceptor(new SessionInterceptor()).addPathPatterns("/token/**", "/admin/**", "/**/token/**").excludePathPatterns(excludePaths).order(5);

        List<String> logPaths = Arrays.asList(
                "/", "/index", "/np-login", "/np-register", "/np-bind", "/admin/**"
                , "/api/login/**", "/note", "/purchase", "/message", "/res", "/token/ubs/**"
                , "/content/**"
        );
        registry.addInterceptor(new LogInterceptor()).addPathPatterns(logPaths).order(6);
        registry.addInterceptor(new DeviceResolverHandlerInterceptor());
        registry.addInterceptor(new SitePreferenceHandlerInterceptor());
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new SitePreferenceHandlerMethodArgumentResolver());
    }

    /**
     * 跨域支持
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowCredentials(true)
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS", "HEAD")
                .maxAge(3600 * 24);
    }
}
