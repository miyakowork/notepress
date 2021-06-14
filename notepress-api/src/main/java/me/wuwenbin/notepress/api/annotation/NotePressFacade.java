package me.wuwenbin.notepress.api.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 无关数据库查询的Service，使用此标记
 * created by Wuwenbin on 2018/7/20 at 15:12
 *
 * @author wuwenbin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Component
public @interface NotePressFacade {

    String value() default "";
}
