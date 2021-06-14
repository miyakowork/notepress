package me.wuwenbin.notepress.api.service;

import me.wuwenbin.notepress.api.model.entity.system.SysLog;
import me.wuwenbin.notepress.api.service.base.INotePressService;

import java.util.List;
import java.util.Map;

/**
 * @author wuwen
 */
public interface ISysLogService extends INotePressService<SysLog> {

    /**
     * 查找日志
     *
     * @param param
     * @return
     */
    List<SysLog> findSysLogs(Map<String,Object> param);
}
