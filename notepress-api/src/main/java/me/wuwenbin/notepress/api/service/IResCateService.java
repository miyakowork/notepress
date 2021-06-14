package me.wuwenbin.notepress.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.ResCate;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.base.INotePressService;

/**
 * @author wuwen
 */
public interface IResCateService extends INotePressService<ResCate> {


    /**
     * 分类数据
     *
     * @param categoryPage
     * @param layuiTableQuery
     * @return
     */
    NotePressResult findCategoryList(IPage<ResCate> categoryPage, LayuiTableQuery<ResCate> layuiTableQuery);

    /**
     * 查询所有资源的分类树
     *
     * @return
     */
    NotePressResult findCateTree();

}
