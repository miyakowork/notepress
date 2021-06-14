package me.wuwenbin.notepress.api.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.setting.Setting;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.util.Objects;

/**
 * created by Wuwenbin on 2019/11/18 at 9:56 上午
 *
 * @author wuwenbin
 */
@Component
public class NotePressUtils implements BeanFactoryPostProcessor, ApplicationContextAware, ServletContextListener {

    /**
     * Spring应用上下文环境
     */
    private static ConfigurableListableBeanFactory beanFactory;
    private static ApplicationContext applicationContext;
    private static ServletContext servletContext;


    /**
     * 获取对象
     *
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     */
    public static <T> T getBean(String name) throws BeansException {
        //noinspection unchecked
        return (T) beanFactory.getBean(name);
    }

    /**
     * 获取类型为requiredType的对象
     *
     * @param clz
     * @return
     * @throws BeansException
     */
    public static <T> T getBean(Class<T> clz) throws BeansException {
        return beanFactory.getBean(clz);
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     *
     * @param name
     * @return boolean
     */
    public static boolean containsBean(String name) {
        return beanFactory.containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。
     * 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     *
     * @param name
     * @return boolean
     * @throws NoSuchBeanDefinitionException
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.isSingleton(name);
    }

    /**
     * @param name
     * @return Class 注册对象的类型
     * @throws NoSuchBeanDefinitionException
     */
    public static Class<?> getType(String name) throws NoSuchBeanDefinitionException {
        return beanFactory.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    public static String[] getAliases(String name)
            throws NoSuchBeanDefinitionException {
        return beanFactory.getAliases(name);
    }


    public static <T> T getEnvPropValByKey(String key, Class<T> clazz) {
        return getBean(Environment.class).getProperty(key, clazz);
    }

    public static String getEnvPropStrByKey(String key) {
        return getEnvPropValByKey(key, String.class);
    }

    public static String getEnvPropStrByKeyDefault(String key, String defaultValue) {
        return getEnvPropStrByKeyDefault(key, String.class, defaultValue);
    }

    public static <T> T getEnvPropStrByKeyDefault(String key, Class<T> clazz, T defaultValue) {
        return getBean(Environment.class).getProperty(key, clazz, defaultValue);
    }

    /**
     * 获取配置中的aeskey
     *
     * @return
     */
    public static AES getAesFromSetting() {
        Setting notePressSetting = NotePressUtils.getBean("notePressSetting");
        byte[] aesKey = notePressSetting.getStr("aesKey", "jwt", "___notepress____").getBytes();
        return SecureUtil.aes(aesKey);
    }

    /**
     * 获取页面模板文件的根路径，以 / 结尾
     * eg：/home/notepress-themes/
     *
     * @return
     */
    public static String getPageRootPath() {
        Setting notePressSetting = NotePressUtils.getBean("notePressSetting");
        return notePressSetting.getStr("themesPath", "app", "");
    }

    /**
     * 获取前端页面主题文件夹根路径
     * eg：/home/notepress-themes/templates/
     *
     * @return
     */
    public static String getThemeRootPath(String themeName) {
        String path = getPageRootPath().concat("templates/");
        if (StrUtil.isNotEmpty(themeName)) {
            path = path.concat(themeName).concat("/");
        }
        return path;
    }


    /**
     * 获取主题的配置文件的绝对路径
     *
     * @param themeName
     * @return
     */
    public static String getThemeConfigFileAbsPath(String themeName) {
        return getThemeRootPath(themeName).concat("conf/conf.setting");
    }

    /**
     * 获取主题文件的配置信息对象
     *
     * @param themeName
     * @return
     */
    public static Setting getThemeSetting(String themeName) {
        String fileAbsPath = NotePressUtils.getThemeConfigFileAbsPath(themeName);
        //noinspection MismatchedQueryAndUpdateOfCollection
        return new Setting(FileUtil.touch(fileAbsPath), CharsetUtil.CHARSET_UTF_8, true);
    }

    /**
     * 根据主题名字获取该主题在数据库中参数表中对应的字段name
     *
     * @param themeName
     * @return
     */
    public static String getThemeDbParamKeyByThemeName(String themeName) {
        Setting setting = getThemeSetting(themeName);
        return setting.get("paramKey");
    }

    /**
     * 获取工程的发布路径根目录
     * 即classes的绝对路径
     * file:/E:/idea_workplace/target/classes/
     *
     * @return
     */
    public static String getClassesPath() {
        return Objects.requireNonNull(NotePressUtils.class.getClassLoader().getResource("")).getPath();
    }

    /**
     * 获取改文件在工程中所在的完整绝对路径
     *
     * @param filePath 相对classes的路径
     * @return
     */
    public static String getFilePathInClassesPath(String filePath) {
        return getClassesPath() + filePath;
    }

    /**
     * 执行jar所在的目录路径
     *
     * @return
     */
    public static String rootPath() {
        ApplicationHome applicationHome = new ApplicationHome(NotePressUtils.class);
        File jar = applicationHome.getSource();
        return jar.getParentFile().toString();
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        NotePressUtils.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        NotePressUtils.beanFactory = beanFactory;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        NotePressUtils.servletContext = servletContextEvent.getServletContext();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        NotePressUtils.servletContext = null;
    }
}
