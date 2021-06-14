package me.wuwenbin.notepress.api.exception;

import lombok.Getter;

/**
 * created by Wuwenbin on 2019/11/14 at 1:22 下午
 *
 * @author wuwenbin
 */
public enum NotePressErrorCode {

    /**
     * 错误码枚举
     */
    InitError(-1, "初始化错误！"),
    IllegalRequestError(-2, "非法请求"),
    NotLogin(-3, "没有登录或登录过期"),
    BusyError(-4, "系统繁忙"),
    SettingError(-5, "配置出错"),
    DevError(-6, "开发错误"),
    NormalError(-7, "一般错误"),
    RequestError(-8, "请求错误"),
    CsrfError(-9, "CSRF错误"),
    TokenError(-10, "token异常"),
    QiniuError(-11, "七牛上传异常"),
    ControllerError(-12, "控制层处理异常"),
    PermissionDenied(403, "没有权限"),
    InternalServerError(500, "内部服务错误");


    @Getter
    private final int code;
    @Getter
    private final String message;

    NotePressErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static NotePressErrorCode getErrorCodeEnumByCode(int code) {
        for (NotePressErrorCode notePressErrorCode : NotePressErrorCode.values()) {
            if (code == notePressErrorCode.getCode()) {
                return notePressErrorCode;
            }
        }
        return null;
    }

}