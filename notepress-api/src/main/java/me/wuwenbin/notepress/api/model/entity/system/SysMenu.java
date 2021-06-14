package me.wuwenbin.notepress.api.model.entity.system;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

/**
 * created by Wuwenbin on 2019/12/4 at 10:47 上午
 * @author wuwenbin
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class SysMenu extends BaseEntity<SysMenu> {

    private Long id;
}
