package me.wuwenbin.notepress.web.controllers.api.theme;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.wuwenbin.notepress.api.constants.NotePressConstants;
import me.wuwenbin.notepress.api.constants.enums.DictionaryTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Content;
import me.wuwenbin.notepress.api.model.entity.Dictionary;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.system.SysNotice;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.query.DictionaryQuery;
import me.wuwenbin.notepress.api.service.*;
import me.wuwenbin.notepress.service.facade.MailFacade;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static me.wuwenbin.notepress.api.constants.ParamKeyConstant.SWITCH_COMMENT;
import static me.wuwenbin.notepress.api.constants.ParamKeyConstant.SWITCH_COMMENT_NOTICE_MAIL;

/**
 * created by Wuwenbin on 2018/2/8 at 18:54
 *
 * @author wuwenbin
 */
@Controller
@RequestMapping("/message")
public class NotePressNoticeController extends NotePressBaseController {

    @Autowired
    private IParamService paramService;
    @Autowired
    private IDictionaryService dictionaryService;
    @Autowired
    private ISysNoticeService noticeService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private IContentService contentService;
    @Autowired
    private MailFacade mailFacade;

    @GetMapping
    public String messagePage(Model model, Page<SysNotice> noticePage) {
        noticePage.setSize(15);
        OrderItem oi = OrderItem.desc("gmt_create");
        noticePage.addOrder(oi);
        //????????????
        List<Dictionary> linkList = dictionaryService.list(DictionaryQuery.build("dictionary_type", DictionaryTypeEnum.LINK));
        model.addAttribute("linkList", linkList);
        //????????????
        IPage<SysNotice> sysNoticePage = toPageBeanNull(noticeService.findMessagePage(noticePage, "-1", "??????"));
        model.addAttribute("messagePage", sysNoticePage);
        model.addAttribute("messageUserInfo", messageUserInfo(noticePage));
        List<SysNotice> rankList = toListBeanNull(noticeService.findMessageRankList());
        model.addAttribute("messageRankList", rankList);
        //??????????????????
        List<Content> randomContents = toListBeanNull(contentService.findRandomContents(6));
        model.addAttribute("randomContents", randomContents);
        //?????????30???tag
        List<Dictionary> tagListTop30 = toListBeanNull(dictionaryService.top30TagList());
        model.addAttribute("tagList", tagListTop30);
        return "message";
    }

    @PostMapping("/lists")
    @ResponseBody
    public NotePressResult comments(Page<SysNotice> page) {
        page.setSize(20);
        OrderItem oi = OrderItem.desc("gmt_create");
        page.addOrder(oi);
        Page<SysNotice> mpage = toPageBeanNull(noticeService.findMessagePage(page, "-1", "??????"));
        Map<String, Object> res = MapUtil.of("messagePage", mpage);
        assert mpage != null;
        res.put("messageUserInfo", messageUserInfo(mpage));
        return writeJsonOk(res);
    }

    @PostMapping("/token/sub")
    @ResponseBody
    public NotePressResult sub(@Valid SysNotice notice, BindingResult bindingResult) {
        String commentStatus = toRNull(paramService.fetchParamByName(SWITCH_COMMENT), Param.class, Param::getValue);
        if (!NotePressConstants.OPEN.equals(commentStatus)) {
            return writeJsonErrorMsg("??????????????????");
        }
        if (bindingResult.hasErrors()) {
            return writeJsonJsr303(bindingResult.getFieldErrors());
        }
        NotePressResult subRes = noticeService.subMessage(notice);
        if (subRes.isSuccess()) {
            String mailStatus = toRNull(paramService.fetchParamByName(SWITCH_COMMENT_NOTICE_MAIL), Param.class, Param::getValue);
            if (NotePressConstants.OPEN.equals(mailStatus)) {
                sendNoticeMail(notice);
            }
            if (!StringUtils.isEmpty(notice.getReplyId())) {
                sendNoticeReplyMail(notice);
            }
            return writeJsonOkMsg("??????????????????");
        }
        return writeJsonErrorMsg("?????????????????????" + subRes.getMsg());
    }


    //========================????????????=======================

    private Map<Long, Map<String, Object>> messageUserInfo(Page<SysNotice> page) {
        return page.getRecords().stream().collect(
                Collectors.toMap(
                        SysNotice::getId,
                        sysNotice -> {
                            Map<String, Object> resMap = new HashMap<>(2);
                            SysUser u = userService.getById(sysNotice.getUserId());
                            resMap.put("avatar", u.getAvatar());
                            resMap.put("nickname", u.getNickname());
                            resMap.put("admin", u.getAdmin());
                            resMap.put("email", u.getEmail());
                            return resMap;
                        }
                )
        );
    }

    /**
     * ??????????????????
     *
     * @param notice
     */
    private void sendNoticeMail(SysNotice notice) {
        String sendContent = "------<br/>" +
                "<b>??????</b>??????<span style='color:red;'>{}</span>??? ??? <b>{}</b>?????????????????????{}</br>" +
                "------</br>";
        final String sendContent2 = StrUtil.format(sendContent, userService.getById(notice.getUserId()).getUsername(), notice.getFloor(), notice.getCommentHtml());
        ThreadUtil.execAsync(() -> mailFacade.sendNotice(sendContent2, basePath(request), notice.getContentId()));
    }

    /**
     * ????????????????????????
     *
     * @param notice
     */
    private void sendNoticeReplyMail(SysNotice notice) {
        SysNotice noticeServiceById = noticeService.getById(notice.getReplyId());
        if (noticeServiceById != null) {
            Long userId = noticeServiceById.getUserId();
            String email = userService.getById(userId).getEmail();
            if (org.apache.commons.lang3.StringUtils.isNotEmpty(email)) {
                ThreadUtil.execAsync(() -> mailFacade.sendNotice2User(email, notice.getContentId(), basePath(request)));
            }
            sendNoticeMail(notice);
        }
    }

}
