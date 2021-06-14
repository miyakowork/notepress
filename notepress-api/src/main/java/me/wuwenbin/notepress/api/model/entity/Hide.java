package me.wuwenbin.notepress.api.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.constants.enums.HideTypeEnum;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

/**
 * @author wuwen
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Hide extends BaseEntity<Hide> {

    @TableId(type = IdType.INPUT)
    private String id;
    private String contentId;
    private HideTypeEnum hideType;
    private Integer hidePrice;
    private String hideHtml;
}
