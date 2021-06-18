package me.wuwenbin.notepress.service.facade;

import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import me.wuwenbin.notepress.api.annotation.NotePressFacade;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author wuwenbin
 */
@NotePressFacade
public class ThemeFacade {

    @Autowired
    private IParamService paramService;

    /**
     * 初始化主题设置
     *
     * @param theme
     */
    public void initThemeConf(String theme) {
        Setting settings = NotePressUtils.getThemeSetting(theme);
        String paramKey = settings.getStr("paramKey");
        Map<String, String> paramValueMap = settings.getMap("conf");
        String paramValue = JSONUtil.toJsonStr(paramValueMap)
                .replace("\\\"", "\"")
                .replace("\"{", "{")
                .replace("}\"", "}");
        Param param = Param.builder().group("3").name(paramKey).orderIndex(0).value(paramValue).build();
        param.setRemark("主题配置的key/参数名");
        paramService.save(param);
    }
}
