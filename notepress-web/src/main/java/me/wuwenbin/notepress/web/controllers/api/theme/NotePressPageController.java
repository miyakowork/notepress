package me.wuwenbin.notepress.web.controllers.api.theme;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.constants.enums.DictionaryTypeEnum;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Dictionary;
import me.wuwenbin.notepress.api.model.entity.*;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
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

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * @author wuwenbin
 */
@Controller
@RequestMapping
public class NotePressPageController extends NotePressBaseController {

    @Autowired
    private IParamService paramService;
    @Autowired
    private IContentService contentService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private IReferService referService;
    @Autowired
    private ISysNoticeService noticeService;

    /**
     * ????????????
     *
     * @param model
     * @param page
     * @param contentPageQuery
     * @param search           ???????????????
     * @param tp               themePage?????????????????????
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
        //????????????????????????????????????????????????3??????????????????
        contentPageQuery.setSearchType(ContentPageQuery.SearchType.ALL);
        Page<Content> contentsAllPage = pageHandle(page, contentPageQuery);
        model.addAttribute("contentsAllPage", contentsAllPage);
        //???????????????????????????????????????2??????????????????????????????????????????
        contentPageQuery.setSearchType(ContentPageQuery.SearchType.ALL);
        Page<Content> contentsNonePage = pageHandle(page, contentPageQuery);
        model.addAttribute("contentsNonePage", contentsNonePage);
        //?????????????????????????????????1???????????????
        contentPageQuery.setSearchType(ContentPageQuery.SearchType.HOT);
        Page<Content> contentsHotPage = pageHandle(page, contentPageQuery);
        model.addAttribute("contentsHotPage", contentsHotPage);
        //?????????????????????????????????1???????????????
        contentPageQuery.setSearchType(ContentPageQuery.SearchType.RECOMMEND);
        Page<Content> contentsRecommendPage = pageHandle(page, contentPageQuery);
        model.addAttribute("contentsRecommendPage", contentsRecommendPage);


        //?????????????????????????????????
        Map<String, String> contentAuthorNames = contentAuthorNames(contentsNonePage);
        Map<String, String> contentAuthorNamesHot = contentAuthorNames(contentsHotPage);
        Map<String, String> contentAuthorNamesRec = contentAuthorNames(contentsRecommendPage);
        Map<String, String> contentAuthorNamesAll = new HashMap<>(15);
        contentAuthorNamesAll.putAll(contentAuthorNames);
        contentAuthorNamesAll.putAll(contentAuthorNamesHot);
        contentAuthorNamesAll.putAll(contentAuthorNamesRec);
        model.addAttribute("contentAuthorNameMap", contentAuthorNamesAll);

        //???????????????????????????
        Map<String, List<Category>> contentCategoryList = contentCategoryList(contentsNonePage);
        Map<String, List<Category>> contentCategoryListHot = contentCategoryList(contentsHotPage);
        Map<String, List<Category>> contentCategoryListRec = contentCategoryList(contentsNonePage);
        Map<String, List<Category>> contentCategoryListAll = new HashMap<>(15);
        contentCategoryListAll.putAll(contentCategoryList);
        contentCategoryListAll.putAll(contentCategoryListHot);
        contentCategoryListAll.putAll(contentCategoryListRec);
        model.addAttribute("contentCategoryMapList", contentCategoryListAll);

        //????????????
        Map<String, Integer> contentCommentCnt = contentCommentCnt(contentsNonePage);
        Map<String, Integer> contentCommentCntHot = contentCommentCnt(contentsHotPage);
        Map<String, Integer> contentCommentCntRec = contentCommentCnt(contentsRecommendPage);
        Map<String, Integer> contentCommentCntAll = new HashMap<>(15);
        contentCommentCntAll.putAll(contentCommentCnt);
        contentCommentCntAll.putAll(contentCommentCntHot);
        contentCommentCntAll.putAll(contentCommentCntRec);
        model.addAttribute("contentCommentCount", contentCommentCntAll);


        //tag??????
        Map<String, List<Dictionary>> contentTagList = contentTagList(contentsNonePage);
        Map<String, List<Dictionary>> contentTagListHot = contentTagList(contentsHotPage);
        Map<String, List<Dictionary>> contentTagListRec = contentTagList(contentsRecommendPage);
        Map<String, List<Dictionary>> contentTagListAll = new HashMap<>(15);
        contentTagListAll.putAll(contentTagList);
        contentTagListAll.putAll(contentTagListHot);
        contentTagListAll.putAll(contentTagListRec);
        model.addAttribute("contentTagList", contentTagListAll);


        //??????????????????
        List<Content> randomContents = toListBeanNull(contentService.findRandomContents(6));
        model.addAttribute("randomContents", randomContents);
        //????????????
        List<Dictionary> linkList = dictionaryService.list(DictionaryQuery.build("dictionary_type", DictionaryTypeEnum.LINK));
        model.addAttribute("linkList", linkList);
        //?????????30???tag
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
//        //????????????????????????????????????????????????????????????4?????????????????????
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
        //?????????????????????????????????
        Map<String, String> contentAuthorNames = contentAuthorNames(contentsNonePage);
        resultMap.put("authorsMap", contentAuthorNames);
        //???????????????????????????
        Map<String, List<Category>> contentCategoryList = contentCategoryList(contentsNonePage);
        resultMap.put("categoriesMap", contentCategoryList);
        //????????????
        Map<String, Integer> contentCommentCnt = contentCommentCnt(contentsNonePage);
        resultMap.put("commentCountsMap", contentCommentCnt);
        //tag??????
        Map<String, List<Dictionary>> contentTagList = contentTagList(contentsNonePage);
        resultMap.put("contentTagList", contentTagList);
        return NotePressResult.createOkData(resultMap);
    }

    //=========================????????????================================

    /**
     * ???????????????????????????
     * pn????????????
     * ps??????????????????
     * so???????????????????????????[????????????1??????]:[????????????1??????].[????????????2??????]:[????????????2??????]???
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
     * ??????????????????
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
     * ??????????????????????????????????????????????????????setSearch??????
     */
    private void setNoneShow(ContentPageQuery contentPageQuery, Map<String, Object> themeSettings) {
        String noneCates = MapUtil.getStr(themeSettings, "noneCates");
        if (StrUtil.isNotEmpty(noneCates)) {
            String excludeCates = contentPageQuery.getExcludeCates();
            if (StrUtil.isNotEmpty(excludeCates)) {
                contentPageQuery.setExcludeCates(excludeCates.concat(".").concat(noneCates));
            } else {
                contentPageQuery.setExcludeCates(noneCates);
            }
        }
    }

