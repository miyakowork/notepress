package me.wuwenbin.notepress.service.impl.helper;

import cn.hutool.core.util.StrUtil;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.constants.enums.HideTypeEnum;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.entity.Content;
import me.wuwenbin.notepress.api.model.entity.Hide;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.query.HideQuery;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.query.SysNoticeQuery;
import me.wuwenbin.notepress.api.service.IHideService;
import me.wuwenbin.notepress.api.utils.NotePressServletUtils;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.mapper.HideMapper;
import me.wuwenbin.notepress.service.mapper.ParamMapper;
import me.wuwenbin.notepress.service.mapper.ReferMapper;
import me.wuwenbin.notepress.service.mapper.SysNoticeMapper;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomInt;

/**
 * 主要是一些处理文章内容的工具方法
 *
 * @author wuwenbin
 */
@SuppressWarnings("DuplicatedCode")
public class ContentHelper {

    private static final String HIDE_COMMENT = "comment";
    private static final String HIDE_LOGIN = "login";
    private static final String HIDE_PURCHASE = "purchase";

    /**
     * 页面必须要有comment-list这个锚点
     */
    private static final String HIDE_COMMENT_REPLACEMENT = "<blockquote data-htype=\"{hideType}\" data-hid=\"{hideId}\" class=\"layui-elem-quote\">此处内容回复可见，" +
            "<a class=\"layui-text\" href=\"#comment-list\">点我回复</a></blockquote>";
    /**
     * 页面必须要有toLoginPage()的js方法
     */
    private static final String HIDE_LOGIN_REPLACEMENT = "<blockquote data-htype=\"{hideType}\" data-hid=\"{hideId}\" class=\"layui-elem-quote\">此处内容登录之后可见，" +
            "<a class=\"layui-text\" href=\"javascript:npfront.toLoginPage();\" target=\"_blank\">点我登录</a></blockquote>";

    /**
     * 购买隐藏的内容
     */
    private static final String HIDE_PURCHASE_REPLACEMENT = "<blockquote data-htype=\"{hideType}\" data-hid=\"{hideId}\" class=\"layui-elem-quote\">此处内容需购买（{price}硬币），" +
            "<a class=\"layui-text\" onclick=\"purchaseContent('{contentId}','{hideId}');\">点我购买</a></blockquote>";

    /**
     * 组装hideMap的参数map
     *
     * @param hideId
     * @param hideType
     * @return
     */
    private static Map<String, Object> hideParamMap(String hideId, String hideType) {
        Map<String, Object> pMap = new HashMap<>(2);
        pMap.put("hideId", hideId);
        pMap.put("hideType", hideType);
        return pMap;
    }

    /**
     * 删除内容与ta和分类的关联关系
     *
     * @param cid
     */
    public static void deleteContentRefer(String cid) {
        ReferMapper referMapper = NotePressUtils.getBean(ReferMapper.class);
        referMapper.delete(ReferQuery.buildDeleteBySelfId(cid));
    }

    /**
     * 删除文章隐藏内容
     *
     * @param cid
     */
    public static void deleteContentHide(String cid) {
        HideMapper hideMapper = NotePressUtils.getBean(HideMapper.class);
        hideMapper.delete(HideQuery.build("content_id", cid));
    }

