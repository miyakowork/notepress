package me.wuwenbin.notepress.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Category;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.base.INotePressService;

import java.util.List;
import java.util.Map;

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
     * 根据内容id集合查找对分类集合
     *
     * @param contentIds
     * @return
     */
    Map<String, List<Category>> findCategoryListByContentIds(List<String> contentIds);
}
