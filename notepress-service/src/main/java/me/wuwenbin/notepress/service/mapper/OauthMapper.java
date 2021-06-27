package me.wuwenbin.notepress.service.mapper;

import com.xkcoding.http.config.HttpConfig;
import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import me.wuwenbin.notepress.api.model.entity.system.Oauth;
import me.wuwenbin.notepress.api.query.OauthQuery;
import me.wuwenbin.notepress.service.mapper.base.NotePressMapper;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.request.AuthGiteeRequest;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthRequest;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * created by Wuwenbin on 2019/11/28 at 9:13 上午
 *
 * @author wuwenbin
 */
@MybatisMapper
public interface OauthMapper extends NotePressMapper<Oauth> {

    /**
     * 根类请求类型获取对应请求对象
     *
     * @param type
     * @return
     */
    default AuthRequest getAuthRequest(String type) {
        AuthRequest authRequest = null;
        Oauth oauth = this.selectOne(OauthQuery.build(type));
        if (oauth == null) {
            throw new AuthException("未获取到有效的Auth配置");
        }
        switch (type) {
            case "github":
                authRequest = new AuthGithubRequest(AuthConfig.builder()
                        .ignoreCheckState(true)
                        .clientId(oauth.getClientId())
                        .clientSecret(oauth.getClientSecret())
                        .redirectUri(oauth.getRedirectUri())
                        //github可能会抽风，需要配置代理
//                        .httpConfig(HttpConfig.builder()
//                                // Http 请求超时时间
//                                .timeout(15000)
//                                // host 和 port 请修改为开发环境的参数
//                                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890)))
//                                .build())
                        .build());
                break;
            case "qq":
                authRequest = new AuthQqRequest(AuthConfig.builder()
                        .clientId(oauth.getClientId())
                        .clientSecret(oauth.getClientSecret())
                        .redirectUri(oauth.getRedirectUri())
                        .build());
                break;
            case "gitee":
                authRequest = new AuthGiteeRequest(AuthConfig.builder()
                        .clientId(oauth.getClientId())
                        .clientSecret(oauth.getClientSecret())
                        .redirectUri(oauth.getRedirectUri())
                        .build());
                break;
            default:
                break;
        }
        if (null == authRequest) {
            throw new AuthException("未获取到有效的Auth配置");
        }
        return authRequest;
    }
}
