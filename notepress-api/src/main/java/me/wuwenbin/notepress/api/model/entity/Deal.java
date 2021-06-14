package me.wuwenbin.notepress.api.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

/**
 * @author wuwen
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class Deal extends BaseEntity<Deal> {

    private Long id;
    private Long userId;
    private Object dealTargetId;
    private Integer dealAmount;
}
