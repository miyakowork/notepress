package me.wuwenbin.notepress.springboot.container.config.xss;


import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

/**
 * xss和sql防注入包装器
 *
 * @author wuwenbin
 * @date 2020年01月14日16:48:44
 */
public class XssAndSqlHttpServletRequestWrapper extends HttpServletRequestWrapper {

    HttpServletRequest orgRequest;
    private Map<String, String[]> parameterMap;
    /**
     * 用于保存读取body中数据
     */
    private final byte[] body;

    public XssAndSqlHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        orgRequest = request;
        parameterMap = request.getParameterMap();
        body = StreamUtils.copyToByteArray(request.getInputStream());
    }

    // 重写几个HttpServletRequestWrapper中的方法

    /**
     * 获取所有参数名
     *
     * @return 返回所有参数名
     */
    @Override
    public Enumeration<String> getParameterNames() {
        Vector<String> vector = new Vector<String>(parameterMap.keySet());
        return vector.elements();
    }

    /**
     * 覆盖getParameter方法，将参数名和参数值都做xss & sql过滤。<br/>
     * 如果需要获得原始的值，则通过super.getParameterValues(name)来获取<br/>
     * getParameterNames,getParameterValues和getParameterMap也可能需要覆盖
     */
    @Override
    public String getParameter(String name) {
        String[] results = parameterMap.get(name);
        if (results == null || results.length <= 0) {
            return null;
        } else {
            String value = results[0];
            if (value != null) {
                value = xssEncode(value);
            }
            return value;
        }
    }

    /**
     * 获取指定参数名的所有值的数组，如：checkbox的所有数据 接收数组变量 ，如checkobx类型
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] results = parameterMap.get(name);
        if (results == null || results.length <= 0) {
            return null;
        } else {
            int length = results.length;
            for (int i = 0; i < length; i++) {
                results[i] = xssEncode(results[i]);
            }
            return results;
        }
    }

    /**
     * 覆盖getHeader方法，将参数名和参数值都做xss & sql过滤。<br/>
     * 如果需要获得原始的值，则通过super.getHeaders(name)来获取<br/>
     * getHeaderNames 也可能需要覆盖
     */
    @Override
    public String getHeader(String name) {

        String value = super.getHeader(xssEncode(name));
        if (value != null) {
            value = xssEncode(value);
        }
        return value;
    }

    /**
     * 将容易引起xss & sql漏洞的半角字符直接替换成全角字符
     *
     * @param s
     * @return
     */
    private static String xssEncode(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        } else {
            s = stripXSSAndSql(s);
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '>':
                    sb.append("&gt;");// 转义大于号
                    break;
                case '<':
                    sb.append("&lt;");// 转义小于号
                    break;
                case '&':
                    sb.append("&amp;");// 转义&
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }

        return sb.toString();
    }

    /**
     * 获取最原始的request
     *
     * @return
     */
    public HttpServletRequest getOrgRequest() {
        return orgRequest;
    }

    /**
     * 获取最原始的request的静态方法
     *
     * @return
     */
    public static HttpServletRequest getOrgRequest(HttpServletRequest req) {
        if (req instanceof XssAndSqlHttpServletRequestWrapper) {
            return ((XssAndSqlHttpServletRequestWrapper) req).getOrgRequest();
        }

        return req;
    }

    /**
     * 防止xss跨脚本攻击（替换，根据实际情况调整）
     */

    public static String stripXSSAndSql(String value) {
        if (value != null) {
            // NOTE: It's highly recommended to use the ESAPI library and
            // uncomment the following line to
            // avoid encoded attacks.
            // value = ESAPI.encoder().canonicalize(value);
            // Avoid null characters
            /** value = value.replaceAll("", ""); ***/
            // Avoid anything between script tags
            Pattern scriptPattern = compile(
                    "<[\r\n| | ]*script[\r\n| | ]*>(.*?)</[\r\n| | ]*script[\r\n| | ]*>", CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid anything in a
            // src="http://www.yihaomen.com/article/java/..." type of
            // e-xpression
            scriptPattern = compile("src[\r\n| | ]*=[\r\n| | ]*[\\\"|\\\'](.*?)[\\\"|\\\']",
                    CASE_INSENSITIVE | MULTILINE | DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Remove any lonesome </script> tag
            scriptPattern = compile("</[\r\n| | ]*script[\r\n| | ]*>", CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Remove any lonesome <script ...> tag
            scriptPattern = compile("<[\r\n| | ]*script(.*?)>",
                    CASE_INSENSITIVE | MULTILINE | DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid eval(...) expressions
            scriptPattern = compile("eval\\((.*?)\\)",
                    CASE_INSENSITIVE | MULTILINE | DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid e-xpression(...) expressions
            scriptPattern = compile("e-xpression\\((.*?)\\)",
                    CASE_INSENSITIVE | MULTILINE | DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid javascript:... expressions
            scriptPattern = compile("javascript[\r\n| | ]*:[\r\n| | ]*", CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid vbscript:... expressions
            scriptPattern = compile("vbscript[\r\n| | ]*:[\r\n| | ]*", CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid onload= expressions
            scriptPattern = compile("onload(.*?)=",
                    CASE_INSENSITIVE | MULTILINE | DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
        }
        return value;
    }

    public static boolean checkXssAndSql(String value) {
        boolean flag = false;
        if (value != null) {
            // NOTE: It's highly recommended to use the ESAPI library and
            // uncomment the following line to
            // avoid encoded attacks.
            // value = ESAPI.encoder().canonicalize(value);
            // Avoid null characters
            /** value = value.replaceAll("", ""); ***/
            // Avoid anything between script tags
            Pattern scriptPattern = compile(
                    "<[\r\n| | ]*script[\r\n| | ]*>(.*?)</[\r\n| | ]*script[\r\n| | ]*>", CASE_INSENSITIVE);
            flag = scriptPattern.matcher(value).find();
            if (flag) {
                return flag;
            }
            // Avoid anything in a
            // src="http://www.yihaomen.com/article/java/..." type of
            // e-xpression
            scriptPattern = compile("src[\r\n| | ]*=[\r\n| | ]*[\\\"|\\\'](.*?)[\\\"|\\\']",
                    CASE_INSENSITIVE | MULTILINE | DOTALL);
            flag = scriptPattern.matcher(value).find();
            if (flag) {
                return flag;
            }
            // Remove any lonesome </script> tag
            scriptPattern = compile("</[\r\n| | ]*script[\r\n| | ]*>", CASE_INSENSITIVE);
            flag = scriptPattern.matcher(value).find();
            if (flag) {
                return flag;
            }
            // Remove any lonesome <script ...> tag
            scriptPattern = compile("<[\r\n| | ]*script(.*?)>",
                    CASE_INSENSITIVE | MULTILINE | DOTALL);
            flag = scriptPattern.matcher(value).find();
            if (flag) {
                return flag;
            }
            // Avoid eval(...) expressions
            scriptPattern = compile("eval\\((.*?)\\)",
                    CASE_INSENSITIVE | MULTILINE | DOTALL);
            flag = scriptPattern.matcher(value).find();
            if (flag) {
                return flag;
            }
            // Avoid e-xpression(...) expressions
            scriptPattern = compile("e-xpression\\((.*?)\\)",
                    CASE_INSENSITIVE | MULTILINE | DOTALL);
            flag = scriptPattern.matcher(value).find();
            if (flag) {
                return flag;
            }
            // Avoid javascript:... expressions
            scriptPattern = compile("javascript[\r\n| | ]*:[\r\n| | ]*", CASE_INSENSITIVE);
            flag = scriptPattern.matcher(value).find();
            if (flag) {
                return flag;
            }
            // Avoid vbscript:... expressions
            scriptPattern = compile("vbscript[\r\n| | ]*:[\r\n| | ]*", CASE_INSENSITIVE);
            flag = scriptPattern.matcher(value).find();
            if (flag) {
                return flag;
            }
            // Avoid onload= expressions
            scriptPattern = compile("onload(.*?)=",
                    CASE_INSENSITIVE | MULTILINE | DOTALL);
            flag = scriptPattern.matcher(value).find();
            if (flag) {
                return flag;
            }
        }
        return flag;
    }

    public final boolean checkParameter() {
        Map<String, String[]> submitParams = new HashMap(parameterMap);
        Set<String> submitNames = submitParams.keySet();
        for (String submitName : submitNames) {
            Object submitValues = submitParams.get(submitName);
            if ((submitValues instanceof String)) {
                if (checkXssAndSql((String) submitValues)) {
                    return true;
                }
            } else if ((submitValues instanceof String[])) {
                for (String submitValue : (String[]) submitValues) {
                    if (checkXssAndSql(submitValue)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener arg0) {

            }
        };
    }

}
