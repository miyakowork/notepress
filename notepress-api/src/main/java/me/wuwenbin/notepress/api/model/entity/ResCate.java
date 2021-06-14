package me.wuwenbin.notepress.api.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.annotation.query.SimpleCondition;
import me.wuwenbin.notepress.api.annotation.query.WrapperCondition;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

/**
 * 分类表
 * created by Wuwenbin on 2019/11/19 at 5:37 下午
 *
 * @author wuwenbin
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class ResCate extends BaseEntity<ResCate> {

    private Long id;
    /**
     * 这个是标准化的名字，比如url调用等，一般使用英文
     */
    @WrapperCondition(SimpleCondition.like)
    private String name;
    private Long pid;

    private Integer orderIndex;

}