    /**
     * model??????page???
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
     * ?????????????????????id??????name
     *
     * @param page
     * @return
     */
    private Map<String, String> contentAuthorNames(Page<Content> page) {
        Page<Content> pageParam = new Page<>();
        BeanUtils.copyProperties(page, pageParam);
        List<Long> contentIds = pageParam.getRecords().stream().map(Content::getAuthorId).collect(Collectors.toList());
        Set<SysUser> sysUsers = new HashSet<>(userService.listByIds(contentIds));
        return pageParam.getRecords().stream().collect(
                toMap(
                        Content::getId,
                        content -> sysUsers.stream()
                                .filter(sysUser -> content.getAuthorId().equals(sysUser.getId()))
                                .findFirst()
                                .orElse(SysUser.builder().nickname("-").build())
                                .getNickname()
                ));
    }

    /**
     * ?????????????????????id?????????????????????
     *
     * @param page
     * @return
     */
    private Map<String, List<Category>> contentCategoryList(Page<Content> page) {
        Page<Content> pageParam = new Page<>();
        BeanUtils.copyProperties(page, pageParam);
        List<String> contentIds = pageParam.getRecords().stream().map(Content::getId).collect(Collectors.toList());
        return categoryService.findCategoryListByContentIds(contentIds);
    }

    /**
     * ?????????????????????
     *
     * @param page
     * @return
     */
    private Map<String, Integer> contentCommentCnt(Page<Content> page) {
        Page<Content> pageParam = new Page<>();
        BeanUtils.copyProperties(page, pageParam);
        List<String> contentIds = pageParam.getRecords().stream().map(Content::getId).collect(Collectors.toList());
        return noticeService.contentNoticeCnt(contentIds);
    }

    /**
     * ?????????taglist
     *
     * @param page
     * @return
     */
    private Map<String, List<Dictionary>> contentTagList(Page<Content> page) {
        Page<Content> pageParam = new Page<>();
        BeanUtils.copyProperties(page, pageParam);
        List<String> contentIds = pageParam.getRecords().stream().map(Content::getId).collect(Collectors.toList());
        return dictionaryService.contentDictionary(contentIds);
    }

}
