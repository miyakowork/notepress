package me.wuwenbin.notepress.service.facade;

import cn.hutool.cache.Cache;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import me.wuwenbin.notepress.api.annotation.NotePressFacade;
import me.wuwenbin.notepress.api.constants.CacheConstant;
import me.wuwenbin.notepress.api.constants.FilePathConstants;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.query.SysUserQuery;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.service.ISysUserService;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.utils.NotePressMailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author wuwen
 */
@NotePressFacade
public class MailFacade {

    @Qualifier("mailCodeCache")
    @Autowired
    private Cache<String, String> mailCodeCache;
    private final IParamService paramService = NotePressUtils.getBean(IParamService.class);


    /**
     * 发送验证码邮件
     *
     * @param email
     */
    public void sendMailCode(String email) {
        Param websiteTitleParam = paramService.getOne(ParamQuery.build((ParamKeyConstant.WEBSITE_NAME)));
        String websiteTitle = websiteTitleParam.getValue();
        String subject = "【" + websiteTitle + "】注册验证码";
        //noinspection MismatchedQueryAndUpdateOfCollection
        Setting mailSetting = new Setting(FilePathConstants.FILE_MAIL_SETTINGS);
        String content = mailSetting.getStr("template_mail_code", "验证码：");
        String code = RandomUtil.randomString(6);
        content = StrUtil.format(content, MapUtil.of("mailCode", code));
        NotePressMailUtils.sendMail(subject, email, content, true);
        String key = email + "-" + CacheConstant.MAIL_CODE_KEY;
        mailCodeCache.remove(key);
        mailCodeCache.put(key, code);
    }

    /**
     * 发送通知消息
     *
     * @param message
     * @param basePath
     */
    public void sendNotice(String message, String basePath, String contentId) {
        String targetMail = NotePressUtils.getBean(ISysUserService.class).getOne(SysUserQuery.buildByAdmin(true)).getEmail();
        Param websiteTitleParam = paramService.getOne(ParamQuery.build((ParamKeyConstant.WEBSITE_NAME)));
        String websiteTitle = websiteTitleParam.getValue();
        String subject = "你的网站 - 【{}】 有新的通知消息，快去查看吧！";
        subject = StrUtil.format(subject, websiteTitle);
        //noinspection MismatchedQueryAndUpdateOfCollection
        Setting noticeSettings = new Setting(FilePathConstants.FILE_MAIL_SETTINGS);
        String content = noticeSettings.getStr("template_notice", "{}");
        if (StrUtil.isEmpty(contentId) || "-1".equalsIgnoreCase(contentId)) {
            contentId = "message";
        } else {
            contentId = "content/".concat(contentId);
        }
        content = StrUtil.format(content, websiteTitle, message, basePath, contentId);
        NotePressMailUtils.sendMail(subject, targetMail, content, true);
    }

    /**
     * 发送通知消息
     *
     * @param targetMail
     * @param contentId
     * @param basePath
     */
    @SuppressWarnings("DuplicatedCode")
    public void sendNotice2User(String targetMail, String contentId, String basePath) {
        Param websiteTitleParam = paramService.getOne(ParamQuery.build((ParamKeyConstant.WEBSITE_NAME)));
        String websiteTitle = websiteTitleParam.getValue();
        String subject = "你在网站【{}】的评论/留言有新回复！";
        subject = StrUtil.format(subject, websiteTitle);
        //noinspection MismatchedQueryAndUpdateOfCollection
        Setting noticeSettings = new Setting(FilePathConstants.FILE_MAIL_SETTINGS);
        String content = noticeSettings.getStr("template_user_notice", "{}");
        content = StrUtil.format(content, basePath, contentId);
        NotePressMailUtils.sendMail(subject, targetMail, content, true);
    }


}
