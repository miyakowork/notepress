package me.wuwenbin.notepress.web.controllers.api.admin;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * @author wuwenbin
 */
@RestController
@RequestMapping("/admin/theme")
public class AdminThemeController extends NotePressBaseController {

    @Autowired
    private IParamService paramService;

    @PostMapping("/update")
    public NotePressResult update(String confStr) {
        //当前使用的主题名称
        String currentThemeName = paramService.fetchParamByName(ParamKeyConstant.THEME_NAME).getDataBean(Param.class).getValue();
        //主题配置文件的配置信息（一般是默认配置信息）
        Setting setting = NotePressUtils.getThemeSetting(currentThemeName);
        NotePressResult setR = paramService.fetchThemeSetting();
        if (setR.isSuccess()) {
            Param param = setR.getDataBean(Param.class);
            String welCardInfoJson = param.getValue();
            JSONObject dbJsonObj = JSONUtil.parseObj(welCardInfoJson);
            JSONObject reqJsonObj = JSONUtil.parseObj(confStr);
            Set<String> reqKeySet = reqJsonObj.keySet();
            Set<String> dbKeySet = dbJsonObj.keySet();
            for (String reqKey : reqKeySet) {
                if (CollectionUtil.contains(dbKeySet, reqKey)) {
                    dbJsonObj.remove(reqKey);
                }
                dbJsonObj.put(reqKey, reqJsonObj.get(reqKey));
            }
            paramService.update(ParamQuery.buildUpdate(setting.getStr("paramKey"), JSONUtil.toJsonStr(dbJsonObj)));
            return writeJsonOkMsg("更新成功！");
        }
        return writeJson(() -> setR);
    }
}
