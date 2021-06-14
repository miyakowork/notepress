package me.wuwenbin.notepress.api.constants.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import lombok.Getter;

/**
 * 字典类型
 * created by Wuwenbin on 2019/11/19 at 5:05 下午
 *
 * @author wuwenbin
 */
public enum DictionaryTypeEnum implements IEnum<String> {


    /**
     * 标签
     */
    TAG("标签"),

    /**
     * 敏感字词
     */
    SENSITIVE_WORD("敏感字词"),

    /**
     * 友情链接
     */
    LINK("友情链接");

    @Getter
    private String label;

    DictionaryTypeEnum(String label) {
        this.label = label;
    }

    /**
     * 枚举数据库存储值
     */
    @Override
    public String getValue() {
        return this.name();
    }
}
