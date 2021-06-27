package me.wuwenbin.notepress.api.service;

import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Dictionary;
import me.wuwenbin.notepress.api.service.base.INotePressService;

import java.util.List;
import java.util.Map;

/**
 * @author wuwen
 */
public interface IDictionaryService extends INotePressService<Dictionary> {


    /**
     * 更新字典信息
     *
     * @param dictionary
     * @return
     */
    NotePressResult updateDictionaryById(Dictionary dictionary);

    /**
     * 新增字典信息
     *
     * @param dictionary
     * @return
     */
    NotePressResult addDictionary(Dictionary dictionary);

    /**
     * 使用数量前30的tag
     *
     * @return
     */
    NotePressResult top30TagList();

    /**
     * 文章标签
     *
     * @param contentIds
     * @return
     */
    Map<String, List<Dictionary>> contentDictionary(List<String> contentIds);

}
