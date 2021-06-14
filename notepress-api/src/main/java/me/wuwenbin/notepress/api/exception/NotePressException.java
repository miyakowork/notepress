package me.wuwenbin.notepress.api.exception;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.model.NotePressResult;

/**
 * created by Wuwenbin on 2019/11/28 at 3:15 下午
 *
 * @author wuwenbin
 */
@Slf4j
@Getter
@Setter
public class NotePressException extends RuntimeException {

    private int errorCode;
    private NotePressResult errorResult;

    public NotePressException(NotePressErrorCode notePressErrorCode, String message) {
        this(notePressErrorCode.getCode(), message);
        this.errorCode = notePressErrorCode.getCode();
        this.errorResult = NotePressResult.createError(notePressErrorCode, message);
    }

    public NotePressException(String message) {
        this(NotePressErrorCode.NormalError, message);
        this.errorResult = NotePressResult.createError(NotePressErrorCode.NormalError, message);
    }

    public NotePressException(NotePressErrorCode notePressErrorCode) {
        this(notePressErrorCode.getCode(), notePressErrorCode.getMessage());
        this.errorResult = NotePressResult.createError(notePressErrorCode, notePressErrorCode.getMessage());
    }

    public NotePressException(RuntimeException e) {
        this(e.getMessage());
    }

    public NotePressException(NotePressResult errorResult, NotePressErrorCode notePressErrorCode, String message) {
        super(message);
        this.errorCode = notePressErrorCode.getCode();
        this.errorResult = errorResult;
    }

    public NotePressException(NotePressResult errorResult, NotePressErrorCode notePressErrorCode) {
        super(notePressErrorCode.getMessage());
        this.errorCode = notePressErrorCode.getCode();
        this.errorResult = errorResult;
    }


    /**
     * 提供错误码与错误信息
     */
    public NotePressException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }


    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    @Override
    public String toString() {
        if (errorResult != null) {
            return StrUtil.format("###### 异常信息###### ==> [{}]", errorResult.getMsg());
        }
        String logInfo = this.getMessage();
        if (StrUtil.isEmpty(logInfo)) {
            logInfo = this.getLocalizedMessage();
        }
        return StrUtil.isEmpty(logInfo) ? "NotePress 运行异常" : StrUtil.format("###### 异常信息###### ==> [{}]", logInfo);
    }
}
