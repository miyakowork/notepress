package me.wuwenbin.notepress.service.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.jwt.JwtHelper;
import me.wuwenbin.notepress.api.service.ISysUserService;
import me.wuwenbin.notepress.api.utils.NotePressServletUtils;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.mapper.SysSessionMapper;

import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * @author wuwenbin
 */
@Slf4j
public class NotePressJwtUtils {

    public static final String AUTH_HEADER_KEY = "access_token";

    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 解析jwt，并返回claims对象
     *
     * @param jsonWebToken
     * @param base64Security
     * @return
     */
    public static Claims parseJwt(String jsonWebToken, String base64Security) {
        try {
            return Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(base64Security))
                    .parseClaimsJws(jsonWebToken).getBody();
        } catch (ExpiredJwtException eje) {
            log.error("===== Token过期：{} =====", eje.getMessage());
            try {
                NotePressUtils.getBean(SysSessionMapper.class).deleteJwtToken(jsonWebToken);
            } catch (Exception ignored) {
            }
            throw new NotePressException(NotePressErrorCode.NotLogin);
        } catch (JwtException e) {
            log.error("===== token解析异常：{} =====", e.getMessage());
            throw new NotePressException(NotePressErrorCode.TokenError);
        } catch (Exception e) {
            log.error("===== 请求异常：{} =====", e.getMessage());
            throw new NotePressException(NotePressErrorCode.RequestError);
        }
    }


    /**
     * 构建jwt
     *
     * @param sysUser
     * @param jwtHelper
     * @return
     */
    public static String createJwt(SysUser sysUser, JwtHelper jwtHelper) {
        try {
            // 使用HS256加密算法
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;

            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);

            //生成签名密钥
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(jwtHelper.getBase64Secret());
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

            //userId是重要信息，进行加密下
            String encryptUserId = Base64.encode(String.valueOf(sysUser.getId()), StandardCharsets.UTF_8);
            String encryptUsername = Base64.encode(sysUser.getUsername(), StandardCharsets.UTF_8);
            AES aes = NotePressUtils.getAesFromSetting();
            String encryptUserIsAdmin = aes.encryptHex(String.valueOf(sysUser.getAdmin()));
            //添加构成JWT的参数
            JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
                    .claim("userId", encryptUserId)
                    .claim("uia", encryptUserIsAdmin)
                    // 代表这个JWT的主体，即它的所有人
                    .setSubject(encryptUsername)
                    // 代表这个JWT的签发主体
                    .setIssuer(jwtHelper.getClientId())
                    // 是一个时间戳，代表这个JWT的签发时间
                    .setIssuedAt(new Date())
                    // 代表这个JWT的接收对象
                    .setAudience(jwtHelper.getName())
                    .signWith(signatureAlgorithm, signingKey);
            //添加Token过期时间
            int ttlMills = jwtHelper.getExpiresMillSecond();
            if (ttlMills >= 0) {
                long expMillis = nowMillis + ttlMills;
                Date exp = new Date(expMillis);
                log.info("======> 过期时间：" + exp);
                // 是一个时间戳，代表这个JWT的过期时间
                builder.setExpiration(exp)
                        // 是一个时间戳，代表这个JWT生效的开始时间，意味着在这个时间之前验证JWT是会失败的
                        .setNotBefore(now);
            }
            //生成JWT
            return builder.compact();
        } catch (Exception e) {
            log.error("签名失败", e);
            throw new NotePressException(NotePressErrorCode.TokenError);
        }
    }

    /**
     * 获取user对象
     *
     * @return
     */
    public static SysUser getUser() {
        ISysUserService sysUserService = NotePressUtils.getBean(ISysUserService.class);
        return sysUserService.getOne(
                Wrappers.<SysUser>query()
                        .eq("admin ", true)
                        .eq("username", getUsername())
                        .eq("status", true));
    }

    /**
     * 从token中获取用户名
     *
     * @param token
     * @param base64Security
     * @return
     */
    public static String getUsername(String token, String base64Security) {
        return Base64.decodeStr(parseJwt(token, base64Security).getSubject(), StandardCharsets.UTF_8);
    }

    /**
     * 从token获取用户名
     *
     * @return
     */
    public static String getUsername() {
        HttpServletRequest request = NotePressServletUtils.getRequest();
        String authHeader = request.getHeader(NotePressJwtUtils.AUTH_HEADER_KEY);
        if (StrUtil.isEmpty(authHeader)) {
            authHeader = request.getParameter(NotePressJwtUtils.AUTH_HEADER_KEY);
        }
        final String accessToken = authHeader.substring(7);
        String base64Secret = NotePressUtils.getBean(JwtHelper.class).getBase64Secret();
        return getUsername(accessToken, base64Secret);
    }

    /**
     * 从token中获取用户ID
     *
     * @param token
     * @param base64Security
     * @return
     */
    public static String getUserId(String token, String base64Security) {
        String userId = parseJwt(token, base64Security).get("userId", String.class);
        return Base64.decodeStr(userId, StandardCharsets.UTF_8);
    }

    /**
     * 从token获取id
     *
     * @return
     */
    public static String getUserId() {
        HttpServletRequest request = NotePressServletUtils.getRequest();
        String authHeader = request.getHeader(NotePressJwtUtils.AUTH_HEADER_KEY);
        if (StrUtil.isEmpty(authHeader)) {
            authHeader = request.getParameter(NotePressJwtUtils.AUTH_HEADER_KEY);
        }
        final String accessToken = authHeader.substring(7);
        String base64Secret = NotePressUtils.getBean(JwtHelper.class).getBase64Secret();
        return getUserId(accessToken, base64Secret);
    }

    /**
     * 检测用户是否为admin
     *
     * @param token
     * @param base64Security
     * @return
     */
    public static boolean userIsAdmin(String token, String base64Security) {
        String uiaEncrypt = parseJwt(token, base64Security).get("uia", String.class);
        AES aes = NotePressUtils.getAesFromSetting();
        String uiaStr = aes.decryptStr(uiaEncrypt, StandardCharsets.UTF_8);
        return Boolean.parseBoolean(uiaStr);
    }

    /**
     * 是否已过期
     *
     * @param token
     * @param base64Security
     * @return
     */
    public static boolean isExpiration(String token, String base64Security) {
        return parseJwt(token, base64Security).getExpiration().before(new Date());
    }

    /**
     * 获取accesstoken
     *
     * @return
     */
    public static String getJwtToken() {
        String authHeader = getAuthHeaderToken();
        if (StrUtil.isNotEmpty(authHeader)) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 获取请求头的token
     *
     * @return
     */
    public static String getAuthHeaderToken() {
        HttpServletRequest request = NotePressServletUtils.getRequest();
        String authHeader = request.getHeader(NotePressJwtUtils.AUTH_HEADER_KEY);
        if (StrUtil.isEmpty(authHeader)) {
            authHeader = request.getParameter(NotePressJwtUtils.AUTH_HEADER_KEY);
        }
        return authHeader;
    }

}
