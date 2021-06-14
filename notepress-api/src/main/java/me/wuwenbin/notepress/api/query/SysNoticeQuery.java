package me.wuwenbin.notepress.api.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import me.wuwenbin.notepress.api.model.entity.system.SysNotice;

/**
 * @author wuwenbin
 */
public class SysNoticeQuery extends BaseQuery {

    public static QueryWrapper<SysNotice> build(String userId, String contentId) {
        return Wrappers.<SysNotice>query()
                .eq("content_id", contentId)
                .eq("user_id", userId);
    }
}
