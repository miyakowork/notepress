package me.wuwenbin.notepress.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Category;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.base.INotePressService;

/**
 * @author wuwenbin
 */
public interface ICategoryService extends INotePressService<Category> {

    /**
     * 分类数据
     *
     * @param categoryPage
     * @param layuiTableQuery
     * @return
     */
    NotePressResult findCategoryList(IPage<Category> categoryPage, LayuiTableQuery<Category> layuiTableQuery);

    /**
     * 根据内容的id查找内容对应的分类集合
     *
     * @param contentId
     * @return
     */
    NotePressResult findCategoryListByContentId(String contentId);
}
