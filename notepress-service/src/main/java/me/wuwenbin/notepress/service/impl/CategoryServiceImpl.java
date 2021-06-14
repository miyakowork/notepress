package me.wuwenbin.notepress.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Category;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.service.ICategoryService;
import me.wuwenbin.notepress.service.mapper.CategoryMapper;
import me.wuwenbin.notepress.service.mapper.ReferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuwenbin
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    private final CategoryMapper categoryMapper;
    private final ReferMapper referMapper;

    @Override
    public NotePressResult findCategoryList(IPage<Category> categoryPage, LayuiTableQuery<Category> layuiTableQuery) {
        return findLayuiTableList(categoryMapper, categoryPage, layuiTableQuery);
    }

    @Override
    public NotePressResult findCategoryListByContentId(String contentId) {
        List<Category> categories = referMapper.selectList(ReferQuery.buildBySelfIdAndType(contentId, ReferTypeEnum.CONTENT_CATEGORY))
                .stream().map(refer -> categoryMapper.selectById(refer.getReferId())).collect(Collectors.toList());
        return NotePressResult.createOkData(categories);
    }
}
