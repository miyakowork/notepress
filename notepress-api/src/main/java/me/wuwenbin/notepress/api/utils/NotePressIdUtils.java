package me.wuwenbin.notepress.api.utils;

import java.time.LocalDateTime;

/**
 * 比较粗糙的id生成器
 *
 * @author wuwen
 */
public class NotePressIdUtils {

    public static String nextId() {
        LocalDateTime now = LocalDateTime.now();
        String a = String.valueOf(now.getYear() + 93);
        String b = String.valueOf(now.getMonth().getValue() + 9);
        String c = String.valueOf(now.getDayOfMonth() + 16);
        String d = String.valueOf(now.getHour() + 8);
        String e = String.valueOf(now.getMinute() + 54);
        String f = String.valueOf(now.getSecond());
        return a + b + c + d + e + f;
    }
}
