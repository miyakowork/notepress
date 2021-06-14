package me.wuwenbin.notepress.api.model.entity;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

import java.util.Map;

/**
 * @author wuwenbin
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Refer extends BaseEntity<Refer> {
    private Long id;
    private String selfId;
    private String referId;
    private ReferTypeEnum referType;
    private String referExtra;

    public <T> T getReferExtra(Class<T> clazz) {
        if (StrUtil.isNotEmpty(this.referExtra)) {
            return JSONUtil.toBean(this.referExtra, clazz);
        }
        return null;
    }

    public Map<String, Object> getReferExtra() {
        if (StrUtil.isNotEmpty(this.referExtra)) {
            //noinspection unchecked
            return JSONUtil.toBean(this.referExtra, Map.class);
        }
        return null;
    }
}
