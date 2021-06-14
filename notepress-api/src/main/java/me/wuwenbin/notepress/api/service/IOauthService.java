package me.wuwenbin.notepress.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.Oauth;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.base.INotePressService;

/**
 * created by Wuwenbin on 2019/11/28 at 9:13 上午
 *
 * @author wuwenbin
 */
public interface IOauthService extends INotePressService<Oauth> {

    /**
     * 查询数据库中是否配置了任何一条 justoauth 配置
     *
     * @param type
     * @return
     */
    NotePressResult getAuthRequest(String type);

    /**
     * 管理oauth列表
     *
     * @param oauthPage
     * @param layuiTableQuery
     * @return
     */
    NotePressResult findOauthList(IPage<Oauth> oauthPage, LayuiTableQuery<Oauth> layuiTableQuery);

}
