package me.wuwenbin.notepress.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import me.wuwenbin.notepress.api.constants.enums.DictionaryTypeEnum;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Category;
import me.wuwenbin.notepress.api.model.entity.Content;
import me.wuwenbin.notepress.api.model.entity.Dictionary;
import me.wuwenbin.notepress.api.model.entity.Refer;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.model.query.ContentPageQuery;
import me.wuwenbin.notepress.api.query.BaseQuery;
import me.wuwenbin.notepress.api.query.ContentQuery;
import me.wuwenbin.notepress.api.query.DictionaryQuery;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.service.IContentService;
import me.wuwenbin.notepress.service.impl.helper.ContentHelper;
import me.wuwenbin.notepress.service.mapper.CategoryMapper;
import me.wuwenbin.notepress.service.mapper.ContentMapper;
import me.wuwenbin.notepress.service.mapper.DictionaryMapper;
import me.wuwenbin.notepress.service.mapper.ReferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wuwenbin
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content> implements IContentService {

    @Autowired
    private ContentMapper contentMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DictionaryMapper dictionaryMapper;
    @Autowired
    private ReferMapper referMapper;

    @Override
    public NotePressResult findContentList(IPage<Content> contentPage, LayuiTableQuery<Content> layuiTableQuery, String cateIds) {
        if (StrUtil.isNotEmpty(cateIds)) {
            Function<QueryWrapper<Content>, QueryWrapper<Content>> extraQuery = wrapper -> {
                Set<String> contentIdSet = getContentIdSetByCates(cateIds);
                return CollectionUtil.isNotEmpty(contentIdSet) ? wrapper.in("id", contentIdSet) : wrapper;
            };
            return findLayuiTableList(contentMapper, contentPage, layuiTableQuery, extraQuery);
        } else {
            return findLayuiTableList(contentMapper, contentPage, layuiTableQuery);
        }
    }

    @Override
    public NotePressResult createContent(Content content, Long userId, String[] categories, String[] tagNames) {
        if (userId == null) {
            throw new NotePressException("未获取到用户信息 ==> userId is null！");
        }
        if (ArrayUtil.isEmpty(categories)) {
            throw new NotePressException("内容至少在一个分类组下！");
        }
        content.setAuthorId(userId);
        ContentHelper.decorateContent(content, true);
        ContentHelper.handleBasePath(content, true);
        if (content.getReprinted() && StringUtils.isEmpty(content.getOriginUrl())) {
            throw new NotePressException("提交的文章为非原创类型（转载类型），请填写原文链接！");
        }
        if (!StringUtils.isEmpty(content.getUrlSeq())) {
            int cnt = contentMapper.selectCount(ContentQuery.buildByUrlSeq(content.getUrlSeq()));
            boolean isExistUrl = cnt > 0;
            if (isExistUrl) {
                throw new NotePressException(StrUtil.format("==> url: [{}] 已存在！", content.getUrlSeq()));
            } else {
                try {
                    content.setUrlSeq(PinyinHelper.convertToPinyinString(content.getUrlSeq(), "", PinyinFormat.WITHOUT_TONE));
                } catch (PinyinException e) {
                    throw new NotePressException(e.getMessage());
                }
            }
        }
        content.setHtmlContent(ContentHelper.handleHideContent(content.getId(), content.getHtmlContent()));
        content.setTextContent(HtmlUtil.cleanHtmlTag(StrUtil.trim(content.getHtmlContent())));
        int cnt = contentMapper.insert(content);
        if (cnt == 1) {
            //处理内容的分类
            List<String> categoryStrList = Arrays.asList(categories);
            content.setCategories(categoryStrList);
            List<Category> categoryList = categoryStrList.stream().map(s -> {
                Category c = categoryMapper.selectOne(BaseQuery.build("nickname", s));
                if (c == null) {
                    Category cc = Category.builder().name(s).nickname(s).status(true).orderIndex(0).build();
                    cc.setCreateBy(userId);
                    cc.setGmtCreate(LocalDateTime.now());
                    int cnt1 = categoryMapper.insert(cc);
                    if (cnt1 == 1) {
                        c = cc;
                    }
                }
                return c;
            }).collect(Collectors.toList());
            content.setCateList(categoryList);
            //插入分类和内容对应关系
            categoryList.forEach(cate -> referMapper.insertContentCategory(content.getId(), String.valueOf(cate.getId()), LocalDateTime.now(), userId));

            //处理内容的tag
            List<String> tagNameList = Arrays.asList(tagNames);
            content.setTags(tagNameList);
            List<Dictionary> tagList = tagNameList.stream().map(t -> {
                Dictionary tag = dictionaryMapper.selectOne(DictionaryQuery.buildByTag(t));
                if (tag == null) {
                    Dictionary tt = Dictionary.builder().dictionaryType(DictionaryTypeEnum.TAG).dictLabel(DictionaryTypeEnum.TAG.getLabel()).dictValue(t).isDefault(false).status(true).build();
                    tt.setGmtCreate(LocalDateTime.now());
                    tt.setCreateBy(userId);
                    int cnt1 = dictionaryMapper.insert(tt);
                    if (cnt1 == 1) {
                        tag = tt;
                    }
                }
                return tag;
            }).collect(Collectors.toList());
            content.setTagList(tagList);
            //插入TAG和内容对应关系
            tagList.forEach(tag -> referMapper.insertContentTag(content.getId(), String.valueOf(tag.getId()), LocalDateTime.now(), userId));
        }
        return NotePressResult.createOk("创建成功！", content);
    }

    @Override
    public NotePressResult modifyContent(Content content, Long userId, String[] categories, String[] tagNames) {
        content.setGmtUpdate(LocalDateTime.now());
        content.setUpdateBy(userId);
        ContentHelper.deleteContentRefer(content.getId());
        ContentHelper.deleteContentHide(content.getId());
        int d = contentMapper.deleteById(content.getId());
        if (d == 1) {
            NotePressResult result = createContent(content, userId, categories, tagNames);
            if (result.isSuccess()) {
                result.setMsg("修改成功！");
            }
            return result;
        } else {
            throw new NotePressException("修改发生未知错误！");
        }
    }

    @Override
    public NotePressResult findContentById(String id) {
        Content content = contentMapper.selectById(id);
        ContentHelper.handleBasePath(content, true);
        //查询对应的分类
        List<Refer> referCategoryList = referMapper.selectList(ReferQuery.buildBySelfIdAndType(id, ReferTypeEnum.CONTENT_CATEGORY));
        List<Category> categories = referCategoryList.stream().map(refer -> categoryMapper.selectById(refer.getReferId())).collect(Collectors.toList());
        content.setCateList(categories);
        List<String> categoryList = categories.stream().map(Category::getNickname).collect(Collectors.toList());
        content.setCategories(categoryList);
        //查询对应的标签
        List<Refer> referTagList = referMapper.selectList(ReferQuery.buildBySelfIdAndType(id, ReferTypeEnum.CONTENT_TAG));
        List<Dictionary> tags = referTagList.stream().map(refer -> dictionaryMapper.selectById(refer.getReferId())).collect(Collectors.toList());
        content.setTagList(tags);
        List<String> tagList = tags.stream().map(Dictionary::getDictValue).collect(Collectors.toList());
        content.setTags(tagList);

        content.setImageList(Collections.singletonList(content.getImages()));
        content.setHtmlContent(ContentHelper.handleShowContent(id, content.getHtmlContent()));
        return NotePressResult.createOkData(content);
    }

    @Override
    public NotePressResult sumContentWords() {
        return NotePressResult.createOkData(contentMapper.sumContentWords());
    }

    @Override
    public NotePressResult findContents(Page<Content> page, ContentPageQuery contentPageQuery) {
        QueryWrapper<Content> baseQuery = Wrappers.<Content>query().eq("history", false).eq("visible", true);
        if (contentPageQuery == null) {
            return NotePressResult.createOkData(contentMapper.selectPage(page, baseQuery));
        } else {
            QueryWrapper<Content> finalQuery = baseQuery;


            String contentSearch = contentPageQuery.getWords();
            String title = contentPageQuery.getTitle();
            finalQuery = finalQuery.and(StrUtil.isNotEmpty(contentSearch) || StrUtil.isNotEmpty(title),
                    wrapper -> wrapper
                            .like(StrUtil.isNotEmpty(contentSearch), "text_content", contentSearch).
                                    or()
                            .like(StrUtil.isNotEmpty(title), "title", title));


            String categories = contentPageQuery.getCates();
            Set<String> contentIdSet = new HashSet<>();
            boolean selectedCategoriesButNotIncludeContent = false;
            if (StrUtil.isNotEmpty(categories)) {
                Set<String> contentIdSetTemp = getContentIdSetByCates(categories);
                contentIdSet.addAll(contentIdSetTemp);
                if (CollectionUtil.isEmpty(contentIdSetTemp)) {
                    selectedCategoriesButNotIncludeContent = true;
                }
            }
            String tags = contentPageQuery.getTags();
            if (StrUtil.isNotEmpty(tags)) {
                contentIdSet.addAll(getContentIdSetByTags(tags));
            }
            String contentIds = contentPageQuery.getContentIds();
            if (StrUtil.isNotEmpty(contentIds)) {
                if ("-1".equals(contentIds)) {
                    contentIdSet.add("-1");
                }
                contentIdSet.addAll(Arrays.asList(contentIds.split("\\.")));
            }

            String excludeCates = contentPageQuery.getExcludeCates();
            if (StrUtil.isNotEmpty(excludeCates)) {
                Set<String> excludeContentIdSet = getContentIdSetByCates(excludeCates);
                if (CollectionUtil.isNotEmpty(excludeContentIdSet)) {
                    if (CollectionUtil.isEmpty(contentIdSet)) {
                        finalQuery = finalQuery.notIn("id", excludeContentIdSet);
                    } else {
                        Set<String> resultContentIdSet = contentIdSet.stream().filter(c -> !excludeContentIdSet.contains(c)).collect(Collectors.toSet());
                        finalQuery = finalQuery.in(CollectionUtil.isNotEmpty(resultContentIdSet), "id", resultContentIdSet);
                    }
                } else {
                    finalQuery = finalQuery.in(CollectionUtil.isNotEmpty(contentIdSet) && !selectedCategoriesButNotIncludeContent, "id", contentIdSet);
                }
            } else {
                finalQuery = finalQuery.in(CollectionUtil.isNotEmpty(contentIdSet) && !selectedCategoriesButNotIncludeContent, "id", contentIdSet);
            }


            ContentPageQuery.SearchType st = contentPageQuery.getSearchType();
            if (st != null) {
                String stName = st.getName();
                if (StrUtil.isEmpty(stName)) {
                    if (ContentPageQuery.SearchType.NONE == st) {
                        finalQuery = finalQuery.eq("hot", false)
                                .eq("recommend", false);
                    }
                } else {
                    finalQuery = finalQuery.eq(st.getName(), true);
                }
            }


            IPage<Content> resultPage = contentMapper.selectPage(page, finalQuery);
            return NotePressResult.createOkData(resultPage);
        }
    }

    @Override
    public NotePressResult findRandomContents(int randomSize) {
        return NotePressResult.createOkData(contentMapper.randomContent(randomSize));
    }

    @Override
    public NotePressResult updateViewsById(String contentId) {
        return NotePressResult.createOkData(contentMapper.updateViews(contentId));
    }

    @Override
    public NotePressResult updateApproveById(String contentId) {
        int res = contentMapper.updateApprove(contentId);
        if (res == 1) {
            return NotePressResult.createOk("点赞成功", true);
        } else {
            return NotePressResult.createErrorMsg("操作失败，请稍后重试！");
        }
    }

    //=========================私有方法================================

    /**
     * 根据np_refer参照表，得出某个分类下的所有content_id的set集合
     *
     * @param categoriesInReq
     * @return
     */
    private Set<String> getContentIdSetByCates(String categoriesInReq) {
        String[] categoryArray = categoriesInReq.split("\\.");
        List<String> cateIdList = Arrays.stream(categoryArray).filter(NumberUtil::isInteger).collect(Collectors.toList());
        List<List<String>> contentIdListList = cateIdList.stream()
                .map(cateId -> referMapper.findRefersByTypeAndReferId(cateId, ReferTypeEnum.CONTENT_CATEGORY)
                        .stream().map(Refer::getSelfId).collect(Collectors.toList()))
                .collect(Collectors.toList());
        Set<String> contentIdSet = new HashSet<>();
        for (List<String> contentIds : contentIdListList) {
            contentIdSet.addAll(CollectionUtil.newHashSet(contentIds));
        }
        return contentIdSet;
    }

    /**
     * 根据np_refer参照表，得出某个tag下的所有content_id的set集合
     *
     * @param tagsInReq
     * @return
     */
    private Set<String> getContentIdSetByTags(String tagsInReq) {
        String[] tagArray = tagsInReq.split("\\.");
        List<String> tagIdList = Arrays.stream(tagArray).filter(NumberUtil::isInteger).collect(Collectors.toList());
        List<List<String>> contentIdListList = tagIdList.stream()
                .map(tagId -> referMapper.findRefersByTypeAndReferId(tagId, ReferTypeEnum.CONTENT_TAG)
                        .stream().map(Refer::getSelfId).collect(Collectors.toList()))
                .collect(Collectors.toList());
        Set<String> contentIdSet = new HashSet<>();
        for (List<String> contentIds : contentIdListList) {
            contentIdSet.addAll(CollectionUtil.newHashSet(contentIds));
        }
        return contentIdSet;
    }
}
