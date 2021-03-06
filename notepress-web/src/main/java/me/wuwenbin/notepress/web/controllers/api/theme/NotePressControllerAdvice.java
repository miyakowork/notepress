package me.wuwenbin.notepress.web.controllers.api.theme;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.service.*;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wuwenbin
 */
@ControllerAdvice(basePackages = "me.wuwenbin.notepress.web.controllers")
public class NotePressControllerAdvice extends NotePressBaseController {

    @Autowired
    private IParamService paramService;
    @Autowired
    private ICategoryService categoryService;
    @Autowired
    private IContentService contentService;
    @Autowired
    private ISysNoticeService sysNoticeService;
    @Autowired
    private IDealService dealService;


    @ModelAttribute("settings")
    public void addSettings(Model model, HttpServletRequest request) {
        List<Param> params = paramService.list(ParamQuery.buildByGtGroup("0"));
        Map<String, Object> settingsMap = params.stream().collect(Collectors.toMap(Param::getName, p -> StrUtil.nullToEmpty(p.getValue())));
        Map<String, Object> settings = handleThemeConf(settingsMap);
        model.addAttribute("settings", settings);
        model.addAttribute("npsu", sessionUser());
        if (!request.getRequestURL().toString().contains("/admin/")) {
            model.addAttribute("cateList", categoryService.list());
            model.addAttribute("contentCount", contentService.count());
            model.addAttribute("blogWords", Optional.of(contentService.sumContentWords()).orElse(writeJsonOk("")).getData());
            model.addAttribute("runningDays", calcRunningDays());
            model.addAttribute("commentCount", sysNoticeService.count());
        }
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param settings
     * @return
     */
    @ModelAttribute("themeSettings")
    public JSONObject themeSettings(@ModelAttribute("settings") HashMap<String, Object> settings) {
        String themeName = MapUtil.getStr(settings, ParamKeyConstant.THEME_NAME);
        String themeConfKeyInDb = NotePressUtils.getThemeDbParamKeyByThemeName(themeName);
        String jsonStr = MapUtil.getStr(settings, themeConfKeyInDb);
        return JSONUtil.parseObj(jsonStr);
    }

//=================================????????????=======================

    private Map<String, Object> handleThemeConf(Map<String, Object> settingsMap) {
        String themeName = MapUtil.getStr(settingsMap, ParamKeyConstant.THEME_NAME);
        String themeConfKeyInDb = NotePressUtils.getThemeDbParamKeyByThemeName(themeName);
        String jsonStr = MapUtil.getStr(settingsMap, themeConfKeyInDb);
        MapUtil.removeAny(settingsMap, themeConfKeyInDb);
        settingsMap.put(themeConfKeyInDb, JSONUtil.parseObj(jsonStr));
        return settingsMap;
    }

    /**
     * ??????????????????user?????????????????????????????????
     *
     * @return
     */
    private Map<String, Object> sessionUser() {
        SysUser user = NotePressSessionUtils.getSessionUser();
        if (user != null) {
            Map<String, Object> userMap = BeanUtil.beanToMap(user);
            userMap.remove("password");
            Integer c = toRNull(dealService.findCoinSumByUserId(user.getId()), Integer.class, coin -> coin);
            userMap.putIfAbsent("coin", c);
            return userMap;
        }
        return null;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private long calcRunningDays() {
        Param startParam = paramService.getOne(ParamQuery.build(ParamKeyConstant.SYSTEM_OPERATION_DATETIME));
        if (startParam != null) {
            if (StrUtil.isEmpty(startParam.getValue())) {
                startParam = paramService.getOne(ParamQuery.build(ParamKeyConstant.SYSTEM_INIT_DATETIME));
            }
            Date init = DateUtil.parse(startParam.getValue());
            Date now = DateUtil.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return DateUtil.between(init, now, DateUnit.DAY);
        }
        return 0;
    }
}
