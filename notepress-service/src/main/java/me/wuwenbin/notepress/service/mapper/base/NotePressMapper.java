package me.wuwenbin.notepress.service.mapper.base;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.wuwenbin.notepress.api.model.page.NotePressPage;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.NumberUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 参考 https://github.com/miyakowork/simple-spring-jdbc
 *
 * @author wuwen
 */
public interface NotePressMapper<T> extends BaseMapper<T> {

    Logger log = LoggerFactory.getLogger(NotePressMapper.class);
    JdbcTemplate JDBC_TEMPLATE = NotePressUtils.getBean(JdbcTemplate.class);
    NamedParameterJdbcTemplate NAMED_PARAMETER_JDBC_TEMPLATE = NotePressUtils.getBean(NamedParameterJdbcTemplate.class);

    /**
     * 组装count部分sql
     *
     * @param nativeSql
     * @return
     */
    default String getCountSql(String nativeSql) {
        final String countSql = "COUNT(0)";
        Assert.hasText(nativeSql, "sql is not correct!");
        String sql = nativeSql.toUpperCase();
        if (sql.contains("DISTINCT(") || sql.contains(" GROUP BY ")) {
            return "SELECT " + countSql + " FROM (" + nativeSql + ") TEMP_COUNT_TABLE";
        }
        String[] froms = sql.split(" FROM ");
        String tempSql = "";
        for (int i = 0; i < froms.length; i++) {
            if (i != froms.length - 1) {
                tempSql = tempSql.concat(froms[i] + " FROM ");
            } else {
                tempSql = tempSql.concat(froms[i]);
            }
            int left = tempSql.split("\\(").length;
            int right = tempSql.split("\\)").length;
            if (left == right) {
                break;
            }
        }
        tempSql = " FROM " + nativeSql.substring(tempSql.length(), sql.length());
        int orderBy = tempSql.toUpperCase().indexOf(" ORDER BY ");
        if (orderBy >= 0) {
            tempSql = tempSql.substring(0, orderBy);
        }
        return "SELECT " + countSql + " ".concat(tempSql);
    }

    /**
     * 组装limit部分sql
     *
     * @param sql
     * @param page
     * @return
     */
    default String getSqlOfMySql(final String sql, NotePressPage<?> page) {
        String querySql = sql;
        if (page.isFirstSetted() && page.isPageSizeSetted()) {
            querySql = querySql.concat(" LIMIT " + page.getFirst() + "," + page.getPageSize());
        }
        return querySql;
    }

    /**
     * 类参数转化
     *
     * @param clazz
     * @return
     */
    default BeanPropertyRowMapper<T> generateRowMapper(Class<T> clazz) {
        return BeanPropertyRowMapper.newInstance(clazz);
    }

    /**
     * 对象参数转化
     *
     * @param o
     * @return
     */
    default BeanPropertySqlParameterSource generateBeanSqlParamSource(Object o) {
        return new BeanPropertySqlParameterSource(o);
    }

    /**
     * 执行insert update delete
     *
     * @param sql
     * @param arrayParameters
     * @return
     * @throws Exception
     */
    default int executeArray(String sql, Object... arrayParameters) {
        Assert.hasText(sql, "sql语句不正确!");
        log.info("==> SQL:" + sql);
        log.info("==> SQL参数：[{}]", Arrays.toString(arrayParameters));
        int affectCount;
        if (arrayParameters != null && arrayParameters.length > 0) {
            affectCount = JDBC_TEMPLATE.update(sql, arrayParameters);
        } else {
            affectCount = JDBC_TEMPLATE.update(sql);
        }
        log.info("-- 响应条目：[{}]", affectCount);
        return affectCount;
    }


    /**
     * 执行insert update delete
     *
     * @param sql
     * @param mapParameter
     * @return
     * @throws Exception
     */
    default int executeMap(String sql, Map<String, Object> mapParameter) throws Exception {
        Assert.hasText(sql, "sql语句不正确!");
        int affectCount;
        if (mapParameter != null && mapParameter.size() > 0) {
            log.info("==> SQL:" + sql);
            log.info("==> SQL参数：[{}]", mapParameter);
            affectCount = NAMED_PARAMETER_JDBC_TEMPLATE.update(sql, mapParameter);
            log.info("==> 响应条目：[{}]", affectCount);
        } else {
            affectCount = executeArray(sql);
        }
        return affectCount;
    }

