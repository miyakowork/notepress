package me.wuwenbin.notepress.api.utils;

import me.wuwenbin.notepress.api.constants.CacheConstant;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;


/**
 * Cache工具类
 *
 * @author wuwen
 */
public class NotePressCacheUtils implements CacheConstant {


    /**
     * 获取SYS_CACHE缓存
     *
     * @param key
     * @return
     */
    public static Object get(String key) {
        return get(SYS_CACHE, key);
    }

    /**
     * 写入SYS_CACHE缓存
     *
     * @param key
     * @return
     */
    public static void put(String key, Object value) {
        put(SYS_CACHE, key, value);
    }

    /**
     * 从SYS_CACHE缓存中移除
     *
     * @param key
     * @return
     */
    public static void remove(String key) {
        remove(SYS_CACHE, key);
    }

    /**
     * 获取缓存
     *
     * @param cacheName
     * @param key
     * @return
     */
    public static Object get(String cacheName, String key) {
        Cache cache = getCache(cacheName);
        Cache.ValueWrapper obj = cache.get(key);
        return obj != null ? obj.get() : null;
    }

    /**
     * 写入缓存
     *
     * @param cacheName
     * @param key
     * @param value
     */
    public static void put(String cacheName, String key, Object value) {
        Cache cache = getCache(cacheName);
        cache.put(key, value);
    }

    /**
     * 从缓存中移除
     *
     * @param cacheName
     * @param key
     */
    public static void remove(String cacheName, String key) {
        getCache(cacheName).evict(key);
    }

    /**
     * 更新缓存
     *
     * @param cacheName
     * @param key
     * @param value
     */
    public static void update(String cacheName, String key, Object value) {
        Cache cache = getCache(cacheName);
        cache.evict(key);
        cache.put(key, value);
    }

    /**
     * 清除所有系统缓存
     */
    public static void clearAllSysCache() {
        getCache(SYS_CACHE).clear();
    }

    /**
     * 获得一个Cache
     *
     * @param cacheName
     * @return
     */
    private static Cache getCache(String cacheName) {
        net.sf.ehcache.CacheManager cacheManager = NotePressUtils.getBean("cacheManager");
        CacheManager springCacheManager = new EhCacheCacheManager(cacheManager);
        return springCacheManager.getCache(cacheName);
    }

}
