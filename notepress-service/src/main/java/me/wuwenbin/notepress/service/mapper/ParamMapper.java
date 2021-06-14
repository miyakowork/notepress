package me.wuwenbin.notepress.service.mapper;

import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.service.mapper.base.NotePressMapper;

/**
 * @author wuwen
 */
@MybatisMapper
public interface ParamMapper extends NotePressMapper<Param> {

    /**
     * 更新参数
     *
     * @param name
     * @param value
     * @return int
     * @throws Exception
     */
    default int updateValueByName(String name, Object value) {
        String sql = "update np_param set `value` = ? where `name` = ?";
        return this.executeArray(sql, value, name);
    }
}
