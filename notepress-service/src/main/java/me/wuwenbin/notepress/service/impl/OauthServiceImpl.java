package me.wuwenbin.notepress.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.Oauth;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.IOauthService;
import me.wuwenbin.notepress.service.mapper.OauthMapper;
import me.zhyd.oauth.request.AuthRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * created by Wuwenbin on 2019/11/28 at 9:14 上午
 *
 * @author wuwenbin
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OauthServiceImpl extends ServiceImpl<OauthMapper, Oauth> implements IOauthService {

    @Autowired
    private OauthMapper oauthMapper;

    /**
     * 查询数据库中是否配置了任何一条 justoauth 配置
     *
     * @param type
     * @return
     */
    @Override
    public NotePressResult getAuthRequest(String type) {
        try {
            AuthRequest request = oauthMapper.getAuthRequest(type);
            if (request != null) {
                return NotePressResult.createOkData(request);
            }
            return NotePressResult.createErrorMsg("未开放" + type + "登录");
        } catch (Exception e) {
            return NotePressResult.createErrorMsg(e.getMessage());
        }
    }

    @Override
    public NotePressResult findOauthList(IPage<Oauth> oauthPage, LayuiTableQuery<Oauth> layuiTableQuery) {
        return findLayuiTableList(oauthMapper, oauthPage, layuiTableQuery);
    }
}
