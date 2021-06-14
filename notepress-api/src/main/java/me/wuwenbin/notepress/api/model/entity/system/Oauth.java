package me.wuwenbin.notepress.api.model.entity.system;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.annotation.query.SimpleCondition;
import me.wuwenbin.notepress.api.annotation.query.WrapperCondition;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

import javax.validation.constraints.NotEmpty;

/**
 * created by Wuwenbin on 2019/11/28 at 9:09 上午
 *
 * @author wuwenbin
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class Oauth extends BaseEntity<Oauth> {

    private Long id;
    @NotEmpty(message = "验证类型不能为空")
    @WrapperCondition(value = SimpleCondition.like)
    private String oauthType;
    @NotEmpty(message = "clientId 不能为空")
    private String clientId;
    @NotEmpty(message = "clientSecret 不能为空")
    private String clientSecret;
    private String redirectUri;
    private String extraParam;
}
