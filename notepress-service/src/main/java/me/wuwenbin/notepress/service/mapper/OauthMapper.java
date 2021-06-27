package me.wuwenbin.notepress.service.mapper;

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
                        .clientId(oauth.getClientId())
                        .clientSecret(oauth.getClientSecret())
                        .redirectUri(oauth.getRedirectUri())
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
