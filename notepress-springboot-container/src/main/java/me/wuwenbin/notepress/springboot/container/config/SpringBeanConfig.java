package me.wuwenbin.notepress.springboot.container.config;

import cn.hutool.setting.Setting;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import me.wuwenbin.notepress.api.model.jwt.JwtHelper;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.springboot.container.listener.NotePressSessionListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.util.Properties;

/**
 * @author wuwen
 */
@Configuration
public class SpringBeanConfig {

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor p = new PaginationInterceptor();
        p.setDialectType("mysql");
        return p;
    }

    /**
     * 文件上传临时路径
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        String temp = System.getProperty("user.dir");
        factory.setLocation(temp);
        return factory.createMultipartConfig();
    }

    /**
     * jwtHelper配置的注入
     *
     * @return
     */
    @Bean
    public JwtHelper jwtHelper(@Qualifier("notePressSetting") Setting notePressSetting) {
        JwtHelper jwtHelper = new JwtHelper();
        String name = notePressSetting.get("jwt", "name");
        jwtHelper.setName(name);
        int millSeconds = NotePressUtils.getEnvPropValByKey("jwt.expiresMillSecond", Integer.class);
        jwtHelper.setExpiresMillSecond(millSeconds);
        String clientId = notePressSetting.get("jwt", "clientId");
        jwtHelper.setClientId(clientId);
        String base64Secret = notePressSetting.get("jwt", "base64Secret");
        jwtHelper.setBase64Secret(base64Secret);
        return jwtHelper;
    }

    /**
     * 项目一些配置注入
     *
     * @return
     */
    @Bean("notePressSetting")
    public Setting notePressSetting() {
        String settingPath = System.getProperty("user.dir").concat(File.separator).concat("notepress.setting");
        Setting setting = new Setting(settingPath);
        setting.autoLoad(true);
        return setting;
    }

    /**
     * 验证码配置
     *
     * @return
     */
    @Bean
    @Qualifier("captchaProducer")
    public DefaultKaptcha kaptcha() {
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        properties.setProperty(Constants.KAPTCHA_BORDER, "yes");
        properties.setProperty(Constants.KAPTCHA_BORDER_COLOR, "220,220,220");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, "38,29,12");
        properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "147");
        properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "34");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE, "25");
        properties.setProperty(Constants.KAPTCHA_SESSION_KEY, "code");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH, "4");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES, "Arial");
        properties.setProperty(Constants.KAPTCHA_NOISE_COLOR, "164,128,55");
        properties.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL, "com.google.code.kaptcha.impl.ShadowGimpy");
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING, "0123456789");
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }

    @Bean
    public ServletListenerRegistrationBean<NotePressSessionListener> servletListenerRegistrationBean() {
        ServletListenerRegistrationBean<NotePressSessionListener> srb = new ServletListenerRegistrationBean<>();
        srb.setListener(new NotePressSessionListener());
        return srb;
    }

}
