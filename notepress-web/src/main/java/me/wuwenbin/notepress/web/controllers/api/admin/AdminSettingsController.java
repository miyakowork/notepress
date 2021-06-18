package me.wuwenbin.notepress.web.controllers.api.admin;

import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wuwenbin
 */
@RestController
@RequestMapping("/admin/settings")
public class AdminSettingsController extends NotePressBaseController {

    @Autowired
    private IParamService paramService;

    @GetMapping("/all")
    public NotePressResult all() {
        List<Param> list = paramService.list(ParamQuery.buildByGtGroup("-1"));
        Map<String, Object> paramMap = list.stream().collect(Collectors.toMap(Param::getName, p -> p.getValue() == null ? "" : p.getValue()));
        return writeJsonOk(paramMap);
    }

    /**
     * email相关信息及设置
     */
    @GetMapping("/email")
    public NotePressResult emailSettings() {
        NotePressResult mailInfo = paramService.fetchMailServer();
        if (!mailInfo.isSuccess()) {
            return writeJsonOk(Collections.EMPTY_MAP);
        }
        return writeJson(() -> mailInfo);
    }

    @PostMapping("/updateEmail")
    public NotePressResult updateEmail() {
        return writeJson(() -> paramService.updateMailSettings(getParameterMap(request.getParameterMap())));
    }


    @PostMapping("/update")
    public NotePressResult update(String name, String value) {
        boolean res = paramService.update(ParamQuery.buildUpdate(name, value));
        return writeJsonJudgedBool(res, "修改成功！", "修改失败！");
    }

    @PostMapping("/updateMap")
    public NotePressResult update() {
        Map<String, Object> paramMap = getParameterMap(request.getParameterMap());
        for (String name : paramMap.keySet()) {
            paramService.update(ParamQuery.buildUpdate(name, paramMap.get(name)));
        }
        return writeJsonOkMsg("修改成功！");
    }
}
