package me.wuwenbin.notepress.api.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import me.wuwenbin.notepress.api.model.entity.system.Oauth;

/**
 * created by Wuwenbin on 2019/11/28 at 9:18 上午
 *
 * @author wuwenbin
 */
public class OauthQuery extends BaseQuery {

    public static QueryWrapper<Oauth> build(String type) {
        return Wrappers.<Oauth>query().eq("oauth_type", type.toUpperCase());
    }
}
