package me.wuwenbin.notepress.web.controllers.api.admin;

import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.system.SysLog;
import me.wuwenbin.notepress.api.query.ContentQuery;
import me.wuwenbin.notepress.api.query.SysUserQuery;
import me.wuwenbin.notepress.api.service.IContentService;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.service.ISysLogService;
import me.wuwenbin.notepress.api.service.ISysUserService;
import me.wuwenbin.notepress.api.utils.NotePressServerUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wuwen
 */
@RestController
@RequestMapping("/admin/index")
public class AdminIndexController extends NotePressBaseController {

    @Autowired
    private IParamService paramService;
    @Autowired
    private IContentService contentService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ISysLogService sysLogService;

    @GetMapping("/menu")
    public NotePressResult menuJson() {
        return writeJson(paramService::fetchIndexMenu);
    }

    @GetMapping("/dashboard")
    public NotePressResult dashboard() {
        Map<String, Object> pMap = new HashMap<>(15);
        pMap.put("postCnt", contentService.count());
        pMap.put("regUser", userService.count(SysUserQuery.buildByAdmin(false)));
        pMap.put("ip", ipSum(null));
        pMap.put("pv", pvSum(null).size());
        pMap.put("todayPostCnt", contentService.count(ContentQuery.buildTodayCount()));
        pMap.put("todayRegUser", userService.count(SysUserQuery.buildTodayCount()));
        pMap.put("todayIp", ipSum(LocalDate.now()));
        pMap.put("todayPv", pvSum(LocalDate.now()).size());
        pMap.put("system_started_datetime", paramService.fetchParamByName(ParamKeyConstant.SYSTEM_STARTED_DATETIME).getDataBean(Param.class).getValue());
        pMap.put("notepress-version", NotePressServerUtils.version());
        pMap.put("layui", NotePressServerUtils.layuiVersion());
        pMap.put("os", NotePressServerUtils.osName());
        pMap.put("jdk", NotePressServerUtils.javaVersion());
        pMap.put("cpu", NotePressServerUtils.cpu());
        pMap.put("mem", NotePressServerUtils.maxMemory());
        return writeJsonOk(pMap);
    }

    private List<SysLog> pvSum(LocalDate time) {
        Map<String, Object> param = new HashMap<>(2);
        List<String> urls = new ArrayList<>();
        String statisticsMethodType = toRNull(paramService.fetchParamByName(ParamKeyConstant.STATISTICS_METHOD), Param.class, Param::getValue);
        if (!StringUtils.isEmpty(statisticsMethodType)) {
            List<String> dbSetList = Arrays.asList(statisticsMethodType.split("\\|"));
            if (dbSetList.contains("admin")) {
                urls.add("/admin/");
            }
            if (dbSetList.contains("content")) {
                urls.add("/content/");
            }
            if (dbSetList.contains("home_index")) {
                urls.add("/index");
            }
            if (dbSetList.contains("other")) {
                urls.add("/purchase");
                urls.add("/message");
                urls.add("/note");
                urls.add("/token/ubs");
                urls.add("/res");
            }
            param.put("urls", urls);
            if (time != null) {
                param.put("time", time);
            } else {
                param.put("time", LocalDateTime.of(2000, 1, 1, 0, 0, 0));
            }
        }
        List<SysLog> r = sysLogService.findSysLogs(param);
        return CollectionUtils.isEmpty(r) ? new ArrayList<>() : r;
    }

    private int ipSum(LocalDate time) {
        List<SysLog> sysLogs = pvSum(time).stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SysLog::getIpAddr))), ArrayList::new));
        return CollectionUtils.isEmpty(sysLogs) ? 0 : sysLogs.size();
    }

}
