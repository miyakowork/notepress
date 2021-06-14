package me.wuwenbin.notepress.api.utils;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

/**
 * created by Wuwenbin on 2019/11/18 at 10:09 上午
 *
 * @author wuwenbin
 */
public class NotePressRequestUtils {

    /**
     * @param value 待处理内容
     * @return
     * @Description 过滤XSS脚本内容
     */
    public static String stripXss(String value) {
        String rlt = null;

        if (null != value) {
            rlt = value.replaceAll("", "");
            Pattern scriptPattern = compile("<script>(.*?)</script>", CASE_INSENSITIVE);
            rlt = scriptPattern.matcher(rlt).replaceAll("");

            scriptPattern = compile("</script>", CASE_INSENSITIVE);
            rlt = scriptPattern.matcher(rlt).replaceAll("");

            scriptPattern = compile("<script(.*?)>", CASE_INSENSITIVE
                    | MULTILINE | DOTALL);
            rlt = scriptPattern.matcher(rlt).replaceAll("");

            scriptPattern = compile("eval\\((.*?)\\)", CASE_INSENSITIVE
                    | MULTILINE | DOTALL);
            rlt = scriptPattern.matcher(rlt).replaceAll("");

            scriptPattern = compile("expression\\((.*?)\\)", CASE_INSENSITIVE
                    | MULTILINE | DOTALL);
            rlt = scriptPattern.matcher(rlt).replaceAll("");

            scriptPattern = compile("javascript:", CASE_INSENSITIVE);
            rlt = scriptPattern.matcher(rlt).replaceAll("");

            scriptPattern = compile("vbscript:", CASE_INSENSITIVE);
            rlt = scriptPattern.matcher(rlt).replaceAll("");

            scriptPattern = compile("onload(.*?)=", CASE_INSENSITIVE
                    | MULTILINE | DOTALL);
            rlt = scriptPattern.matcher(rlt).replaceAll("");
        }

        return rlt;
    }

    /**
     * @param value 待处理内容
     * @return
     * @Description 过滤SQL注入内容
     */
    public static String stripSqlInjection(String value) {
        return (null == value) ? null : value.replaceAll("('.+--)|(--)|(%7C)", "");
    }

    /**
     * @param value 待处理内容
     * @return
     * @Description 过滤SQL/XSS注入内容
     */
    public static String stripSqlXss(String value) {
        return stripXss(stripSqlInjection(value));
    }
}