    /**
     * 执行insert update delete
     *
     * @param sql
     * @param beanParameter
     * @return
     * @throws Exception
     */
    default int executeBean(String sql, Object beanParameter) throws Exception {
        Assert.hasText(sql, "sql语句不正确!");
        int affectCount;
        if (beanParameter != null) {
            log.info("==> SQL:" + sql);
            log.info("==> SQL参数：[{}]", BeanUtil.beanToMap(beanParameter));
            affectCount = NAMED_PARAMETER_JDBC_TEMPLATE.update(sql, generateBeanSqlParamSource(beanParameter));
            log.info("==> 响应条目：[{}]", affectCount);
        } else {
            affectCount = executeArray(sql);
        }
        return affectCount;
    }

    /**
     * 查询单个数字结果
     *
     * @param sql
     * @param arrayParameters
     * @return
     */
    default Number findNumberByArray(String sql, Object... arrayParameters) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            log.info("SQL:" + sql);
            log.info("-- SQL参数：[{}]", Arrays.toString(arrayParameters));
            if (arrayParameters != null && arrayParameters.length > 0) {
                return JDBC_TEMPLATE.queryForObject(sql, Number.class, arrayParameters);
            } else {
                return JDBC_TEMPLATE.queryForObject(sql, Number.class);
            }
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return 0;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! ", e);
            return 0;
        }
    }

    /**
     * 查询指定类型数字类型单个结果
     *
     * @param sql
     * @param numberClass
     * @param arrayParameters
     * @param <S>
     * @return
     */
    default <S extends Number> S queryNumberByArray(String sql, Class<S> numberClass, Object... arrayParameters) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            log.info("==> SQL:" + sql);
            log.info("==> SQL参数：[{}]", Arrays.toString(arrayParameters));
            if (arrayParameters != null && arrayParameters.length > 0) {
                Number n = JDBC_TEMPLATE.queryForObject(sql, numberClass, arrayParameters);
                if (n == null) {
                    return NumberUtils.parseNumber("0", numberClass);
                }
                return (S) n;
            } else {
                Number n = JDBC_TEMPLATE.queryForObject(sql, numberClass);
                if (n == null) {
                    return NumberUtils.parseNumber("0", numberClass);
                } else {
                    return (S) n;
                }
            }
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return NumberUtils.parseNumber("0", numberClass);
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return NumberUtils.parseNumber("0", numberClass);
        }
    }

    /**
     * 查询数字结果
     *
     * @param sql
     * @param mapParameter
     * @return
     */
    default Number findNumberByMap(String sql, Map<String, Object> mapParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            if (mapParameter != null) {
                log.info("==> SQL:" + sql);
                log.info("==> SQL参数：[{}]", mapParameter);
                return NAMED_PARAMETER_JDBC_TEMPLATE.queryForObject(sql, mapParameter, Number.class);
            } else {
                return findNumberByArray(sql);
            }
        } catch (EmptyResultDataAccessException ere) {
            return 0;
        } catch (Exception e) {
            log.error("==> not result!", e);
            return 0;
        }
    }

    /**
     * 查询数字结果指定数字类型
     *
     * @param sql
     * @param numberClass
     * @param mapParameter
     * @param <S>
     * @return
     */
    default <S extends Number> S queryNumberByMap(String sql, Class<S> numberClass, Map<String, Object> mapParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            if (mapParameter != null && mapParameter.size() > 0) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", mapParameter);
                Number n = NAMED_PARAMETER_JDBC_TEMPLATE.queryForObject(sql, mapParameter, numberClass);
                if (n == null) {
                    return NumberUtils.parseNumber("0", numberClass);
                } else {
                    return (S) n;
                }
            } else {
                Number n = queryNumberByArray(sql, numberClass);
                if (n == null) {
                    return NumberUtils.parseNumber("0", numberClass);
                } else {
                    return (S) n;
                }
            }
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return NumberUtils.parseNumber("0", numberClass);
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return NumberUtils.parseNumber("0", numberClass);
        }
    }

    /**
     * 查询数字结果
     *
     * @param sql
     * @param beanParameter
     * @return
     */
    default Number findNumberByBean(String sql, Object beanParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            if (beanParameter != null) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", BeanUtil.beanToMap(beanParameter));
                return NAMED_PARAMETER_JDBC_TEMPLATE.queryForObject(sql, generateBeanSqlParamSource(beanParameter), Number.class);
            } else {
                return findNumberByArray(sql);
            }
        } catch (EmptyResultDataAccessException ere) {
            return 0;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return 0;
        }
    }

    /**
     * 查询数字结果，指定数字类型
     *
     * @param sql
     * @param numberClass
     * @param beanParameter
     * @param <S>
     * @return
     */
    default <S extends Number> S queryNumberByBean(String sql, Class<S> numberClass, Object beanParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            if (beanParameter != null) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", BeanUtil.beanToMap(beanParameter));
                Number n = NAMED_PARAMETER_JDBC_TEMPLATE.queryForObject(sql, generateBeanSqlParamSource(beanParameter), numberClass);
                if (n == null) {
                    return NumberUtils.parseNumber("0", numberClass);
                } else {
                    return (S) n;
                }
            } else {
                Number n = queryNumberByArray(sql, numberClass);
                if (n == null) {
                    return NumberUtils.parseNumber("0", numberClass);
                } else {
                    return (S) n;
                }
            }
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return NumberUtils.parseNumber("0", numberClass);
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return NumberUtils.parseNumber("0", numberClass);
        }
    }

    /**
     * 查询接本类型结果
     *
     * @param sql
     * @param objClass
     * @param arrayParameters
     * @param <S>
     * @return
     */
    default <S> S findPrimitiveByArray(String sql, Class<S> objClass, Object... arrayParameters) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            log.info("==> SQL: {}", sql);
            log.info("==> SQL参数：[{}]", Arrays.toString(arrayParameters));
            if (arrayParameters != null && arrayParameters.length > 0) {
                return JDBC_TEMPLATE.queryForObject(sql, objClass, arrayParameters);
            } else {
                return JDBC_TEMPLATE.queryForObject(sql, objClass);
            }
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询接本类型结果
     *
     * @param sql
     * @param objClass
     * @param mapParameter
     * @param <S>
     * @return
     */
    default <S> S findPrimitiveByMap(String sql, Class<S> objClass, Map<String, Object> mapParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            if (mapParameter != null && mapParameter.size() > 0) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", mapParameter);
                return NAMED_PARAMETER_JDBC_TEMPLATE.queryForObject(sql, mapParameter, objClass);
            } else {
                return findPrimitiveByArray(sql, objClass);
            }
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询接本类型结果
     *
     * @param sql
     * @param objClass
     * @param beanParameter
     * @param <S>
     * @return
     */
    default <S> S findPrimitiveByBean(String sql, Class<S> objClass, Object beanParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            if (beanParameter != null) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", BeanUtil.beanToMap(beanParameter));
                return NAMED_PARAMETER_JDBC_TEMPLATE.queryForObject(sql, generateBeanSqlParamSource(beanParameter), objClass);
            } else {
                return findPrimitiveByArray(sql, objClass);
            }
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询Map类型结果
     *
     * @param sql
     * @param arrayParameters
     * @return
     */
    default Map<String, Object> findMapByArray(String sql, Object... arrayParameters) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            log.info("==> SQL: {}", sql);
            log.info("==> SQL参数：[{}]", Arrays.toString(arrayParameters));
            if (arrayParameters != null && arrayParameters.length > 0) {
                return JDBC_TEMPLATE.queryForMap(sql, arrayParameters);
            } else {
                return JDBC_TEMPLATE.queryForMap(sql);
            }
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询Map类型结果
     *
     * @param sql
     * @param mapParameter
     * @return
     */
    default Map<String, Object> findMapByMap(String sql, Map<String, Object> mapParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            if (mapParameter != null && mapParameter.size() > 0) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", mapParameter);
                return NAMED_PARAMETER_JDBC_TEMPLATE.queryForMap(sql, mapParameter);
            } else {
                return findMapByArray(sql);
            }
        } catch (EmptyResultDataAccessException ere) {
            log.info("==> 查询SQL无结果" + ere);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询Map类型结果
     *
     * @param sql
     * @param beanParameter
     * @return
     */
    default Map<String, Object> findMapByBean(String sql, Object beanParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            if (beanParameter != null) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", BeanUtil.beanToMap(beanParameter));
                return NAMED_PARAMETER_JDBC_TEMPLATE.queryForMap(sql, generateBeanSqlParamSource(beanParameter));
            } else {
                return findMapByArray(sql);
            }
        } catch (EmptyResultDataAccessException ere) {
            log.info("==> 查询SQL无结果" + ere);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询javaBean类型结果
     *
     * @param sql
     * @param clazz
     * @param arrayParameters
     * @return
     */
    default T findBeanByArray(String sql, Class<T> clazz, Object... arrayParameters) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            Assert.notNull(clazz, "类集合中对象类型不能为空!");
            log.info("==> SQL: {}", sql);
            log.info("==> SQL参数：[{}]", Arrays.toString(arrayParameters));
            if (arrayParameters != null && arrayParameters.length > 0) {
                return JDBC_TEMPLATE.queryForObject(sql, generateRowMapper(clazz), arrayParameters);
            } else {
                return JDBC_TEMPLATE.queryForObject(sql, generateRowMapper(clazz));
            }
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }


    /**
     * 查询javaBean类型结果
     *
     * @param sql
     * @param clazz
     * @param mapParameter
     * @return
     */
    default T findBeanByMap(String sql, Class<T> clazz, Map<String, Object> mapParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            Assert.notNull(clazz, "集合中对象类型不能为空!");
            if (mapParameter != null && mapParameter.size() > 0) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", mapParameter);
                return NAMED_PARAMETER_JDBC_TEMPLATE.queryForObject(sql, mapParameter, generateRowMapper(clazz));
            } else {
                return findBeanByArray(sql, clazz);
            }
        } catch (EmptyResultDataAccessException ere) {
            log.info("==> 查询SQL无结果" + ere);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询javaBean类型结果
     *
     * @param sql
     * @param clazz
     * @param beanParameter
     * @return
     */
    default T findBeanByBean(String sql, Class<T> clazz, Object beanParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确！");
            Assert.notNull(clazz, "集合中对象类型不能为空！");
            if (beanParameter != null) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", BeanUtil.beanToMap(beanParameter));
                return NAMED_PARAMETER_JDBC_TEMPLATE.queryForObject(sql, generateBeanSqlParamSource(beanParameter), generateRowMapper(clazz));
            } else {
                return findBeanByArray(sql, clazz);
            }
        } catch (EmptyResultDataAccessException ere) {
            log.info("==> 查询SQL无结果" + ere);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询基本类型列表结果
     *
     * @param sql
     * @param objClass
     * @param arrayParameters
     * @param <R>
     * @return
     */
    default <R> List<R> findListPrimitiveByArray(String sql, Class<R> objClass, Object... arrayParameters) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            log.info("==> SQL: {}", sql);
            log.info("==> SQL参数：[{}]", Arrays.toString(arrayParameters));
            List<R> list;
            if (arrayParameters != null && arrayParameters.length > 0) {
                list = JDBC_TEMPLATE.queryForList(sql, objClass, arrayParameters);
            } else {
                list = JDBC_TEMPLATE.queryForList(sql, objClass);
            }
            log.info("==> 响应条目：[{}]", list.size());
            return list;
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询基本类型列表结果
     *
     * @param sql
     * @param objClass
     * @param mapParameter
     * @param <R>
     * @return
     */
    default <R> List<R> findListPrimitiveByMap(String sql, Class<R> objClass, Map<String, Object> mapParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            List<R> list;
            if (mapParameter != null && mapParameter.size() > 0) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", mapParameter);
                list = NAMED_PARAMETER_JDBC_TEMPLATE.queryForList(sql, mapParameter, objClass);
                log.info("==> 响应条目：[{}]", list.size());
            } else {
                list = findListPrimitiveByArray(sql, objClass);
            }
            return list;
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询基本类型列表结果
     *
     * @param sql
     * @param objClass
     * @param beanParameter
     * @param <R>
     * @return
     */
    default <R> List<R> findListPrimitiveByBean(String sql, Class<R> objClass, Object beanParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            List<R> list;
            if (beanParameter != null) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", BeanUtil.beanToMap(beanParameter));
                list = NAMED_PARAMETER_JDBC_TEMPLATE.queryForList(sql, generateBeanSqlParamSource(beanParameter), objClass);
                log.info("==> 响应条目：[{}]", list.size());
            } else {
                list = findListPrimitiveByArray(sql, objClass);
            }
            return list;
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询Map列表结果
     *
     * @param sql
     * @param arrayParameters
     * @return
     */
    default List<Map<String, Object>> findListMapByArray(String sql, Object... arrayParameters) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            log.info("==> SQL: {}", sql);
            log.info("==> SQL参数：[{}]", Arrays.toString(arrayParameters));
            List<Map<String, Object>> list;
            if (arrayParameters != null && arrayParameters.length > 0) {
                list = JDBC_TEMPLATE.queryForList(sql, arrayParameters);
            } else {
                list = JDBC_TEMPLATE.queryForList(sql);
            }
            log.info("==> 响应条目：[{}]", list.size());
            return list;
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询Map列表结果
     *
     * @param sql
     * @param mapParameter
     * @return
     */
    default List<Map<String, Object>> findListMapByMap(String sql, Map<String, Object> mapParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            List<Map<String, Object>> list;
            if (mapParameter != null && mapParameter.size() > 0) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", mapParameter);
                list = NAMED_PARAMETER_JDBC_TEMPLATE.queryForList(sql, mapParameter);
                log.info("==> 响应条目：[{}]", list.size());
            } else {
                list = findListMapByArray(sql);
            }
            return list;
        } catch (EmptyResultDataAccessException ere) {
            log.info("==> 查询SQL无结果" + ere);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询Map列表结果
     *
     * @param sql
     * @param beanParameter
     * @return
     */
    default List<Map<String, Object>> findListMapByBean(String sql, Object beanParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            List<Map<String, Object>> list;
            if (beanParameter != null) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", BeanUtil.beanToMap(beanParameter));
                list = NAMED_PARAMETER_JDBC_TEMPLATE.queryForList(sql, generateBeanSqlParamSource(beanParameter));
                log.info("==> 响应条目：[{}]", list.size());
            } else {
                list = findListMapByArray(sql);
            }
            return list;
        } catch (EmptyResultDataAccessException ere) {
            log.info("==> 查询SQL无结果" + ere);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询javaBean列表结果
     *
     * @param sql
     * @param clazz
     * @param arrayParameters
     * @return
     */
    default List<T> findListBeanByArray(String sql, Class<T> clazz, Object... arrayParameters) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            Assert.notNull(clazz, "集合中对象类型不能为空!");
            log.info("==> SQL: {}", sql);
            log.info("==> SQL参数：[{}]", Arrays.toString(arrayParameters));
            List<T> list;
            if (arrayParameters != null && arrayParameters.length > 0) {
                list = JDBC_TEMPLATE.query(sql, generateRowMapper(clazz), arrayParameters);
            } else {
                list = JDBC_TEMPLATE.query(sql, generateRowMapper(clazz));
            }
            log.info("==> 响应条目：[{}]", list.size());
            return list;
        } catch (EmptyResultDataAccessException e) {
            log.info("==> 查询SQL无结果" + e);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询javaBean列表结果
     *
     * @param sql
     * @param clazz
     * @param mapParameter
     * @return
     */
    default List<T> findListBeanByMap(String sql, Class<T> clazz, Map<String, Object> mapParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            Assert.notNull(clazz, "集合中对象类型不能为空!");
            List<T> list;
            if (mapParameter != null && mapParameter.size() > 0) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", mapParameter);
                list = NAMED_PARAMETER_JDBC_TEMPLATE.query(sql, mapParameter, generateRowMapper(clazz));
                log.info("==> 响应条目：[{}]", list.size());
            } else {
                list = findListBeanByArray(sql, clazz);
            }
            return list;
        } catch (EmptyResultDataAccessException ere) {
            log.info("==> 查询SQL无结果" + ere);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询javaBean列表结果
     *
     * @param sql
     * @param clazz
     * @param beanParameter
     * @return
     */
    default List<T> findListBeanByBean(String sql, Class<T> clazz, Object beanParameter) {
        try {
            Assert.hasText(sql, "sql语句不正确!");
            Assert.notNull(clazz, "集合中对象类型不能为空!");
            List<T> list;
            if (beanParameter != null) {
                log.info("==> SQL: {}", sql);
                log.info("==> SQL参数：[{}]", BeanUtil.beanToMap(beanParameter));
                list = NAMED_PARAMETER_JDBC_TEMPLATE.query(sql, generateBeanSqlParamSource(beanParameter), generateRowMapper(clazz));
                log.info("==> 响应条目：[{}]", list.size());
            } else {
                list = findListBeanByArray(sql, clazz);
            }
            return list;
        } catch (EmptyResultDataAccessException ere) {
            log.info("==> 查询SQL无结果" + ere);
            return null;
        } catch (Exception e) {
            log.error("==> 查询SQL异常 no result! " + e);
            return null;
        }
    }

    /**
     * 查询分页列表结果
     *
     * @param sql
     * @param page
     * @param arrayParameters
     * @return
     */
    default NotePressPage<Map<String, Object>> findPageListMapByArray(String sql, NotePressPage<Map<String, Object>> page, Object... arrayParameters) {
        Assert.notNull(page, "==> 分页信息不能为空");
        Assert.hasText(sql, "==> sql语句不正确!");
        long count;
        if (page.isAutoCount()) {
            count = queryNumberByArray(getCountSql(sql), Long.class, arrayParameters);
            page.setTotalCount((int) count);
        }
        List<Map<String, Object>> list = findListMapByArray(getSqlOfMySql(sql, page), arrayParameters);
        page.setRawResult(list);
        return page;
    }

    /**
     * 查询分页列表结果
     *
     * @param sql
     * @param page
     * @param mapParameter
     * @return
     */
    default NotePressPage<Map<String, Object>> findPageListMapByMap(String sql, NotePressPage<Map<String, Object>> page, Map<String, Object> mapParameter) {
        Assert.notNull(page, "==> 分页信息不能为空");
        Assert.hasText(sql, "==> sql语句不正确!");
        long count;
        if (page.isAutoCount()) {
            count = queryNumberByMap(getCountSql(sql), Long.class, mapParameter);
            page.setTotalCount((int) count);
        }
        List<Map<String, Object>> list = findListMapByMap(getSqlOfMySql(sql, page), mapParameter);
        page.setRawResult(list);
        return page;
    }

    /**
     * 查询分页列表结果
     *
     * @param sql
     * @param clazz
     * @param page
     * @param arrayParameters
     * @return
     */
    default NotePressPage<T> findPageListBeanByArray(String sql, Class<T> clazz, NotePressPage<T> page, Object... arrayParameters) {
        Assert.notNull(page, "分页信息不能为空");
        Assert.hasText(sql, "sql语句不正确!");
        long count;
        if (page.isAutoCount()) {
            count = queryNumberByArray(getCountSql(sql), Long.class, arrayParameters);
            page.setTotalCount((int) count);
        }
        List<T> list = findListBeanByArray(getSqlOfMySql(sql, page), clazz, arrayParameters);
        page.setTResult(list);
        return page;
    }

    /**
     * 查询分页列表结果
     *
     * @param sql
     * @param clazz
     * @param page
     * @param mapParameter
     * @return
     */
    default NotePressPage<T> findPageListBeanByMap(String sql, Class<T> clazz, NotePressPage<T> page, Map<String, Object> mapParameter) {
        Assert.notNull(page, "分页信息不能为空");
        Assert.hasText(sql, "sql语句不正确!");
        long count;
        if (page.isAutoCount()) {
            count = queryNumberByMap(getCountSql(sql), Long.class, mapParameter);
            page.setTotalCount((int) count);
        }
        List<T> list = findListBeanByMap(getSqlOfMySql(sql, page), clazz, mapParameter);
        page.setTResult(list);
        return page;
    }

    /**
     * 查询分页列表结果
     *
     * @param sql
     * @param clazz
     * @param page
     * @param beanParameter
     * @return
     */
    default NotePressPage<T> findPageListBeanByBean(String sql, Class<T> clazz, NotePressPage<T> page, Object beanParameter) {
        Assert.notNull(page, "分页信息不能为空");
        Assert.hasText(sql, "sql语句不正确!");
        long count;
        if (page.isAutoCount()) {
            count = queryNumberByBean(getCountSql(sql), Long.class, beanParameter);
            page.setTotalCount((int) count);
        }
        List<T> list = findListBeanByBean(getSqlOfMySql(sql, page), clazz, beanParameter);
        page.setTResult(list);
        return page;
    }

}
