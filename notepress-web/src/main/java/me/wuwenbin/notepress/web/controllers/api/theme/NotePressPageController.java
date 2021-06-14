package me.wuwenbin.notepress.web.controllers.api.theme;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.constants.enums.DictionaryTypeEnum;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.*;
import me.wuwenbin.notepress.api.model.query.ContentPageQuery;
import me.wuwenbin.notepress.api.query.DictionaryQuery;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.query.SysNoticeQuery;
import me.wuwenbin.notepress.api.service.*;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @author wuwenbin
 */
@Controller
@RequestMapping
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class NotePressPageController extends NotePressBaseController {

    private final IParamService paramService;
    private final IContentService contentService;
    private final ISysUserService userService;
    private final IDictionaryService dictionaryService;
    private final ICategoryService categoryService;
    private final IReferService referService;
    private final ISysNoticeService noticeService;

    /**
     * 内容页面
     *
     * @param model
     * @param page
     * @param contentPageQuery
     * @param search           是否为搜索
     * @param tp               themePage是否为主题页面
     * @return
     */
    @GetMapping("/index")
    public String index(Model model, Page<Content> page, ContentPageQuery contentPageQuery,
                        @ModelAttribute("themeSettings") HashMap<String, Object> themeSettings,
                        String search, String tp, String zdy) {
        setPage(page);
        setSearch(contentPageQuery, model, themeSettings);
        if (StrUtil.isEmpty(search) && StrUtil.isEmpty(tp)) {
            setNoneShow(contentPageQuery, themeSettings);
        }
        //页面的内容，包含置顶、热门、推荐3种标签的一起
        contentPageQuery.setSearchType(ContentPageQuery.SearchType.ALL);
        Page<Content> contentsAllPage = pageHandle(page, contentPageQuery);
        model.addAttribute("contentsAllPage", contentsAllPage);
        //页面内容，不包含热门、推荐2种标签类型一起，但是包括置顶
        contentPageQuery.setSearchType(ContentPageQuery.SearchType.ALL);
        Page<Content> contentsNonePage = pageHandle(page, contentPageQuery);
        model.addAttribute("contentsNonePage", contentsNonePage);
        //页面内容，只包含热门的1种标签类型
        contentPageQuery.setSearchType(ContentPageQuery.SearchType.HOT);
        Page<Content> contentsHotPage = pageHandle(page, contentPageQuery);
        model.addAttribute("contentsHotPage", contentsHotPage);
        //页面内容，只包含推荐的1种标签类型
        contentPageQuery.setSearchType(ContentPageQuery.SearchType.RECOMMEND);
        Page<Content> contentsRecommendPage = pageHandle(page, contentPageQuery);
        model.addAttribute("contentsRecommendPage", contentsRecommendPage);


        //设置内容的作者相关信息
        Map<String, String> contentAuthorNames = contentAuthorNames(contentsNonePage);
        Map<String, String> contentAuthorNamesHot = contentAuthorNames(contentsHotPage);
        Map<String, String> contentAuthorNamesRec = contentAuthorNames(contentsRecommendPage);
        Map<String, String> contentAuthorNamesAll = new HashMap<>(15);
        contentAuthorNamesAll.putAll(contentAuthorNames);
        contentAuthorNamesAll.putAll(contentAuthorNamesHot);
        contentAuthorNamesAll.putAll(contentAuthorNamesRec);
        model.addAttribute("contentAuthorNameMap", contentAuthorNamesAll);

        //设置内容的分类信息
        Map<String, List<Category>> contentCategoryList = contentCategoryList(contentsNonePage);
        Map<String, List<Category>> contentCategoryListHot = contentCategoryList(contentsHotPage);
        Map<String, List<Category>> contentCategoryListRec = contentCategoryList(contentsNonePage);
        Map<String, List<Category>> contentCategoryListAll = new HashMap<>(15);
        contentCategoryListAll.putAll(contentCategoryList);
        contentCategoryListAll.putAll(contentCategoryListHot);
        contentCategoryListAll.putAll(contentCategoryListRec);
        model.addAttribute("contentCategoryMapList", contentCategoryListAll);

        //评论数量
        Map<String, Integer> contentCommentCnt = contentCommentCnt(contentsNonePage);
        Map<String, Integer> contentCommentCntHot = contentCommentCnt(contentsHotPage);
        Map<String, Integer> contentCommentCntRec = contentCommentCnt(contentsRecommendPage);
        Map<String, Integer> contentCommentCntAll = new HashMap<>(15);
        contentCommentCntAll.putAll(contentCommentCnt);
        contentCommentCntAll.putAll(contentCommentCntHot);
        contentCommentCntAll.putAll(contentCommentCntRec);
        model.addAttribute("contentCommentCount", contentCommentCntAll);


        //tag列表
        Map<String, List<Dictionary>> contentTagList = contentTagList(contentsNonePage);
        Map<String, List<Dictionary>> contentTagListHot = contentTagList(contentsHotPage);
        Map<String, List<Dictionary>> contentTagListRec = contentTagList(contentsRecommendPage);
        Map<String, List<Dictionary>> contentTagListAll = new HashMap<>(15);
        contentTagListAll.putAll(contentTagList);
        contentTagListAll.putAll(contentTagListHot);
        contentTagListAll.putAll(contentTagListRec);
        model.addAttribute("contentTagList", contentTagListAll);


        //随机几篇文章
        List<Content> randomContents = toListBeanNull(contentService.findRandomContents(6));
        model.addAttribute("randomContents", randomContents);
        //友情链接
        List<Dictionary> linkList = dictionaryService.list(DictionaryQuery.build("dictionary_type", DictionaryTypeEnum.LINK));
        model.addAttribute("linkList", linkList);
        //数量前30的tag
        List<Dictionary> tagListTop30 = toListBeanNull(dictionaryService.top30TagList());
        model.addAttribute("tagList", tagListTop30);

        if (StrUtil.isNotEmpty(zdy)) {
            return "zdy";
        } else {
            return StrUtil.isEmpty(search) ? StrUtil.isEmpty(tp) ? "contents" : "page" : "search";
        }
    }

    @RequestMapping("/index/next")
    @ResponseBody
    public NotePressResult nextPage(Page<Content> page, ContentPageQuery contentPageQuery,
                                    @ModelAttribute("themeSettings") HashMap<String, Object> themeSettings,
                                    String search, String tp) {
        setPage(page);
        setSearch(contentPageQuery, null, themeSettings);
        if (StrUtil.isEmpty(search) && StrUtil.isEmpty(tp)) {
            setNoneShow(contentPageQuery, themeSettings);
        }
        Map<String, Object> resultMap = new HashMap<>(5);
//        //页面内容，不包含置顶、热门、推荐、最新的4种标签类型一起
//        if (contentPageQuery.getSearchType() != ContentPageQuery.SearchType.ALL) {
//            contentPageQuery.setSearchType(ContentPageQuery.SearchType.NONE);
//        }

        String noteCates = MapUtil.getStr(themeSettings, "noteCates");
        if (StrUtil.isNotEmpty(noteCates)) {
            contentPageQuery.setExcludeCates(noteCates);
        }

        Page<Content> contentsNonePage = new Page<>();
        BeanUtils.copyProperties(page, contentsNonePage);
        contentsNonePage = toPageBeanNull(contentService.findContents(contentsNonePage, contentPageQuery));
        resultMap.put("nonePage", contentsNonePage);
        //设置内容的作者相关信息
        Map<String, String> contentAuthorNames = contentAuthorNames(contentsNonePage);
        resultMap.put("authorsMap", contentAuthorNames);
        //设置内容的分类信息
        Map<String, List<Category>> contentCategoryList = contentCategoryList(contentsNonePage);
        resultMap.put("categoriesMap", contentCategoryList);
        //评论数量
        Map<String, Integer> contentCommentCnt = contentCommentCnt(contentsNonePage);
        resultMap.put("commentCountsMap", contentCommentCnt);
        //tag列表
        Map<String, List<Dictionary>> contentTagList = contentTagList(contentsNonePage);
        resultMap.put("contentTagList", contentTagList);
        return NotePressResult.createOkData(resultMap);
    }

    //=========================私有方法================================

    /**
     * 需要传入参数如下：
     * pn（页码）
     * ps（页面大小）
     * so（排序对象，格式：[排序字段1名称]:[排序字段1方向].[排序字段2名称]:[排序字段2方向]）
     *
     * @param page
     */
    private void setPage(Page<Content> page) {
        String pageNo = request.getParameter("pn");
        String ps = request.getParameter("ps");
        page.setCurrent(StringUtils.isEmpty(pageNo) ? 1 : Integer.parseInt(pageNo) <= 0 ? 1 : Integer.parseInt(pageNo));
        NotePressResult result = paramService.fetchParamByName(ParamKeyConstant.CONTENT_PAGE_SIZE);
        int pageSize = result.isSuccess() ? Integer.parseInt(result.getDataBean(Param.class).getValue()) : 15;
        page.setSize(StringUtils.isEmpty(ps) ? pageSize : Integer.parseInt(ps) <= 0 ? 15 : Integer.parseInt(ps));

        String sortObjs = request.getParameter("so");
        OrderItem oiTop = OrderItem.desc("`top`");
        OrderItem oiGmt = OrderItem.desc("gmt_create");
        page.addOrder(oiTop);
        page.addOrder(oiGmt);

        if (StrUtil.isNotEmpty(sortObjs)) {
            String[] sortArray = sortObjs.split(".");
            for (String sort : sortArray) {
                String[] sortObj = sort.split(":");
                String sortField = StrUtil.toUnderlineCase(sortObj[0]);
                if (sortObj.length > 1) {
                    String sortOrder = sortObj[1];
                    OrderItem oi;
                    if ("asc".contentEquals(sortOrder)) {
                        oi = OrderItem.asc(sortField);
                    } else {
                        oi = OrderItem.desc(sortField);
                    }
                    page.addOrder(oi);
                } else {
                    page.addOrder(OrderItem.asc(sortField));
                }
            }
        }

    }

    /**
     * 设置查询参数
     *
     * @param contentPageQuery
     */
    private void setSearch(ContentPageQuery contentPageQuery, Model model,
                           Map<String, Object> themeSettings) {
        String noteCates = MapUtil.getStr(themeSettings, "noteCates");
        if (StrUtil.isNotEmpty(noteCates)) {
            contentPageQuery.setExcludeCates(noteCates);
        }
        String s = request.getParameter("s");
        if (!StringUtils.isEmpty(s)) {
            contentPageQuery.setTitle(s);
            contentPageQuery.setWords(s);
        }
        if (model != null) {
            model.addAttribute("s", s);
        }
    }

    /**
     * 设置首页不显示的分类，此方法需要放到setSearch之后
     */
    private void setNoneShow(ContentPageQuery contentPageQuery, Map<String, Object> themeSettings) {
        String noneCates = MapUtil.getStr(themeSettings, "noneCates");
        if (StrUtil.isNotEmpty(noneCates)) {
            contentPageQuery.setExcludeCates(contentPageQuery.getExcludeCates().concat(".").concat(noneCates));
        }
    }

    /**
     * model添加page值
     *
     * @param page
     * @param contentPageQuery
     */
    private Page<Content> pageHandle(Page<Content> page, ContentPageQuery contentPageQuery) {
        Page<Content> pageParam = new Page<>();
        BeanUtils.copyProperties(page, pageParam);
        return toPageBeanNull(contentService.findContents(pageParam, contentPageQuery));
    }

    /**
     * 根据每篇内容的id获取name
     *
     * @param page
     * @return
     */
    private Map<String, String> contentAuthorNames(Page<Content> page) {
        Page<Content> pageParam = new Page<>();
        BeanUtils.copyProperties(page, pageParam);
        return pageParam.getRecords().stream().collect(
                toMap(
                        Content::getId,
                        content -> userService.getById(content.getAuthorId()).getNickname()
                ));
    }

    /**
     * 根据每篇内容的id获取分类名集合
     *
     * @param page
     * @return
     */
    private Map<String, List<Category>> contentCategoryList(Page<Content> page) {
        Page<Content> pageParam = new Page<>();
        BeanUtils.copyProperties(page, pageParam);
        return pageParam.getRecords().stream().collect(
                toMap(
                        Content::getId,
                        content -> {
                            List<Category> cl = toListBeanNull(categoryService.findCategoryListByContentId(content.getId()));
                            return Objects.requireNonNull(cl);
                        }
                ));
    }

    /**
     * 内容的评论数量
     *
     * @param page
     * @return
     */
    private Map<String, Integer> contentCommentCnt(Page<Content> page) {
        Page<Content> pageParam = new Page<>();
        BeanUtils.copyProperties(page, pageParam);
        return pageParam.getRecords().stream().collect(
                toMap(
                        Content::getId,
                        content -> noticeService.count(SysNoticeQuery.buildNotEmpty("content_id", content.getId()))
                ));
    }

    /**
     * 内容的taglist
     *
     * @param page
     * @return
     */
    private Map<String, List<Dictionary>> contentTagList(Page<Content> page) {
        Page<Content> pageParam = new Page<>();
        BeanUtils.copyProperties(page, pageParam);
        return pageParam.getRecords().stream().collect(
                toMap(
                        Content::getId,
                        content -> {
                            List<String> tagIdList = referService.list(ReferQuery.buildBySelfIdAndType(content.getId(), ReferTypeEnum.CONTENT_TAG))
                                    .stream().map(Refer::getReferId).collect(Collectors.toList());
                            return dictionaryService.list(DictionaryQuery.buildByIdCollection(tagIdList));
                        }
                ));
    }

}
