package me.wuwenbin.notepress.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.model.entity.system.SysLog;
import me.wuwenbin.notepress.api.service.ISysLogService;
import me.wuwenbin.notepress.service.mapper.SysLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @author wuwen
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class ISysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements ISysLogService {

    private final SysLogMapper sysLogMapper;

    /**
     * 查找日志
     *
     * @param param
     * @return
     */
    @Override
    public List<SysLog> findSysLogs(Map<String, Object> param) {
        String sql = "select * from np_sys_log where url in(:urls) and time >= :time";
        return sysLogMapper.findListBeanByMap(sql, SysLog.class, param);
    }
}
