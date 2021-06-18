package me.wuwenbin.notepress.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import me.wuwenbin.notepress.api.constants.NotePressConstants;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.utils.NotePressCacheUtils;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.bo.IndexMenu;
import me.wuwenbin.notepress.service.mapper.ParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wuwen
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ParamServiceImpl extends ServiceImpl<ParamMapper, Param> implements IParamService {

    @Autowired
    private ParamMapper paramMapper;

    @Override
    public NotePressResult fetchIndexMenu() {
        Param param = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.THEME_NAME));
        if (param != null) {
            return NotePressResult.createOkData(IndexMenu.fetchMenuByTheme(param.getValue()));
        }
        return NotePressResult.createOkData(IndexMenu.fetchMenuByTheme(null));
    }

    /**
     * 文件上传路径
     *
     * @return
     */
    @Override
    public NotePressResult fetchFileUploadInfo() {
        String defaultUploadPath = NotePressUtils.rootPath().concat("/").concat(NotePressConstants.DEFAULT_UPLOAD_PATH).concat("/");
        return this.fetchSettingsValByKey("app", "uploadPath", String.class, defaultUploadPath);
    }


    /**
     * 初始化状态 1 或 0（初始化 & 未初始化）
     *
     * @return
     */
    @Override
    public NotePressResult fetchInitStatus() {
        Object cacheObj = NotePressCacheUtils.get(ParamKeyConstant.SYSTEM_INIT_STATUS);
        if (cacheObj != null) {
            Param initStatusParamCache = (Param) cacheObj;
            boolean isOk = "1".equals(initStatusParamCache.getValue());
            return NotePressResult.createOk(isOk ? "初始化已完成" : "未进行初始化", isOk);
        } else {
            Param initStatus = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.SYSTEM_INIT_STATUS));
            if (initStatus != null) {
                NotePressCacheUtils.put(ParamKeyConstant.SYSTEM_INIT_STATUS, initStatus);
                boolean isOk = "1".equals(initStatus.getValue());
                return NotePressResult.createOk(isOk ? "初始化已完成" : "未进行初始化设置", isOk);
            }
            return NotePressResult.createOk("未进行初始化设置", false);
        }
    }

    /**
     * 邮件服务器配置
     *
     * @return
     */
    @Override
    public NotePressResult fetchMailServer() {
        Param mailSenderNameParam = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.MAIL_SENDER_NAME));
        Param mailServerAccountParam = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.MAIL_SERVER_ACCOUNT));
        Param mailServerPasswordParam = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.MAIL_SERVER_PASSWORD));
        Param mailServerAddrParam = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.MAIL_SMPT_SERVER_ADDR));
        Param mailServerPortParam = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.MAIL_SMPT_SERVER_PORT));
        if (mailSenderNameParam != null && mailServerAccountParam != null &&
                mailServerPasswordParam != null && mailServerAddrParam != null && mailServerPortParam != null) {
            String senderName = mailSenderNameParam.getValue();
            String serverAccount = mailServerAccountParam.getValue();
            String serverPassword = mailServerPasswordParam.getValue();
            String serverAddr = mailServerAddrParam.getValue();
            String serverPort = mailServerPortParam.getValue();
            if (StrUtil.isNotEmpty(senderName) && StrUtil.isNotEmpty(serverAccount) &&
                    StrUtil.isNotEmpty(serverPassword) && StrUtil.isNotEmpty(serverAddr) && StrUtil.isNotEmpty(serverPort)) {
                Map<String, String> mailServerConfigMap = new HashMap<>(5);
                mailServerConfigMap.put(ParamKeyConstant.MAIL_SENDER_NAME, senderName);
                mailServerConfigMap.put(ParamKeyConstant.MAIL_SERVER_ACCOUNT, serverAccount);
                mailServerConfigMap.put(ParamKeyConstant.MAIL_SERVER_PASSWORD, serverPassword);
                mailServerConfigMap.put(ParamKeyConstant.MAIL_SMPT_SERVER_ADDR, serverAddr);
                mailServerConfigMap.put(ParamKeyConstant.MAIL_SMPT_SERVER_PORT, serverPort);
                return NotePressResult.createOkData(mailServerConfigMap);
            }
        }
        return NotePressResult.createErrorMsg("邮件服务器配置不完全");
    }

    @Override
    public NotePressResult fetchParamByName(String name) {
        Object obj = NotePressCacheUtils.get(name);
        if (obj == null) {
            return NotePressResult.createOkData(paramMapper.selectOne(ParamQuery.build(name)));
        }
        return NotePressResult.createOkData(obj);
    }

    @Override
    @SneakyThrows
    public NotePressResult updateMailSettings(Map<String, Object> mailParamMap) {
        paramMapper.updateValueByName(ParamKeyConstant.MAIL_SMPT_SERVER_ADDR, mailParamMap.get(ParamKeyConstant.MAIL_SMPT_SERVER_ADDR).toString());
        paramMapper.updateValueByName(ParamKeyConstant.MAIL_SMPT_SERVER_PORT, mailParamMap.get(ParamKeyConstant.MAIL_SMPT_SERVER_PORT).toString());
        paramMapper.updateValueByName(ParamKeyConstant.MAIL_SERVER_ACCOUNT, mailParamMap.get(ParamKeyConstant.MAIL_SERVER_ACCOUNT).toString());
        paramMapper.updateValueByName(ParamKeyConstant.MAIL_SERVER_PASSWORD, mailParamMap.get(ParamKeyConstant.MAIL_SERVER_PASSWORD).toString());
        paramMapper.updateValueByName(ParamKeyConstant.MAIL_SENDER_NAME, mailParamMap.get(ParamKeyConstant.MAIL_SENDER_NAME).toString());
        return NotePressResult.createOkMsg("更新邮件服务器配置成功！");
    }

    @Override
    public NotePressResult fetchThemeSetting() {
        Param param = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.THEME_NAME));
        if (param != null && StrUtil.isNotEmpty(param.getValue())) {
            String currentThemeName = param.getValue();
            Setting setting = NotePressUtils.getThemeSetting(currentThemeName);
            Param themeConfParam = paramMapper.selectOne(ParamQuery.build(setting.getStr("paramKey")));
            return NotePressResult.createOkData(themeConfParam);
        }
        return NotePressResult.createErrorMsg("获取当前主题配置对象信息失败！");
    }

    /**
     * 更新插入一个字段
     *
     * @param param
     * @return
     */
    @Override
    public NotePressResult upsertParam(Param param) {
        String paramName = param.getName();
        String paramValue = param.getValue();
        int c = paramMapper.selectCount(ParamQuery.build(paramName));
        if (c > 0) {
            paramMapper.updateValueByName(paramName, paramValue);
            return NotePressResult.createOk("更新成功", param);
        } else {
            paramMapper.insert(param);
            return NotePressResult.createOk("插入成功", param);
        }
    }
}
