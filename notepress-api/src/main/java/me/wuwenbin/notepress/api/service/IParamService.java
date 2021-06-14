package me.wuwenbin.notepress.api.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.setting.Setting;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.service.base.INotePressService;
import me.wuwenbin.notepress.api.utils.NotePressUtils;

import java.util.Map;

/**
 * created by Wuwenbin on 2019/11/22 at 10:10 上午
 *
 * @author wuwenbin
 */
public interface IParamService extends INotePressService<Param> {

    /**
     * 获取配置文件的对象值
     *
     * @param group
     * @param key
     * @param clazz
     * @param defaultValue
     * @param <T>
     * @return
     */
    default <T> NotePressResult fetchSettingsValByKey(String group, String key, Class<T> clazz, T defaultValue) {
        Setting notePressSetting = NotePressUtils.getApplicationContext().getBean("notePressSetting", Setting.class);
        if (clazz == String.class) {
            String val = notePressSetting.getStr(key, group, (String) defaultValue);
            return NotePressResult.createOkData(val);
        } else if (clazz == Integer.class) {
            Integer val = notePressSetting.getInt(key, group, (Integer) defaultValue);
            return NotePressResult.createOkData(val);
        } else if (clazz == Double.class) {
            Double val = notePressSetting.getDouble(key, group, (Double) defaultValue);
            return NotePressResult.createOkData(val);
        } else if (clazz == Boolean.class) {
            Boolean val = notePressSetting.getBool(key, group, (Boolean) defaultValue);
            return NotePressResult.createOkData(val);
        } else if (clazz == Long.class) {
            Long val = notePressSetting.getLong(key, group, (Long) defaultValue);
            return NotePressResult.createOkData(val);
        } else {
            Object val = notePressSetting.get(group, key);
            val = ObjectUtil.isEmpty(val) ? defaultValue : val;
            return NotePressResult.createOkData(val);
        }
    }

    /**
     * 根据主题获取对应的后台管理菜单
     *
     * @return
     */
    NotePressResult fetchIndexMenu();

    /**
     * 文件上传路径
     *
     * @return
     */
    NotePressResult fetchFileUploadInfo();


    /**
     * 初始化状态 1 或 0（初始化 & 未初始化）
     *
     * @return
     */
    NotePressResult fetchInitStatus();

    /**
     * 邮件服务器配置
     *
     * @return
     */
    NotePressResult fetchMailServer();

    /**
     * 根据name获取参数
     *
     * @param name
     * @return
     */
    NotePressResult fetchParamByName(String name);

    /**
     * 更新邮件设置
     *
     * @param mailParamMap
     * @return
     */
    NotePressResult updateMailSettings(Map<String, Object> mailParamMap);

    /**
     * 获取当前使用的主题的配置信息对象
     *
     * @return
     */
    NotePressResult fetchThemeSetting();

    /**
     * 更新插入一个字段
     *
     * @param param
     * @return
     */
    NotePressResult upsertParam(Param param);
}
