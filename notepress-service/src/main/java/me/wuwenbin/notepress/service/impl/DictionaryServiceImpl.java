package me.wuwenbin.notepress.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Dictionary;
import me.wuwenbin.notepress.api.service.IDictionaryService;
import me.wuwenbin.notepress.service.mapper.DictionaryMapper;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author wuwen
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class DictionaryServiceImpl extends ServiceImpl<DictionaryMapper, Dictionary> implements IDictionaryService {

    private final DictionaryMapper dictionaryMapper;

    /**
     * 更新字典信息
     *
     * @param dictionary
     * @return
     */
    @Override
    public NotePressResult updateDictionaryById(Dictionary dictionary) {
        dictionary.setUpdateBy(NotePressSessionUtils.getSessionUser().getId());
        dictionary.setGmtUpdate(LocalDateTime.now());
        dictionary.setDictLabel(dictionary.getDictionaryType().getLabel());
        int res = dictionaryMapper.updateById(dictionary);
        if (res > 0) {
            return NotePressResult.createOkMsg("修改字典信息成功！");
        }
        return NotePressResult.createErrorMsg("修改字典信息失败！");
    }

    /**
     * 新增字典信息
     *
     * @param dictionary
     * @return
     */
    @Override
    public NotePressResult addDictionary(Dictionary dictionary) {
        dictionary.setGmtCreate(LocalDateTime.now());
        dictionary.setCreateBy(NotePressSessionUtils.getSessionUser().getId());
        int res = dictionaryMapper.insert(dictionary);
        if (res == 1) {
            return NotePressResult.createOkMsg("新增字典成功！");
        }
        return NotePressResult.createErrorMsg("新增字典失败！");
    }

    @Override
    public NotePressResult top30TagList() {
        return NotePressResult.createOkData(dictionaryMapper.topTagList(30, null));
    }
}
