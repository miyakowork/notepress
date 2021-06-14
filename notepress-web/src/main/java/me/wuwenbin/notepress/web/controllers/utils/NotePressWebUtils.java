package me.wuwenbin.notepress.web.controllers.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;

/**
 * @author wuwenbin
 */
public class NotePressWebUtils {

    /**
     * 获取网页上所有的图片路径
     *
     * @param htmlCode
     * @return
     */
    public static List<String> getImageSrc(String htmlCode) {
        List<String> imageSrcList = new ArrayList<>();
        Pattern p = compile("<img\\b[^>]*\\bsrc\\b\\s*=\\s*('|\")?([^'\"\n\r\f>]+(\\.jpg|\\.bmp|\\.eps|\\.gif|\\.mif|\\.miff|\\.png|\\.tif|\\.tiff|\\.svg|\\.wmf|\\.jpe|\\.jpeg|\\.dib|\\.ico|\\.tga|\\.cut|\\.pic)\\b)[^>]*>", CASE_INSENSITIVE);
        Matcher m = p.matcher(htmlCode);
        String quote;
        String src;
        while (m.find()) {
            quote = m.group(1);
            src = (quote == null || quote.trim().length() == 0) ? m.group(2).split("\\s+")[0] : m.group(2);
            imageSrcList.add(src);

        }
        return imageSrcList;
    }

    /**
     * 返回值类型为Map<String, Object>
     *
     * @param properties
     * @return
     */
    public static Map<String, Object> getParameterMap(Map<String, String[]> properties) {
        Map<String, Object> returnMap = new HashMap<>(16);
        Iterator<Map.Entry<String, String[]>> iterator = properties.entrySet().iterator();
        String name;
        String value = "";
        while (iterator.hasNext()) {
            Map.Entry<String, String[]> entry = iterator.next();
            name = entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else {
                String[] values = (String[]) valueObj;
                //用于请求参数中有多个相同名称
                for (String value1 : values) {
                    value = value1 + ",";
                }
                value = value.substring(0, value.length() - 1);
            }
            returnMap.put(name, value);
        }
        return returnMap;
    }

    /**
     * 基路径
     *
     * @param request
     * @return
     */
    public static String basePath(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
        //        return "http://wuwenbin.me/";
    }
}
