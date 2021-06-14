package me.wuwenbin.notepress.api.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * created by Wuwenbin on 2019年11月26日13:48:10 上午
 *
 * @author wuwenbin
 */
@Slf4j
public class NotePressIpUtils {

    private static final String UNKNOWN = "unknown";

    /**
     * 获取实际ip地址
     * this code is copied from RuoYi framework
     *
     * @param request
     * @return
     */
    public static String getRemoteAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 服务器 IP
     *
     * @return
     */
    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        return "127.0.0.1";
    }

    /**
     * 服务器域名
     *
     * @return
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
        return "未知";
    }


    /**
     * 获取ip地理信息位置
     *
     * @param ip
     * @return
     */
    public static IpInfo getIpInfo(String ip) {
        String resp = "";
        try {
            String url = "http://whois.pconline.com.cn/ip.jsp?ip={}";
            url = StrUtil.format(url, ip);
            resp = HttpUtil.get(url, 4000);
            log.info("获取 ip详细地址，参数：{}", ip);
            JSON json = JSONUtil.parse(resp);
            String result = json.toString();
            String[] res = result.split("\\s+");
            return res.length > 1 ?
                    IpInfo.builder().address(StringUtils.isEmpty(res[0]) ? "未知" : res[0]).line(res[1]).build() :
                    IpInfo.builder().address(res[0]).build();
        } catch (cn.hutool.json.JSONException je) {
            String[] res = resp.split(" ");
            return res.length > 1 ?
                    IpInfo.builder().address(StringUtils.isEmpty(res[0]) ? "未知" : res[0]).line(res[1]).build() :
                    IpInfo.builder().address(res[0]).build();
        } catch (Exception e) {
            log.error("获取ip地理位置信息失败", e);
            return IpInfo.builder().address("未知位置").build();
        }
    }


    @Data
    @Builder
    public static class IpInfo {
        /**
         * ip详细信息地址
         */
        private String address;

        /**
         * 运营商/网络线路
         */
        private String line;
    }


}
