package me.wuwenbin.notepress.api.model.entity.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author wuwen
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Data
public abstract class BaseEntity<T extends BaseEntity> implements Serializable {
    /**
     * 创建日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    protected LocalDateTime gmtCreate;

    /**
     * 修改日期
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    protected LocalDateTime gmtUpdate;

    /**
     * 创建人
     */
    protected Long createBy;

    /**
     * 修改人
     */
    protected Long updateBy;

    /**
     * 备注信息
     */
    protected String remark;

    public T gmtCreate(LocalDateTime gmtCreate) {
        this.setGmtCreate(gmtCreate);
        return (T) this;
    }

    public T gmtUpdate(LocalDateTime gmtUpdate) {
        this.setGmtUpdate(gmtUpdate);
        return (T) this;
    }

    public T createBy(Long createBy) {
        this.setCreateBy(createBy);
        return (T) this;
    }

    public T updateBy(Long updateBy) {
        this.setUpdateBy(updateBy);
        return (T) this;
    }

    public T remark(String remark) {
        this.setRemark(remark);
        return (T) this;
    }
}
