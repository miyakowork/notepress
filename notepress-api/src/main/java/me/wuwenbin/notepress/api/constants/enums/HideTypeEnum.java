package me.wuwenbin.notepress.api.constants.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;

/**
 * @author wuwenbin
 */
public enum HideTypeEnum implements IEnum<String> {
    /**
     * 文章内容中隐藏的类型
     * 未登录隐藏
     * 未评论隐藏
     * 未购买隐藏
     */
    NOT_LOGIN,
    NOT_COMMENT,
    NOT_PURCHASE;

    @Override
    public String getValue() {
        return this.name().toLowerCase();
    }
}
