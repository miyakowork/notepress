package me.wuwenbin.notepress.api.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;

/**
 * @author wuwen
 */
public class NotePressLayuiTableUtils {

    /**
     * 前台传过来的参数传递给page
     *
     * @param <T>
     * @param page
     * @param layuiTableQuery
     */
    public static <T> void startPage(IPage<T> page, LayuiTableQuery<T> layuiTableQuery) {
        int pno = layuiTableQuery.getPage();
        int pageSize = layuiTableQuery.getLimit();
        if (pageSize < 1) {
            throw new NotePressException("页面大小必须大于 1");
        }
        int pageNo = pno <= 0 ? 1 : pno;
        page.setCurrent(pageNo);
        page.setSize(pageSize);
    }
}
