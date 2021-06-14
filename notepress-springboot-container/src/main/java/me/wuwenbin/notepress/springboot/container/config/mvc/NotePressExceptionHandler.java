package me.wuwenbin.notepress.springboot.container.config.mvc;

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
@ControllerAdvice
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
}
