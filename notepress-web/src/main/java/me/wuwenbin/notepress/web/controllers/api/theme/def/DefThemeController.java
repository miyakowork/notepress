package me.wuwenbin.notepress.web.controllers.api.theme.def;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Dictionary;
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
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @author wuwenbin
 */
@SuppressWarnings("DuplicatedCode")
@Controller
@RequestMapping
public class DefThemeController extends NotePressBaseController {

    @Autowired
    private IContentService contentService;
    @Autowired
    private IParamService paramService;
    @Autowired
    private IReferService referService;
    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private ISysNoticeService noticeService;

    @GetMapping("/note")
    public String notesPage(Model model, Page<Content> page, ContentPageQuery contentPageQuery,
                            @ModelAttribute("themeSettings") HashMap<String, Object> themeSettings) {
        setPage(page);
        setSearch(contentPageQuery, model, themeSettings);
        //页面的内容，包含置顶、热门、推荐3种标签的一起
        contentPageQuery.setSearchType(ContentPageQuery.SearchType.ALL);
        page = toPageBeanNull(contentService.findContents(page, contentPageQuery));
        model.addAttribute("notePage", page);

        //tag列表
        Set<Dictionary> noteTags = contentTagList(page);
        model.addAttribute("noteTags", noteTags);
        return "notes";
    }

    @PostMapping("/note/next")
    @ResponseBody
    public NotePressResult noteNextPage(Page<Content> page, ContentPageQuery contentPageQuery,
                                        @ModelAttribute("themeSettings") HashMap<String, Object> themeSettings) {
        setPage(page);
        setSearch(contentPageQuery, null, themeSettings);
        //页面的内容，包含置顶、热门、推荐3种标签的一起
        contentPageQuery.setSearchType(ContentPageQuery.SearchType.ALL);
        page = toPageBeanNull(contentService.findContents(page, contentPageQuery));
        return NotePressResult.createOkData(page);
    }


    /**
     * 内容页面
     *
     * @param model
     * @param page
     * @param contentPageQuery
     * @return
     */
    @GetMapping("/purchase")
    public String index(Model model, Page<Content> page, ContentPageQuery contentPageQuery,
                        @ModelAttribute("themeSettings") HashMap<String, Object> themeSettings) {
        setPagePurchase(page);
        setSearchPurchase(contentPageQuery, model);
        String purchaseCates = MapUtil.getStr(themeSettings, "purchaseCates");
        Page<Content> contentsAllPage = new Page<>();
        BeanUtils.copyProperties(page, contentsAllPage);
        if (StrUtil.isNotEmpty(purchaseCates)) {
            contentPageQuery.setCates(purchaseCates);
            contentsAllPage = pageHandle(page, contentPageQuery);
        }
        model.addAttribute("nonePage", contentsAllPage);
        //设置内容的分类信息
        Map<String, List<Category>> contentCategoryList = contentCategoryList(contentsAllPage);
        model.addAttribute("contentCategoryMapList", contentCategoryList);
        //评论数量
        Map<String, Integer> contentCommentCnt = contentCommentCnt(contentsAllPage);
        model.addAttribute("contentCommentCount", contentCommentCnt);
        //tag列表
        Map<String, List<Dictionary>> contentTagList = contentTagListPurchase(contentsAllPage);
        model.addAttribute("contentTagList", contentTagList);

        return "purchase";
    }

