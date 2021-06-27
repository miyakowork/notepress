package me.wuwenbin.notepress.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Category;
import me.wuwenbin.notepress.api.model.entity.Refer;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.service.ICategoryService;
import me.wuwenbin.notepress.service.mapper.CategoryMapper;
import me.wuwenbin.notepress.service.mapper.ReferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wuwenbin
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ReferMapper referMapper;

    @Override
    public NotePressResult findCategoryList(IPage<Category> categoryPage, LayuiTableQuery<Category> layuiTableQuery) {
        return findLayuiTableList(categoryMapper, categoryPage, layuiTableQuery);
    }


    @Override
    public Map<String, List<Category>> findCategoryListByContentIds(List<String> contentIds) {
        Map<String, List<Category>> result = new HashMap<>(contentIds.size());
        for (String contentId : contentIds) {
            List<String> cateIds = referMapper.selectList(ReferQuery.buildBySelfIdAndType(contentId, ReferTypeEnum.CONTENT_CATEGORY))
                    .stream()
                    .map(Refer::getReferId)
                    .collect(Collectors.toList());
            List<Category> categories = categoryMapper.selectBatchIds(cateIds);
            result.put(contentId, categories);
        }
        return result;
    }
}
