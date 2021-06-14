package me.wuwenbin.notepress.api.model.entity.system;

import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * @author wuwen
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysSession extends BaseEntity<SysSession> {

    private String jwtToken;
    private LocalDateTime expired;
    private Long sessionUserId;
    private String sessionUsername;
    /**
     * 是否为admin请求
     */
    private Boolean adminReq = false;

    public SysSession(String jwtToken, LocalDateTime expired) {
        this.jwtToken = jwtToken;
        this.expired = expired;
    }

    public static SysSession admin(String jwtToken) {
        SysSession sysSession = new SysSession(jwtToken, LocalDateTime.now().plusHours(2));
        sysSession.setAdminReq(true);
        sysSession.setGmtCreate(LocalDateTime.now());
        return sysSession;
    }

    public static SysSession user(SysUser user) {
        SysSession sysSession = new SysSession();
        sysSession.setSessionUserId(user.getId());
        sysSession.setSessionUsername(user.getUsername());
        sysSession.setExpired(LocalDateTime.now().plusHours(2));
        sysSession.setAdminReq(false);
        sysSession.setGmtCreate(LocalDateTime.now());
        return sysSession;
    }

}
