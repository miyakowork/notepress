package me.wuwenbin.notepress.service.utils;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.utils.NotePressUtils;

import java.util.Map;

/**
 * created by Wuwenbin on 2019/12/4 at 7:01 下午
 *
 * @author wuwenbin
 */
public class NotePressMailUtils {

    /**
     * @param subject    邮件主题/标题
     * @param targetMail
     * @param content
     * @param isHtml
     */
    public static void sendMail(String subject, String targetMail, String content, boolean isHtml) {
        NotePressResult serverResult = NotePressUtils.getBean(IParamService.class).fetchMailServer();
        if (serverResult.isSuccess()) {
            Map<String, String> serverConfig = serverResult.getDataMap(String.class, String.class);
            String host = MapUtil.getStr(serverConfig, ParamKeyConstant.MAIL_SMPT_SERVER_ADDR);
            String port = MapUtil.getStr(serverConfig, ParamKeyConstant.MAIL_SMPT_SERVER_PORT);
            String from = MapUtil.getStr(serverConfig, ParamKeyConstant.MAIL_SERVER_ACCOUNT);
            String user = MapUtil.getStr(serverConfig, ParamKeyConstant.MAIL_SENDER_NAME);
            String pass = MapUtil.getStr(serverConfig, ParamKeyConstant.MAIL_SERVER_PASSWORD);

            MailAccount account = new MailAccount();
            account.setHost(host);
            account.setPort(Integer.valueOf(port));
            account.setAuth(true);
            account.setSslEnable(true);
            account.setFrom(from);
            account.setUser(user);
            account.setPass(pass);
            MailUtil.send(account, targetMail, subject, content, isHtml);
        } else {
            throw new NotePressException("未正确配置邮件服务器");
        }
    }

    /**
     * 以模板信息文本发送邮件
     *
     * @param subject
     * @param targetMail
     * @param isHtml
     * @param templateContent
     * @param templateParam
     */
    public static void sendMailTemplate(String subject, String targetMail, boolean isHtml, String templateContent, Map<String, Object> templateParam) {
        String content = StrUtil.format(templateContent, templateParam);
        sendMail(subject, targetMail, content, isHtml);
    }
}