    @RequestMapping("/purchase/next")
    @ResponseBody
    public NotePressResult nextPage(Model model, Page<Content> page, ContentPageQuery contentPageQuery,
                                    @ModelAttribute("themeSettings") HashMap<String, Object> themeSettings) {
        setPagePurchase(page);
        setSearchPurchase(contentPageQuery, model);
        String purchaseCates = MapUtil.getStr(themeSettings, "purchaseCates");
        Page<Content> contentsAllPage = new Page<>();
        BeanUtils.copyProperties(page, contentsAllPage);
        if (StrUtil.isNotEmpty(purchaseCates)) {
            contentPageQuery.setCates(purchaseCates);
            contentsAllPage = pageHandle(page, contentPageQuery);
        }
        Map<String, Object> resultMap = new HashMap<>(5);
        resultMap.put("nonePage", contentsAllPage);
        //设置内容的分类信息
        Map<String, List<Category>> contentCategoryList = contentCategoryList(contentsAllPage);
        resultMap.put("categoriesMap", contentCategoryList);
        //评论数量
        Map<String, Integer> contentCommentCnt = contentCommentCnt(contentsAllPage);
        resultMap.put("commentCountsMap", contentCommentCnt);
        //tag列表
        Map<String, List<Dictionary>> contentTagList = contentTagListPurchase(contentsAllPage);
        resultMap.put("contentTagList", contentTagList);
        return writeJsonOk(resultMap);
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
        page.setCurrent(StringUtils.isEmpty(pageNo) ? 1 : Integer.parseInt(pageNo) <= 0 ? 1 : Integer.parseInt(pageNo));
        page.setSize(10);
        OrderItem oiTop = OrderItem.desc("`top`");
        OrderItem oiGmt = OrderItem.desc("gmt_create");
        page.addOrder(oiTop);
        page.addOrder(oiGmt);
    }

    /**
     * 设置查询参数
     *
     * @param contentPageQuery
     */
    private void setSearch(ContentPageQuery contentPageQuery, Model model, Map<String, Object> themeSettings) {
        String s = request.getParameter("s");
        if (!StringUtils.isEmpty(s)) {
            contentPageQuery.setTitle(s);
            contentPageQuery.setWords(s);
        }
        String noteCates = MapUtil.getStr(themeSettings, "noteCates");
        if (StringUtils.isNotEmpty(noteCates)) {
            contentPageQuery.setCates(noteCates);
        } else {
            contentPageQuery.setContentIds("-1");
        }
        if (model != null) {
            model.addAttribute("s", s);
        }
    }

    /**
     * 文章的taglist
     *
     * @param page
     * @return
     */
    private Set<Dictionary> contentTagList(Page<Content> page) {
        Page<Content> pageParam = new Page<>();
        BeanUtils.copyProperties(page, pageParam);
        List<Set<Dictionary>> dictSetList = new ArrayList<>();
        Function<Content, Set<Dictionary>> mapper = content -> {
            Set<String> tagIdList = referService.list(ReferQuery.buildBySelfIdAndType(content.getId(), ReferTypeEnum.CONTENT_TAG))
                    .stream().map(Refer::getReferId).collect(Collectors.toSet());
            return new HashSet<>(dictionaryService.list(DictionaryQuery.buildByIdCollection(tagIdList)));
        };
        for (Content content1 : pageParam.getRecords()) {
            Set<Dictionary> dictionaries = mapper.apply(content1);
            dictSetList.add(dictionaries);
        }
        Set<Dictionary> resultSet = new HashSet<>(4);
        for (Set<Dictionary> dictionarySet : dictSetList) {
            resultSet.addAll(dictionarySet);
        }
        return resultSet;
    }


    /**
     * 需要传入参数如下：
     * pn（页码）
     * ps（页面大小）
     * so（排序对象，格式：[排序字段1名称]:[排序字段1方向].[排序字段2名称]:[排序字段2方向]）
     *
     * @param page
     */
    private void setPagePurchase(Page<Content> page) {
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
    private void setSearchPurchase(ContentPageQuery contentPageQuery, Model model) {
        String s = request.getParameter("s");
        if (!StringUtils.isEmpty(s)) {
            contentPageQuery.setTitle(s);
            contentPageQuery.setWords(s);
        }
        if (model != null) {
            model.addAttribute("s", s);
        }
        //页面的内容，包含置顶、热门、推荐3种标签的一起
        contentPageQuery.setSearchType(ContentPageQuery.SearchType.ALL);
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
    private Map<String, List<Dictionary>> contentTagListPurchase(Page<Content> page) {
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
