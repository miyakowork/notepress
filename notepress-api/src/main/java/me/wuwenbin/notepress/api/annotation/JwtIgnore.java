package me.wuwenbin.notepress.api.annotation;

import java.lang.annotation.*;

/**
 * 表明是Mybatis的Mapper
 * created by Wuwenbin on 2018/7/20 at 15:12
 *
 * @author wuwenbin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface JwtIgnore {

}
