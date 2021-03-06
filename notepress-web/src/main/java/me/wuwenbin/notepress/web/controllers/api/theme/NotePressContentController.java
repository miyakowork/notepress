package me.wuwenbin.notepress.web.controllers.api.theme;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.wuwenbin.notepress.api.constants.NotePressConstants;
import me.wuwenbin.notepress.api.constants.enums.DictionaryTypeEnum;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Content;
import me.wuwenbin.notepress.api.model.entity.Dictionary;
import me.wuwenbin.notepress.api.model.entity.Refer;
import me.wuwenbin.notepress.api.model.entity.system.SysNotice;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.query.DictionaryQuery;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.service.*;
import me.wuwenbin.notepress.service.impl.helper.ContentHelper;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import me.wuwenbin.notepress.web.controllers.utils.ContentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author wuwen
 */
@Controller
@RequestMapping("/content")
public class NotePressContentController extends NotePressBaseController {

    @Autowired
    private IContentService contentService;
    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ISysNoticeService noticeService;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private IReferService referService;
    @Autowired
    private IDealService dealService;
    @Autowired
    private IHideService hideService;


    @GetMapping("/{cId}")
    public ModelAndView article(@PathVariable("cId") String cId, Model model,
                                @ModelAttribute("themeSettings") Map<String, Object> themeSettings,
                                Page<SysNotice> commentPage) {
        Content content = contentService.getById(cId);
        if (content.getHistory() || !content.getVisible()) {
            return new ModelAndView(new RedirectView("/"));
        }
        contentService.updateViewsById(cId);

        model.addAttribute("author", userService.getById(content.getAuthorId()).getNickname());
        model.addAttribute("cateList", categoryService.list());
        model.addAttribute("tags", contentTagList(cId));

        ContentUtils.ContentIncludeHeaders contentIncludeHeaders = ContentUtils.getContentIncludeHeaders(content);
        content = contentIncludeHeaders.getContent();
        model.addAttribute("headers", contentIncludeHeaders.getHeaders());

        String articlePageStyle = MapUtil.getStr(themeSettings, "article_page_style");
        if (NotePressConstants.PAGE_STYLE_LEFT.equalsIgnoreCase(articlePageStyle)
                || NotePressConstants.PAGE_STYLE_RIGHT.equalsIgnoreCase(articlePageStyle)) {
            //??????????????????
            List<Content> randomContents = toListBeanNull(contentService.findRandomContents(6));
            model.addAttribute("randomContents", randomContents);
            //?????????30???tag
            List<Dictionary> tagListTop30 = toListBeanNull(dictionaryService.top30TagList());
            model.addAttribute("tagList", tagListTop30);
            //????????????
            List<Dictionary> linkList = dictionaryService.list(DictionaryQuery.build("dictionary_type", DictionaryTypeEnum.LINK));
            model.addAttribute("linkList", linkList);
        }
        OrderItem oi1 = OrderItem.desc("floor");
        OrderItem oi2 = OrderItem.desc("gmt_create");
        commentPage.addOrder(oi1);
        commentPage.addOrder(oi2);
        commentPage = toPageBeanNull(noticeService.findMessagePage(commentPage, cId, "????????????"));
        model.addAttribute("comments", commentPage);
        if (commentPage != null) {
            Map<Long, Object> userMap = commentPage.getRecords().stream().collect(Collectors.toMap(
                    SysNotice::getId, (Function<SysNotice, SysUser>) sysNotice -> userService.getById(sysNotice.getUserId())
            ));
            model.addAttribute("userCommentMap", userMap);
        }

        //??????????????????
        content.setHtmlContent(ContentHelper.handleShowContent(cId, content.getHtmlContent()));
        model.addAttribute("content", content);

        return new ModelAndView("content");
    }

    @GetMapping("/u/{urlSeq}")
    public ModelAndView articleUrl(@PathVariable("urlSeq") String urlSeq) {
        urlSeq = StrUtil.isNotEmpty(urlSeq) ? urlSeq : "";
        Content content = contentService.getOne(Wrappers.<Content>query().eq("url_seq", "/" + urlSeq));
        return new ModelAndView(new RedirectView("/content/" + content.getId()));
    }

    @PostMapping("/comments")
    @ResponseBody
    public NotePressResult comments(Page<SysNotice> page, String cId) {
        OrderItem oi1 = OrderItem.desc("floor");
        OrderItem oi2 = OrderItem.desc("gmt_create");
        page.addOrder(oi1);
        page.addOrder(oi2);
        Map<String, Object> resultMap = new HashMap<>(2);
        Page<SysNotice> commentPage = toPageBeanNull(noticeService.findMessagePage(page, cId, "????????????"));
        resultMap.put("commentPage", commentPage);
        if (commentPage != null) {
            Map<Long, Object> userMap = commentPage.getRecords().stream().collect(Collectors.toMap(
                    SysNotice::getId, (Function<SysNotice, SysUser>) sysNotice -> userService.getById(sysNotice.getUserId())
            ));
            resultMap.put("userCommentMap", userMap);
        } else {
            resultMap.put("userCommentMap", null);
        }
        return writeJsonOk(resultMap);
    }


    @PostMapping("/approve")
    @ResponseBody
    public NotePressResult approve(@RequestParam String cId) {
        return contentService.updateApproveById(cId);
    }


    @PostMapping("/token/purchase")
    @ResponseBody
    public NotePressResult purchaseHide(@RequestParam String contentId, @RequestParam String hideId) {
        SysUser sessionUser = NotePressSessionUtils.getFrontSessionUser();
        long userId = sessionUser.getId();
        int remainCoin = dealService.findCoinSumByUserId(userId).getDataInt();
        int hidePrice = hideService.getById(hideId).getHidePrice();
        if (remainCoin >= hidePrice) {
            int cnt = hideService.purchaseContentHideContent(contentId, hideId, userId, hidePrice);
            return writeJsonJudgedBool(cnt == 1, "???????????????", "???????????????");
        } else {
            return writeJsonErrorMsg("???????????????????????????????????????????????????????????????????????????");
        }

    }

    //====================????????????=====================

    /**
     * ???????????????????????????tag
     *
     * @param contentId
     * @return
     */
    private List<Dictionary> contentTagList(String contentId) {
        List<String> tagIdList = referService.list(ReferQuery.buildBySelfIdAndType(contentId, ReferTypeEnum.CONTENT_TAG))
                .stream().map(Refer::getReferId).collect(Collectors.toList());
        return dictionaryService.list(DictionaryQuery.buildByIdCollection(tagIdList));
    }

}
