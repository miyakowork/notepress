package me.wuwenbin.notepress.springboot.container.config.cache;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * created by Wuwenbin on 2019/11/27 at 2:54 下午
 *
 * @author wuwenbin
 */
@Configuration
public class CacheBean {

    /**
     * 验证码缓存，10分钟有效
     *
     * @return
     */
    @Bean("passwordRetryCache")
    public Cache<String, Integer> passwordRetryCache() {
        return CacheUtil.newTimedCache(10 * 60 * 1000);
    }

    /**
     * 验证码缓存，5分钟有效
     *
     * @return
     */
    @Bean("mailCodeCache")
    public Cache<String, String> mailCodeCache() {
        return CacheUtil.newTimedCache(5 * 60 * 1000);
    }

    /**
     * 验证码缓存，5分钟有效
     *
     * @return
     */
    @Bean("kaptchaCodeCache")
    public Cache<String, String> kaptchaCodeCache() {
        return CacheUtil.newTimedCache(5 * 60 * 1000);
    }

}
