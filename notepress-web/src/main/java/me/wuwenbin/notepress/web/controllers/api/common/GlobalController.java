package me.wuwenbin.notepress.web.controllers.api.common;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wuwenbin
 */
@RestController
@RequestMapping("/global")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class GlobalController extends NotePressBaseController {

    private final IParamService paramService;

    @GetMapping("/conf")
    public NotePressResult conf() {
        List<Param> params = paramService.list(ParamQuery.buildByGtGroup("0"));
        Map<String, Object> settingsMap = params.stream().collect(Collectors.toMap(Param::getName, p -> StrUtil.nullToEmpty(p.getValue())));
        return writeJsonOk(settingsMap);
    }

}
