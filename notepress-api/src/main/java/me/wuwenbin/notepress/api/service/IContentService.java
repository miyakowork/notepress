package me.wuwenbin.notepress.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Content;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.model.query.ContentPageQuery;
import me.wuwenbin.notepress.api.service.base.INotePressService;

/**
 * @author wuwenbin
 */
public interface IContentService extends INotePressService<Content> {

    /**
     * 分类数据
     *
     * @param contentPage
     * @param layuiTableQuery
     * @param cateId
     * @return
     */
    NotePressResult findContentList(IPage<Content> contentPage, LayuiTableQuery<Content> layuiTableQuery, String cateId);

    /**
     * 添加新的内容
     *
     * @param content
     * @param userId
     * @param categories
     * @param tagNames
     * @return
     */
    NotePressResult createContent(Content content, Long userId, String[] categories, String[] tagNames);

    /**
     * 修改内容
     * 操作逻辑是：先删除（包括一些分类和tag对应的关系），然后在插入一条
     * 原因：省去检验是否存在对应关系的步骤
     *
     * @param content
     * @param userId
     * @param categories
     * @param tagNames
     * @return
     */
    NotePressResult modifyContent(Content content, Long userId, String[] categories, String[] tagNames);

    /**
     * 根据id查找content
     *
     * @param id
     * @return
     */
    NotePressResult findContentById(String id);

    /**
     * 统计字数
     *
     * @return
     */
    NotePressResult sumContentWords();


    /**
     * 查询页面内容
     *
     * @param page
     * @param contentPageQuery
     * @return
     */
    NotePressResult findContents(Page<Content> page, ContentPageQuery contentPageQuery);

    /**
     * 随机文章
     *
     * @param randomSize
     * @return
     */
    NotePressResult findRandomContents(int randomSize);

    /**
     * 更新流量量
     * @param contentId
     * @return
     */
    NotePressResult updateViewsById(String contentId);

    /**
     * 刷新点赞量
     * @param contentId
     * @return
     */
    NotePressResult updateApproveById(String contentId);
}
