package me.wuwenbin.notepress.springboot.container.main;

import cn.hutool.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author wuwenbin
 */
@Slf4j
@SpringBootApplication
@MapperScan(basePackages = "me.wuwenbin", annotationClass = MybatisMapper.class)
@EnableScheduling
@EnableCaching
@ComponentScan({"me.wuwenbin"})
public class NotePressApplication implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    private final Setting notePressSetting;

    public NotePressApplication(@Qualifier("notePressSetting") Setting notePressSetting) {
        this.notePressSetting = notePressSetting;
    }

    public static void main(String[] args) {
        SpringApplication.run(NotePressApplication.class, args);
        log.info("处理完毕，NotePress 启动成功！ヾ(^∀^)ﾉ");
    }

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        int port = notePressSetting.getInt("serverPort", "app", 80);
        factory.setPort(port);
    }
}
