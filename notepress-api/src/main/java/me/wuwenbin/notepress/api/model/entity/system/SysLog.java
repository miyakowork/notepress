package me.wuwenbin.notepress.api.model.entity.system;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * @author wuwen
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class SysLog extends BaseEntity<SysLog> {

    private String id;
    private String contentType;
    private String ipAddr;
    private String ipInfo;
    private String requestMethod;
    private String sessionId;
    private String url;
    private String userAgent;
    private String username;
    private LocalDateTime time;
    private String browser;
    private Long userId;

}
