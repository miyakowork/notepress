package me.wuwenbin.notepress.web.controllers.api.theme;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.*;
import me.wuwenbin.notepress.api.model.entity.system.SysNotice;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.query.BaseQuery;
import me.wuwenbin.notepress.api.query.HideQuery;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.service.*;
import me.wuwenbin.notepress.api.service.plugin.pay.IWxpayQrCodeService;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author wuwen
 */
@Controller
@RequestMapping("/token/ubs")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class NotePressUbsController extends NotePressBaseController {

    private final ISysUserService userService;
    private final IDictionaryService dictionaryService;
    private final ISysNoticeService noticeService;
    private final IContentService contentService;
    private final IReferService referService;
    private final IResService resService;
    private final IParamService paramService;
    private final IWxpayQrCodeService qrCodeService;
    private final IDealService dealService;
    private final IHideService hideService;

    @GetMapping
    public String index(Model model) {
        //数量前30的tag
        List<Dictionary> tagListTop30 = toListBeanNull(dictionaryService.top30TagList());
        model.addAttribute("tagList", tagListTop30);
        model.addAttribute("s", "");
        //noinspection ConstantConditions
        int commentCnt = noticeService.count(Wrappers.<SysNotice>query().eq("user_id", NotePressSessionUtils.getSessionUser().getId()));
        model.addAttribute("commentCnt", commentCnt);
        int purchaseCnt = referService.count(ReferQuery.buildBySelfIdAndType(NotePressSessionUtils.getSessionUser().getId().toString(), ReferTypeEnum.USER_RES));
        model.addAttribute("purchaseCnt", purchaseCnt);
        SysUser sessionUser = NotePressSessionUtils.getFrontSessionUser();
        Set<String> contentIds = dealService.list(BaseQuery.build("user_id", sessionUser.getId()))
                .stream()
                .map(deal -> {
                    Object o = deal.getDealTargetId();
                    if (ObjectUtil.isNotEmpty(o) && o.toString().contains(",")) {
                        int cnt = hideService.count(HideQuery.build("id", o.toString().split(",")[1]));
                        if (cnt > 0) {
                            return o.toString().split(",")[0];
                        }
                    }
                    return null;
                }).filter(StrUtil::isNotEmpty).collect(Collectors.toSet());
        int hideCnt = contentIds.size();
        model.addAttribute("hideCnt", hideCnt);

        List<Object> prices = toListBeanNull(qrCodeService.findWxPrices());
        model.addAttribute("prices", prices);
        String rate = toRNull(paramService.fetchParamByName(ParamKeyConstant.RECHARGE_RATE), Param.class, Param::getValue);
        model.addAttribute("multiple", rate != null ? Integer.parseInt(rate) : 10);
        return "ubs";
    }

    @PostMapping("/updateInfo")
    @ResponseBody
    public NotePressResult updateInfo(String nickname, String newPwd) {
        return writeJson(() -> userService.userUpdateInfo(nickname, newPwd));
    }

    @PostMapping("/checkPwd")
    @ResponseBody
    public NotePressResult checkPwd(String pwd) {
        pwd = SecureUtil.md5(pwd);
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        if (sessionUser != null) {
            if (sessionUser.getPassword().contentEquals(pwd)) {
                return writeJsonOkMsg("检验成功！");
            }
        }
        return writeJsonErrorMsg("校验失败！");
    }

    @PostMapping("/myComments")
    @ResponseBody
    public NotePressResult myComments(Page<SysNotice> page) {
        SysUser sessionUser = NotePressSessionUtils.getFrontSessionUser();
        page.addOrder(OrderItem.desc("gmt_create"));
        IPage<SysNotice> nPage = noticeService.page(page, Wrappers.<SysNotice>query().eq("user_id", sessionUser.getId()));
        List<SysNotice> noticeList = nPage.getRecords();
        Map<String, String> contentIdTitleMap = noticeList.stream()
                .filter(sysNotice -> !"-1".contentEquals(sysNotice.getContentId()))
                .collect(Collectors.toMap(SysNotice::getContentId, sysNotice -> contentService.getById(sysNotice.getContentId()).getTitle(), (v1, v2) -> v2));
        contentIdTitleMap.putIfAbsent("-1", "游客/用户留言");
        Map<String, Object> resMap = MapUtil.of("comments", nPage);
        resMap.put("titles", contentIdTitleMap);
        return writeJsonOk(resMap);
    }

    @PostMapping("/myPurchased")
    @ResponseBody
    public NotePressResult myPurchased(Page<Res> page) {
        SysUser sessionUser = NotePressSessionUtils.getFrontSessionUser();
        page.addOrder(OrderItem.desc("gmt_create"));
        List<String> resIds = referService.list(
                ReferQuery.buildBySelfIdAndType(sessionUser.getId().toString(), ReferTypeEnum.USER_RES))
                .stream()
                .map(refer -> resService.getById(refer.getReferId()).getId()).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(resIds)) {
            resIds = Collections.singletonList("0");
        }
        IPage<Res> rPage = resService.page(page, Wrappers.<Res>query().in("id", resIds));
        return writeJsonOk(rPage);
    }

    @PostMapping("/myHide")
    @ResponseBody
    public NotePressResult myHide(Page<Content> page) {
        SysUser sessionUser = NotePressSessionUtils.getFrontSessionUser();
        page.addOrder(OrderItem.desc("gmt_create"));
        Set<String> contentIds = dealService.list(BaseQuery.build("user_id", sessionUser.getId()))
                .stream()
                .map(deal -> {
                    Object o = deal.getDealTargetId();
                    if (ObjectUtil.isNotEmpty(o) && o.toString().contains(",")) {
                        int cnt = hideService.count(HideQuery.build("id", o.toString().split(",")[1]));
                        if (cnt > 0) {
                            return o.toString().split(",")[0];
                        }
                    }
                    return null;
                }).filter(StrUtil::isNotEmpty).collect(Collectors.toSet());
        if (CollectionUtil.isEmpty(contentIds)) {
            contentIds = Collections.singleton("0");
        }
        IPage<Content> rPage = contentService.page(page, Wrappers.<Content>query().in("id", contentIds));
        return writeJsonOk(rPage);
    }

    @PostMapping("/create/order")
    @ResponseBody
    public String createOrder(@RequestParam BigDecimal price, @RequestParam String type) {
        Param domainParam = toBeanNull(paramService.fetchParamByName(ParamKeyConstant.RECHARGE_SERVER_DOMAIN), Param.class);
        Param keyParam = toBeanNull(paramService.fetchParamByName(ParamKeyConstant.RECHARGE_SIGN_SECRET_KEY), Param.class);
        if (domainParam != null && keyParam != null) {
            String domain = domainParam.getValue();
            String key = keyParam.getValue();
            String url = domain + "/pay/order?userId={}&type={}&price={}&sign={}";
            long userId = NotePressSessionUtils.getFrontSessionUser().getId();
            String p = price.toString();
            if (!p.contains(".")) {
                p += ".00";
            }
            String sign = SecureUtil.md5(SecureUtil.md5(p + type) + key);
            url = StrUtil.format(url, userId, type, price, sign);
            return url;
        }
        return null;
    }

    /**
     * 用户一日一签加硬币
     *
     * @return
     */
    @GetMapping("/sign")
    @ResponseBody
    public NotePressResult rechargeUser() {
        long userId = NotePressSessionUtils.getFrontSessionUser().getId();
        int c = dealService.count(Wrappers.<Deal>query()
                .eq("user_id", userId)
                .like("gmt_create", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        if (c > 0) {
            return NotePressResult.createOkMsg("今日已签到，请明日再来！");
        } else {
            int coin = 1;
            int res = dealService.rechargeCoin(userId, userId, coin, "签到获取硬币").getDataInt();
            return writeJsonJudgedBool(res == 1, "签到成功，获取" + coin + "硬币", "充值失败！");
        }
    }
}
