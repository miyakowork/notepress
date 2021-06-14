package me.wuwenbin.notepress.api.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import me.wuwenbin.notepress.api.constants.enums.DictionaryTypeEnum;
import me.wuwenbin.notepress.api.model.entity.Dictionary;

import java.util.Collection;

/**
 * @author wuwenbin
 */
public class DictionaryQuery extends BaseQuery {

    public static QueryWrapper<Dictionary> buildByType(String dictValue, DictionaryTypeEnum dictionaryTypeEnum) {
        return Wrappers.<Dictionary>query().eq("dictionary_type", dictionaryTypeEnum.getValue())
                .eq(StrUtil.isNotEmpty(dictValue), "dict_value", dictValue);
    }

    public static QueryWrapper<Dictionary> buildByTag(String dictValue) {
        return buildByType(dictValue, DictionaryTypeEnum.TAG);
    }

    public static QueryWrapper<Dictionary> buildByIdCollection(Collection<String> idCollection) {
        return Wrappers.<Dictionary>query().in("id", idCollection);
    }

}
