package me.wuwenbin.notepress.springboot.container.config.mvc;

import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.NotePressResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 此处的拦截只是针对controller层的
 *
 * @author wuwen
 */
@Slf4j
@ControllerAdvice(basePackages = "me.wuwenbin.notepress.web.controllers")
public class NotePressExceptionHandler {

    @ExceptionHandler(NotePressException.class)
    @ResponseBody
    public NotePressResult handle(NotePressException e) {
        NotePressResult result = e.getErrorResult();
        if (result != null) {
            return result;
        }
        return NotePressResult.createError(e);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public NotePressResult handle(Exception e) {
        e.printStackTrace();
        log.error("内部服务错误：{}", e.getMessage());
        return NotePressResult.createErrorMsg("内部服务异常");
    }
}
