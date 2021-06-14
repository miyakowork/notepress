package me.wuwenbin.notepress.springboot.container.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.Setting;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.constants.FilePathConstants;
import me.wuwenbin.notepress.api.constants.NotePressConstants;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.utils.NotePressServletUtils;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.facade.ThemeFacade;
import me.wuwenbin.notepress.service.mapper.SysSessionMapper;
import me.wuwenbin.notepress.web.controllers.utils.NotePressWebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wuwen
 */
@Slf4j
@Component
@Order(1)
public class StartInitListener implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired
    private IParamService paramService;
    @Qualifier("notePressSetting")
    @Autowired
    private Setting notePressSetting;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SysSessionMapper sessionMapper;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        log.info("启动 NotePress 中，请稍候...");
        if (appStartNeedInitDatabase()) {
            log.info("检测到配置需要初始化数据库，开始执行初始化数据库...");
            truncateTable();
            log.info("执行初始化数据库完毕，进行下一步...");


            log.info("正在进行设置参数表，请稍候...");
            setUpParams();
            log.info("设置参数表完毕，进行下一步...");
            setUpSystemInitTime();

            log.info("正在进行主题初始化");
            NotePressUtils.getBean(ThemeFacade.class)
                    .initThemeConf(paramService.getOne(ParamQuery.build("theme_name")).getValue());
            log.info("主题设置初始化数据完毕，进行下一步");
        }
        log.info("正在创建上传文件夹，请稍候...");
        setUploadPath();
        log.info("设置上传文件夹完毕，进行下一步...");

        setUpSystemStartedTime();
        clearSession();
        log.info("即将启动完成，请稍后...");
    }

    /**
     * 检测是否配置需要重新初始化数据库
     * 配置文件中
     *
     * @return
     */
    private boolean appStartNeedInitDatabase() {
        boolean c1 = paramService.fetchSettingsValByKey("app", "startInit", Boolean.class, false).getBoolData();
        Param p1 = paramService.getOne(ParamQuery.build(ParamKeyConstant.SYSTEM_INIT_STATUS));
        boolean c2 = p1 == null || StrUtil.isNotEmpty(p1.getValue()) && "0".equals(p1.getValue());
        return c1 && c2;
    }

    /**
     * 初始化数据库
     */
    private void truncateTable() {
        String sql = "select TABLE_NAME from information_schema.`TABLES` WHERE table_schema = ?";
        String dbName = notePressSetting.getStr("name", "db", "notepress");
        List<String> tableNames = jdbcTemplate.queryForList(sql, String.class, dbName);
        for (String tableName : tableNames) {
            String s1 = String.format("TRUNCATE  %s", tableName);
            jdbcTemplate.update(s1);
            String s2 = String.format("ALTER TABLE %s auto_increment = 1;", tableName);
            jdbcTemplate.update(s2);
        }
    }


    /**
     * 设置参数表
     */
    private void setUpParams() {
        List<Param> params = getParams();
        for (Param param : params) {
            paramService.save(param);
        }
    }

    /**
     * 设置系统初始化时间
     */
    private void setUpSystemInitTime() {
        LocalDateTime now = LocalDateTime.now();
        String formatDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("NotePress 初始化时间 ==> {}", formatDate);
        paramService.update(ParamQuery.buildUpdate(ParamKeyConstant.SYSTEM_INIT_DATETIME, formatDate));
    }

    /**
     * 设置系统启动时间
     */
    private void setUpSystemStartedTime() {
        LocalDateTime now = LocalDateTime.now();
        String formatDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("NotePress 启动时间 ==> {}", formatDate);
        paramService.update(ParamQuery.buildUpdate(ParamKeyConstant.SYSTEM_STARTED_DATETIME, formatDate));
    }

    /**
     * 设置上传文件夹
     */
    private void setUploadPath() {
        log.info("开始设置上传文件夹");
        String path = notePressSetting.get("app", "uploadPath");
        if (!StringUtils.isEmpty(path)) {
            log.info("文件上传目录设置为 ==>「{}」", path);
            setUpFilePath(path);
        } else {
            try {
                String defaultUploadPath = NotePressUtils.rootPath().concat("/").concat(NotePressConstants.DEFAULT_UPLOAD_PATH).concat("/");
                setUpFilePath(defaultUploadPath);
            } catch (Exception e) {
                log.error("上传路径未正确设置");
                throw new NotePressException("上传路径未正确设置，原因 ==> 配置文件「notepress.setting」中属性「uploadPath」未设置或设置有误！");
            }
        }
        log.info("设置文件夹成功");
    }


    private List<Param> getParams() {
        List<Param> params = new ArrayList<>();
        int group = -1, groupMax = 3;
        while (group < groupMax) {
            String groupStr = String.valueOf(group);
            Map<String, String> paramsMap = new Setting(FilePathConstants.FILE_PARAM_SETTINGS).getMap(groupStr);
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                String mapKey = entry.getKey();
                String mapValue = entry.getValue();
                String[] remarkAndDefault = mapValue.split(",");
                String remark = remarkAndDefault[0];
                String defaultValue = remarkAndDefault.length > 1 ? remarkAndDefault[1] : null;
                Param param = Param.builder()
                        .name(mapKey).group(groupStr).value(defaultValue).orderIndex(0)
                        .build()
                        .remark(remark).gmtCreate(LocalDateTime.now());
                params.add(param);
            }
            group++;
        }
        return params;
    }


    private void setUpFilePath(String path) {
        path = path.replace("file:", "");
        File filePath = new File(path + "file/");
        File imgPath = new File(path + "img/");
        boolean f = false, i = false;
        if (!filePath.exists() && !filePath.isDirectory()) {
            f = filePath.mkdirs();
        }
        if (!imgPath.exists() && !imgPath.isDirectory()) {
            i = imgPath.mkdirs();
        }
        if (f && i) {
            log.info("创建上传目录成功 ==>：「{}」和「{}」", path + "file/", path + "img/");
        } else if (f) {
            log.info("已存在文件夹或创建文件夹出错： ==> 「{}」", (path + "img/"));
        } else if (i) {
            log.info("已存在文件夹或创建文件夹出错： ==> 「{}」", (path + "file/"));
        } else {
            log.info("已存在文件夹或创建文件夹出错： ==> 「{}」和「{}」", (path + "img/"), (path + "file/"));
        }
    }

    /**
     * 清除会话消息
     */
    private void clearSession() {
        sessionMapper.executeArray("TRUNCATE np_sys_session");
    }

}