    /**
     * 装饰一些属性
     *
     * @param content
     * @param isCreate 是否为创建新的文章
     */
    public static void decorateContent(Content content, boolean isCreate) {
        if (isCreate && StringUtils.isEmpty(content.getGmtCreate())) {
            LocalDateTime now = LocalDateTime.now();
            content.setGmtCreate(now);
            content.setGmtUpdate(now);
        }
        if (StringUtils.isEmpty(content.getCreateBy())) {
            content.setCreateBy(content.getAuthorId());
        }
        if (StringUtils.isEmpty(content.getGmtUpdate()) && !StringUtils.isEmpty(content.getGmtCreate())) {
            content.setGmtUpdate(content.getGmtCreate());
        }
        ParamMapper paramMapper = NotePressUtils.getBean(ParamMapper.class);
        if (StringUtils.isEmpty(content.getSeoDescription())) {
            content.setSeoDescription(paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.SEO_DESCRIPTION)).getValue());
        }
        if (StringUtils.isEmpty(content.getSeoKeywords())) {
            content.setSeoKeywords(paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.SEO_KEYWORDS)).getValue());
        }

        if (StringUtils.isEmpty(content.getVisible())) {
            content.setVisible(false);
        }
        if (StringUtils.isEmpty(content.getAppreciable())) {
            content.setAppreciable(false);
        }
        if (StringUtils.isEmpty(content.getCommented())) {
            content.setCommented(false);
        }
        if (StringUtils.isEmpty(content.getReprinted())) {
            content.setReprinted(false);
        }
        if (StringUtils.isEmpty(content.getTop())) {
            content.setTop(false);
        }
        if (StringUtils.isEmpty(content.getRecommend())) {
            content.setRecommend(false);
        }
        if (StringUtils.isEmpty(content.getHot())) {
            content.setHot(false);
        }
        if (StringUtils.isEmpty(content.getViews())) {
            content.setViews(randomInt(666, 1609));
        }
        if (StringUtils.isEmpty(content.getApproveCnt())) {
            content.setApproveCnt(randomInt(6, 169));
        }
        if (StringUtils.isEmpty(content.getHistory())) {
            content.setHistory(true);
        }
    }

    /**
     * 处理文章内容的隐藏
     *
     * @param contentId
     * @param contentHtml
     */
    public static String handleHideContent(String contentId, String contentHtml) {
        String contentHtmlResult = contentHtml;
        IHideService hideService = NotePressUtils.getBean(IHideService.class);
        contentHtml = contentHtml.replace(" />", ">");
        Document document = Jsoup.parse(contentHtml);
        document.outputSettings().prettyPrint(false);
        JXDocument doc = JXDocument.create(document);

        //处理回复可见
        List<JXNode> hide4Comments = doc.selN(StrUtil.format("//div[@data-hide='{}']", HIDE_COMMENT));
        for (JXNode comment : hide4Comments) {
            String html = comment.asElement().outerHtml();
            String hideId = comment.asElement().attr("data-hid");
            if (StrUtil.isEmpty(hideId)) {
                throw new NotePressException("未获取到hide token，请刷新重试！");
            }
            String replacement = StrUtil.format(HIDE_COMMENT_REPLACEMENT, hideParamMap(hideId, HIDE_COMMENT));
            contentHtml = contentHtml.replace(html, replacement);
            Hide existHideInTable = hideService.getOne(HideQuery.build(hideId, contentId, HideTypeEnum.NOT_COMMENT.getValue()));
            if (existHideInTable == null) {
                Hide hide = Hide.builder().id(hideId).contentId(contentId)
                        .hideType(HideTypeEnum.NOT_COMMENT).hideHtml(html).build();
                hideService.save(hide);
            } else {
                hideService.update(HideQuery.build(html, hideId, contentId, HideTypeEnum.NOT_COMMENT.getValue()));
            }
            contentHtmlResult = contentHtml;
        }


        //处理登录可见
        List<JXNode> hideLogins = doc.selN(StrUtil.format("//div[@data-hide='{}']", HIDE_LOGIN));
        for (JXNode login : hideLogins) {
            String html = login.asElement().outerHtml();
            String hideId = login.asElement().attr("data-hid");
            if (StrUtil.isEmpty(hideId)) {
                throw new NotePressException("未获取到hide token，请刷新重试！");
            }
            String replacement = StrUtil.format(HIDE_LOGIN_REPLACEMENT, hideParamMap(hideId, HIDE_LOGIN));
            contentHtml = contentHtml.replace(html, replacement);
            Hide existHideInTable = hideService.getOne(HideQuery.build(hideId, contentId, HideTypeEnum.NOT_LOGIN.getValue()));
            if (existHideInTable == null) {
                Hide hide = Hide.builder().id(hideId).contentId(contentId)
                        .hideType(HideTypeEnum.NOT_LOGIN).hideHtml(html).build();
                hideService.save(hide);
            } else {
                hideService.update(HideQuery.build(html, hideId, contentId, HideTypeEnum.NOT_LOGIN.getValue()));
            }
            contentHtmlResult = contentHtml;
        }


        //处理购买可见
        List<JXNode> hidePurchases = doc.selN(StrUtil.format("//div[@data-hide='{}']", HIDE_PURCHASE));
        for (JXNode purchase : hidePurchases) {
            String html = purchase.asElement().outerHtml();
            String hideId = purchase.asElement().attr("data-hid");
            String hidePrice = purchase.asElement().attr("data-price");
            if (StrUtil.isEmpty(hideId)) {
                throw new NotePressException("未获取到hide token，请刷新重试！");
            }
            Map<String, Object> hideMap = new HashMap<>(4);
            hideMap.put("hideId", hideId);
            hideMap.put("hideType", HIDE_PURCHASE);
            hideMap.put("contentId", contentId);
            hideMap.put("price", hidePrice);
            String replacement = StrUtil.format(HIDE_PURCHASE_REPLACEMENT, hideMap);
            contentHtml = contentHtml.replace(html, replacement);
            Hide existHideInTable = hideService.getOne(HideQuery.build(hideId, contentId, HideTypeEnum.NOT_PURCHASE.getValue()));
            if (existHideInTable == null) {
                if (StrUtil.isEmpty(hidePrice)) {
                    throw new RuntimeException("隐藏的购买内容，必须填写购买硬币的个数！");
                }
                Hide hide = Hide.builder()
                        .id(hideId).contentId(contentId)
                        .hideType(HideTypeEnum.NOT_PURCHASE).hidePrice(Integer.valueOf(hidePrice)).hideHtml(html).build();
                hideService.save(hide);
            } else {
                hideService.update(HideQuery.build(html, hideId, contentId, HideTypeEnum.NOT_PURCHASE.getValue()));
            }
            contentHtmlResult = contentHtml;
        }

        return contentHtmlResult;
    }


    /**
     * 处理文章内容的显示
     *
     * @param contentId
     * @param contentHtml
     */
    public static String handleShowContent(String contentId, String contentHtml) {
        contentHtml = contentHtml.replace(" />", ">");
        Document document = Jsoup.parse(contentHtml);
        document.outputSettings().prettyPrint(false);
        JXDocument doc = JXDocument.create(document);

        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        //处理回复可见
        if (sessionUser != null) {
            if (sessionUser.getAdmin()) {
                contentHtml = handleShow(doc, contentHtml, HIDE_COMMENT);
            } else {
                long userId = sessionUser.getId();
                SysNoticeMapper sysNoticeMapper = NotePressUtils.getBean(SysNoticeMapper.class);
                int cnt = sysNoticeMapper.selectCount(SysNoticeQuery.build(String.valueOf(userId), contentId));
                if (cnt > 0) {
                    contentHtml = handleShow(doc, contentHtml, HIDE_COMMENT);
                }
            }
        }

        //处理购买可见
        if (sessionUser != null) {
            if (sessionUser.getAdmin()) {
                contentHtml = handleShow(doc, contentHtml, HIDE_PURCHASE);
            } else {
                long userId = sessionUser.getId();
                IHideService hideService = NotePressUtils.getBean(IHideService.class);
                List<JXNode> hides = doc.selN(StrUtil.format("//blockquote[@data-htype='{}']", HIDE_PURCHASE));
                for (JXNode hideNode : hides) {
                    String hideId = hideNode.asElement().attr("data-hid");

                    if (hideService.userIsPurchased(contentId, userId, hideId)) {
                        Hide hide = hideService.getById(hideId);
                        contentHtml = contentHtml.replace(hideNode.asElement().outerHtml(), hide.getHideHtml());
                    }
                }
            }
        }


        //处理登录可见
        if (sessionUser != null) {
            contentHtml = handleShow(doc, contentHtml, HIDE_LOGIN);
        }
        return contentHtml;
    }

    /**
     * 处理前后端分离url的不一致的显示的问题
     *
     * @param content
     * @param isHide  true：去掉https://domain:port，false:添加http://domain:port/
     */
    public static void handleBasePath(Content content, boolean isHide) {
        String htmlContent = content.getHtmlContent();
        String images = content.getImages();
        String cover = content.getCover();
        String mdContent = content.getMdContent();
        HttpServletRequest request = NotePressServletUtils.getRequest();
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/";
        if (isHide) {
            htmlContent = htmlContent.replace(basePath.concat("upfiles/"), "/upfiles/");
        } else {
            htmlContent = htmlContent.replace("/upfiles/", basePath.concat("upfiles/"));
        }
        content.setHtmlContent(htmlContent);
        if (isHide) {
            images = images.replace(basePath.concat("upfiles/"), "/upfiles/");
        } else {
            images = images.replace("/upfiles/", basePath.concat("upfiles/"));
        }
        content.setImages(images);
        content.setImageList(Arrays.asList(images.split(",")));
        if (isHide) {
            cover = cover.replace(basePath.concat("upfiles/"), "/upfiles/");
        } else {
            cover = cover.replace("/upfiles/", basePath.concat("upfiles/"));
        }
        content.setCover(cover);
        if (isHide) {
            mdContent = mdContent.replace(basePath.concat("upfiles/"), "/upfiles/");
        } else {
            mdContent = mdContent.replace("/upfiles/", basePath.concat("upfiles/"));
        }
        content.setMdContent(mdContent);
    }

    /**
     * 处理隐藏内容
     *
     * @param doc
     * @param contentHtml
     * @param hideType
     * @return
     */
    private static String handleShow(JXDocument doc, String contentHtml, String hideType) {
        IHideService hideService = NotePressUtils.getBean(IHideService.class);
        List<JXNode> hides = doc.selN(StrUtil.format("//blockquote[@data-htype='{}']", hideType));
        for (JXNode hideNode : hides) {
            String hideId = hideNode.asElement().attr("data-hid");
            Hide hide = hideService.getById(hideId);
            contentHtml = contentHtml.replace(hideNode.asElement().outerHtml(), hide.getHideHtml());
        }
        return contentHtml;
    }
}
