package me.wuwenbin.notepress.api.utils;

import java.util.Properties;

/**
 * @author wuwenbin
 */
public class NotePressServerUtils {

    private static final Properties PROPS = System.getProperties();

    /**
     * NotePress版本
     */
    public static String version() {
        return "v1.4.1";
    }

    /**
     * layui版本
     *
     * @return
     */
    public static String layuiVersion() {
        return "layui for notepress 定制版";
    }


    /**
     * java版本
     *
     * @return
     */
    public static String javaVersion() {
        return PROPS.getProperty("java.version");
    }

    /**
     * 操作系统名称
     *
     * @return
     */
    public static String osName() {
        return PROPS.getProperty("os.name") + " ver" + PROPS.getProperty("os.version");
    }

    /**
     * cpu核心数
     *
     * @return
     */
    public static String cpu() {
        return Runtime.getRuntime().availableProcessors() + " 核";
    }

    /**
     * (Runtime.getRuntime().totalMemory() / 1024 / 1024) + "M";
     *
     * @return
     */
    public static String totalMemory() {
        return (Runtime.getRuntime().totalMemory() / 1024 / 1024) + " MB";
    }

    /**
     * 虚拟机空闲内存量
     *
     * @return
     */
    public static String freeMemory() {
        return (Runtime.getRuntime().freeMemory() / 1024 / 1024) + " MB";
    }

    /**
     * 虚拟机使用的最大内存量
     *
     * @return
     */
    public static String maxMemory() {
        return (Runtime.getRuntime().maxMemory() / 1024 / 1024) + " MB";
    }


}
