package me.wuwenbin.notepress.api.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import me.wuwenbin.notepress.api.model.entity.Content;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author wuwenbin
 */
public class ContentQuery extends BaseQuery {

    public static UpdateWrapper<Content> buildTagModify(String cid, String name, Boolean value) {
        return Wrappers.<Content>update()
                .set(StrUtil.isNotEmpty(name), name, value).eq("id", cid);
    }

    public static UpdateWrapper<Content> buildDelete(String id, boolean visible) {
        return Wrappers.<Content>update()
                .set("visible", visible).eq("id", id);
    }

    public static QueryWrapper<Content> buildTodayCount() {
        String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return Wrappers.<Content>query().like("gmt_create", nowDate);
    }

    public static QueryWrapper<Content> buildByUrlSeq(String urlSeq) {
        return Wrappers.<Content>query().eq(StrUtil.isNotEmpty(urlSeq), "url_seq", urlSeq);
    }

}
