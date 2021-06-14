package me.wuwenbin.notepress.service.bo;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author wuwen
 */
@Data
@Builder
public class RegisterUserBo implements Serializable {

    @NotNull
    @Length(min = 4, max = 20, message = "4~20字符串之间")
    private String npUsername;

    /**
     * 因为前端已经md5加密了
     */
    @NotNull
    @Length(min = 32, max = 32, message = "密码格式不正确")
    private String npPassword;

    @Length(min = 1, max = 20, message = "1~20字符之间")
    private String npNickname;

    @Email
    private String npMail;

    @NotEmpty
    private String code;
}
