package me.wuwenbin.notepress.api.query;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.annotation.query.SimpleCondition;
import me.wuwenbin.notepress.api.annotation.query.WrapperCondition;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;
import me.wuwenbin.notepress.api.model.page.SortOrder;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 通用组装查询对象
 *
 * @author wuwen
 */
@Slf4j
public class BaseQuery {

    /**
     * 根据bean的参数自动生成相关的query
     *
     * @param entity
     * @param isEmptyQuery
     * @param <T>
     * @param <S>
     * @return
     */
    public static <T extends BaseEntity<T>, S> QueryWrapper<T> build(S entity, boolean isEmptyQuery) {
        if (entity != null) {
            //noinspection rawtypes
            Class clazz = entity.getClass();
            QueryWrapper<T> queryWrapper = Wrappers.query();
            Field[] fields = clazz.getDeclaredFields();
            Field[] superFiles = clazz.getSuperclass().getDeclaredFields();
            Field[] allFields = ArrayUtil.addAll(fields, superFiles);

            for (Field field : allFields) {
                PropertyDescriptor pd;
                try {
                    pd = new PropertyDescriptor(field.getName(), clazz);
                } catch (IntrospectionException e) {
                    throw new NotePressException(NotePressErrorCode.DevError, "反射实体类字段出错，" + e.getMessage());
                }
                Method getMethod = pd.getReadMethod();
                if (getMethod != null) {
                    try {
                        if (getMethod.invoke(entity) != null) {
                            log.info("字段 ==> {}，类型是：{}，取到的值是：{}", field.getName(), field.getType(), getMethod.invoke(entity));
                            WrapperCondition wrapperCondition = field.getAnnotation(WrapperCondition.class);
                            String fieldName = StrUtil.toUnderlineCase(field.getName());
                            Object fieldValue = getMethod.invoke(entity);
                            if (ObjectUtil.isNotEmpty(fieldValue)) {
                                if (wrapperCondition != null) {
                                    SimpleCondition sc = wrapperCondition.value();
                                    switch (sc) {
                                        case ne:
                                            queryWrapper.ne(fieldName, fieldValue);
                                            break;
                                        case gt:
                                            queryWrapper.gt(fieldName, fieldValue);
                                            break;
                                        case gte:
                                            queryWrapper.ge(fieldName, fieldValue);
                                            break;
                                        case lt:
                                            queryWrapper.lt(fieldName, fieldValue);
                                            break;
                                        case lte:
                                            queryWrapper.le(fieldName, fieldValue);
                                            break;
                                        case like:
                                            queryWrapper.like(fieldName, fieldValue);
                                            break;
                                        case noteLike:
                                            queryWrapper.notLike(fieldName, fieldValue);
                                            break;
                                        case likeLeft:
                                            queryWrapper.likeLeft(fieldName, fieldValue);
                                            break;
                                        case likeRight:
                                            queryWrapper.likeRight(fieldName, fieldValue);
                                            break;
                                        case isNull:
                                            queryWrapper.isNull(fieldName);
                                            break;
                                        case isNotNull:
                                            queryWrapper.isNotNull(fieldName);
                                            break;
                                        default:
                                            queryWrapper.eq(fieldName, fieldValue);
                                            break;
                                    }
                                } else {
                                    queryWrapper.eq(fieldName, fieldValue);
                                }
                            }
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new NotePressException(NotePressErrorCode.DevError,
                                "反射实体类执行对应get方法出错，字段 ==> " + field.getName() + "，类型是：" + field.getType() + "，错误信息 => " + e.getMessage());
                    }
                }

            }
            return queryWrapper;
        } else {
            if (isEmptyQuery) {
                return Wrappers.emptyWrapper();
            } else {
                return Wrappers.query();
            }
        }
    }

    /**
     * 默认构造为空的
     *
     * @param entity
     * @param <T>
     * @param <S>
     * @return
     */
    public static <T extends BaseEntity<T>, S> QueryWrapper<T> build(S entity) {
        return build(entity, true);
    }

    /**
     * 目前再支持一种单列的排序自动拼接功能
     * 多列多排序后续增加
     *
     * @param entity
     * @param order
     * @param sort
     * @param <T>
     * @param <S>
     * @return
     */
    public static <T extends BaseEntity<T>, S> QueryWrapper<T> build(S entity, String order, String sort) {
        QueryWrapper<T> queryWrapper = build(entity, false);
        if (StrUtil.isNotEmpty(sort)) {
            sort = StrUtil.toUnderlineCase(sort);
            if ("desc".equalsIgnoreCase(order)) {
                return queryWrapper.orderByDesc(sort);
            } else {
                return queryWrapper.orderByAsc(sort);
            }
        }
        return queryWrapper;
    }

    /**
     * 多列排序
     *
     * @param entity
     * @param sortOrders
     * @param <T>
     * @param <S>
     * @return
     */
    public static <T extends BaseEntity<T>, S> QueryWrapper<T> buildOrders(S entity, List<SortOrder> sortOrders) {
        QueryWrapper<T> queryWrapper = build(entity, false);
        for (SortOrder sortOrder : sortOrders) {
            String sort = sortOrder.getSort();
            String order = sortOrder.getOrder();
            if (StrUtil.isNotEmpty(sort)) {
                sort = StrUtil.toUnderlineCase(sort);
                if ("desc".equalsIgnoreCase(order)) {
                    queryWrapper.orderByDesc(sort);
                } else {
                    queryWrapper.orderByAsc(sort);
                }
            }
        }
        return queryWrapper;
    }

    /**
     * 一个字段的通用查询
     *
     * @param name
     * @param value
     * @param <T>
     * @return
     */
    public static <T extends BaseEntity<T>> QueryWrapper<T> build(String name, Object value) {
        return Wrappers.<T>query().eq(name, value);
    }

    public static <T extends BaseEntity<T>> QueryWrapper<T> build(boolean condition, String name, Object value) {
        return Wrappers.<T>query().eq(condition, name, value);
    }

    public static <T extends BaseEntity<T>> QueryWrapper<T> buildNotEmpty(String name, Object value) {
        return build(ObjectUtil.isNotEmpty(value), name, value);
    }

    /**
     * 多个字段的通用查询
     *
     * @param nameValueArray 参数形式为：String,Object,String,Object.....如此
     * @param <T>
     * @return
     */
    public static <T extends BaseEntity<T>> QueryWrapper<T> build(Object... nameValueArray) {
        return buildNotEmptyOrNotNull(false, false, nameValueArray);
    }

    /**
     * 通用多个字段查询
     * 优先级：notEmpty>notNull
     *
     * @param notEmpty
     * @param notNull
     * @param nameValueArray
     * @param <T>
     * @return
     */
    public static <T extends BaseEntity<T>> QueryWrapper<T> buildNotEmptyOrNotNull(boolean notEmpty, boolean notNull, Object... nameValueArray) {
        if (!ObjectUtil.isEmpty(nameValueArray)) {
            if (nameValueArray.length % 2 == 0) {
                QueryWrapper<T> queryWrapper = Wrappers.query();
                for (int i = 0; i < nameValueArray.length; i = i + 2) {
                    if (notEmpty) {
                        queryWrapper.eq(ObjectUtil.isNotEmpty(nameValueArray[i + 1]), String.valueOf(nameValueArray[i]), nameValueArray[i + 1]);
                    } else if (notNull) {
                        queryWrapper.eq(ObjectUtil.isNotNull(nameValueArray[i + 1]), String.valueOf(nameValueArray[i]), nameValueArray[i + 1]);
                    } else {
                        queryWrapper.eq(String.valueOf(nameValueArray[i]), nameValueArray[i + 1]);
                    }
                }
                return queryWrapper;
            }
            throw new NotePressException(StrUtil.format("输入的参数：{}有误，请查证！", Arrays.toString(nameValueArray)));
        }
        return Wrappers.emptyWrapper();
    }
}
