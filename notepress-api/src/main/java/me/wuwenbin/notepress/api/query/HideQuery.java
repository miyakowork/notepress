package me.wuwenbin.notepress.api.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import me.wuwenbin.notepress.api.model.entity.Hide;

/**
 * @author wuwenbin
 */
public class HideQuery extends BaseQuery {

    /**
     * 查询wrapper
     *
     * @param hideId
     * @param contentId
     * @param hideType
     * @return
     */
    public static QueryWrapper<Hide> build(String hideId, String contentId, String hideType) {
        return Wrappers.<Hide>query()
                .eq(StrUtil.isNotEmpty(hideId), "id", hideId)
                .eq(StrUtil.isNotEmpty(contentId), "content_id", contentId)
                .eq(StrUtil.isNotEmpty(hideType), "hide_type", hideType);
    }

    /**
     * 更新wrapper
     *
     * @param hideHtml
     * @param hideId
     * @param contentId
     * @param hideType
     * @return
     */
    public static UpdateWrapper<Hide> build(String hideHtml, String hideId, String contentId, String hideType) {
        return Wrappers.<Hide>update().set(StrUtil.isNotEmpty(hideHtml), "hide_html", hideHtml)
                .eq(StrUtil.isNotEmpty(hideId), "id", hideId)
                .eq(StrUtil.isNotEmpty(contentId), "content_id", contentId)
                .eq(StrUtil.isNotEmpty(hideType), "hide_type", hideType);
    }
}
