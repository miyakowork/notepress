package me.wuwenbin.notepress.api.model.entity.system;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.wuwenbin.notepress.api.annotation.query.SimpleCondition;
import me.wuwenbin.notepress.api.annotation.query.WrapperCondition;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

/**
 * created by Wuwenbin on 2019-08-05 at 13:32
 *
 * @author wuwenbin
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity<SysUser> {

    private Long id;
    @Length(min = 4, max = 20, message = "4~20字符之间")
    @WrapperCondition(SimpleCondition.like)
    private String username;

    /**
     * 两次md5加密
     */
    @NotEmpty
    @Length(min = 32, max = 32, message = "密码格式不正确")
    private String password;
    @WrapperCondition(SimpleCondition.like)
    @Length(min = 1, max = 20, message = "1~20字符之间")
    private String nickname;
    private String avatar;
    @WrapperCondition(SimpleCondition.like)
    @Email
    private String email;
    private Boolean status;
    private String lastLoginIp;
    private String lastLoginAddr;
    private LocalDateTime lastLoginTime;

    /**
     * 是否为管理员
     */
    private Boolean admin;


}
