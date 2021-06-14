package me.wuwenbin.notepress.springboot.container.config.cache;

import net.sf.ehcache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

/**
 * created by Wuwenbin on 2018/4/10 at 上午11:15
 *
 * @author wuwenbin
 */
@Configuration
public class CacheManagerConfig {

    @Bean
    public CacheManager cacheManager() {
        //springBootEhCache在ehcache.xml中配置的名字
        String ehcacheName = "notePressCache";
        CacheManager cacheManager = CacheManager.getCacheManager(ehcacheName);
        if (cacheManager == null) {
            try {
                cacheManager = CacheManager.create(new ClassPathResource("ehcache" + File.separator + "ehcache.xml").getInputStream());
            } catch (IOException e) {
                throw new RuntimeException("加载 ehcahce 配置文件出错", e);
            }
        }
        return cacheManager;
    }

    /**
     * springBoot中使用Ehcahce的缓存
     *
     * @param cacheManager
     * @return
     */
    @Bean
    public EhCacheCacheManager ehCacheCacheManager(CacheManager cacheManager) {
        return new EhCacheCacheManager(cacheManager);
    }
}
