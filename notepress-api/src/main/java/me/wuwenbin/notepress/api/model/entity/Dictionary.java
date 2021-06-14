package me.wuwenbin.notepress.api.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.annotation.query.SimpleCondition;
import me.wuwenbin.notepress.api.annotation.query.WrapperCondition;
import me.wuwenbin.notepress.api.constants.enums.DictionaryTypeEnum;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author wuwenbin
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class Dictionary extends BaseEntity<Dictionary> {

    private Long id;

    /**
     * 字典标签
     */
    @NotEmpty
    @Length(max = 50, min = 1, message = "1~50个字符之间")
    private String dictLabel;

    /**
     * 字典的值，全部使用模糊查询
     */
    @NotNull
    @WrapperCondition(SimpleCondition.like)
    private String dictValue;

    /**
     * 状态,是否在用
     */
    private Boolean status;

    /**
     * 是否为默认
     */
    private Boolean isDefault;

    /**
     * 字典类型
     */
    private DictionaryTypeEnum dictionaryType;


}
