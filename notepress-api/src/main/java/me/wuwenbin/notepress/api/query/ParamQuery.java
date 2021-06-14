package me.wuwenbin.notepress.api.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import me.wuwenbin.notepress.api.model.entity.Param;

/**
 * 常用查询条件拼装
 * created by Wuwenbin on 2019/11/18 at 3:24 下午
 *
 * @author wuwenbin
 */
public class ParamQuery extends BaseQuery {

    public static QueryWrapper<Param> build(String name) {
        return Wrappers.<Param>query().eq("name", name);
    }

    public static UpdateWrapper<Param> buildUpdate(String name, Object value) {
        return Wrappers.<Param>update().set("`value`", value).eq("`name`", name);
    }

    public static QueryWrapper<Param> buildByGtGroup(String group) {
        return Wrappers.<Param>query().gt(StrUtil.isNotEmpty(group), "`group`", group);
    }

}
