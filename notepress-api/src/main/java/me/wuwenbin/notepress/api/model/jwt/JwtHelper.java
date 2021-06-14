package me.wuwenbin.notepress.api.model.jwt;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.Data;

/**
 * @author wuwenbin
 */
@Data
public class JwtHelper {

    private String clientId;
    private String base64Secret;
    private String name;
    private int expiresMillSecond;

    public static void main(String[] args) {
        System.out.println("name =  " + RandomUtil.randomString(12));
        String clientId = IdUtil.fastSimpleUUID();
        System.out.println("clientId = " + clientId);
        System.out.println("base64Secret = " + Base64.encode(clientId));
        System.out.println("aesKey = " + RandomUtil.randomString(16));
    }
}
