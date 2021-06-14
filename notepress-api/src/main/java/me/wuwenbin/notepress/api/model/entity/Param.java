package me.wuwenbin.notepress.api.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

/**
 * created by Wuwenbin on 2019-07-23 at 14:46
 *
 * @author wuwenbin
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class Param extends BaseEntity<Param> {

    private Long id;
    private String name;
    private String value;
    @TableField("`group`")
    private String group;
    private Integer orderIndex;
}
