package me.wuwenbin.notepress.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.SysNotice;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.base.INotePressService;

/**
 * @author wuwen
 */
public interface ISysNoticeService extends INotePressService<SysNotice> {

    /**
     * 管理消息列表
     *
     * @param noticePage
     * @param layuiTableQuery
     * @return
     */
    NotePressResult findNoticeList(IPage<SysNotice> noticePage, LayuiTableQuery<SysNotice> layuiTableQuery);

    /**
     * 分组查询消息通知
     *
     * @return
     */
    NotePressResult findNoticeTypes();

    /**
     * 查询浏览排行
     *
     * @return
     */
    NotePressResult findMessageRankList();

    /**
     * 提交留言/回复
     *
     * @param sysNotice
     * @return
     */
    NotePressResult subMessage(SysNotice sysNotice);

    /**
     * 查询通知
     *
     * @param messagePage
     * @param contentId
     * @param pageType
     * @return
     */
    NotePressResult findMessagePage(Page<SysNotice> messagePage, String contentId, String pageType);
}
