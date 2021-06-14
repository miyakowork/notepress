package me.wuwenbin.notepress.springboot.container.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/**
 * created by Wuwenbin on 2019-07-23 at 14:13
 *
 * @author wuwenbin
 */
@Slf4j
@Configuration
@Data
public class DataSourceConfig {

    private final Environment env;
    @Qualifier("notePressSetting")
    private final Setting notePressSetting;
    private String ip;
    private String port;
    private String name;
    private String user;
    private String pass;
    private String useSsl;
    private String jdbcUrl;

    @Autowired
    public DataSourceConfig(Environment env) {
        this.env = env;
        try {
            this.notePressSetting = NotePressUtils.getBean("notePressSetting");
            this.ip = notePressSetting.getStr("ip", "db", "127.0.0.1");
            this.port = notePressSetting.getStr("port", "db", "3306");
            this.name = notePressSetting.getStr("name", "db", "notepress");
            this.user = notePressSetting.getStr("user", "db", "root");
            this.pass = notePressSetting.getStr("pass", "db", "123456");
            this.useSsl = notePressSetting.getStr("useSSL", "db", "true");
            this.jdbcUrl = notePressSetting.getStr("jdbcUrl", "db", "");
        } catch (Exception e) {
            throw new NotePressException(NotePressErrorCode.SettingError, "初始化数据源配置失败");
        }
    }

    @Bean
    public DataSource dataSource() {
        try {
            DruidDataSource druidDataSource = DruidDataSourceBuilder.create().build();
            String url = StrUtil.isNotEmpty(jdbcUrl) ? getJdbcUrl() : StrUtil
                    .format("jdbc:mysql://{}:{}/{}?useUnicode=true&zeroDateTimeBehavior=convertToNull&characterEncoding=UTF-8&useSSL={}&serverTimezone=GMT%2B8"
                            , getIp(), getPort(), getName(), getUseSsl());
            druidDataSource.setUrl(url);
            druidDataSource.setUsername(getUser());
            druidDataSource.setPassword(getPass());
            return druidDataSource;
        } catch (Exception e) {
            log.error("初始化数据源出错，错误信息：{}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

}
