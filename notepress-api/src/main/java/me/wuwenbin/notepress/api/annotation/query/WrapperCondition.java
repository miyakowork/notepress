package me.wuwenbin.notepress.api.annotation.query;

import java.lang.annotation.*;

/**
 * 表明是Mybatis的Mapper
 * created by Wuwenbin on 2018/7/20 at 15:12
 *
 * @author wuwenbin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface WrapperCondition {

    /**
     * 是否为简单查询条件
     * 即是否都在 SimpleCondition 类中
     * 默认是简单查询
     * 为以后扩展复杂查询留下接口
     *
     * @return
     */
    boolean isSimple() default true;

    /**
     * 默认的查询条件是简单查询的等于查询
     *
     * @return
     */
    SimpleCondition value() default SimpleCondition.eq;

}
