package me.wuwenbin.notepress.api.service.base;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;
import me.wuwenbin.notepress.api.model.layui.LayuiTable;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.query.BaseQuery;
import me.wuwenbin.notepress.api.utils.NotePressLayuiTableUtils;
import org.springframework.util.CollectionUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author wuwen
 */
public interface INotePressService<T extends BaseEntity<T>> extends IService<T> {

    /**
     * 通用的查找l数据
     *
     * @param baseMapper
     * @param entityPage
     * @param layuiTableQuery
     * @return
     */
    default NotePressResult findLayuiTableList(BaseMapper<T> baseMapper, IPage<T> entityPage, LayuiTableQuery<T> layuiTableQuery) {
        NotePressLayuiTableUtils.startPage(entityPage, layuiTableQuery);
        if (CollectionUtils.isEmpty(layuiTableQuery.getSortOrders())) {
            entityPage = baseMapper.selectPage(entityPage, BaseQuery.build(layuiTableQuery.getExtra(), layuiTableQuery.getOrder(), layuiTableQuery.getSort()));
        } else {
            entityPage = baseMapper.selectPage(entityPage, BaseQuery.buildOrders(layuiTableQuery.getExtra(), layuiTableQuery.getSortOrders()));
        }
        return NotePressResult.createOkData(LayuiTable.success(entityPage));
    }

    /**
     * 通用的查找l数据，带额外的query条件
     *
     * @param baseMapper
     * @param entityPage
     * @param layuiTableQuery
     * @param extraQuery
     * @return
     */
    default NotePressResult findLayuiTableList(BaseMapper<T> baseMapper, IPage<T> entityPage, LayuiTableQuery<T> layuiTableQuery,
                                               Function<QueryWrapper<T>, QueryWrapper<T>> extraQuery) {
        NotePressLayuiTableUtils.startPage(entityPage, layuiTableQuery);
        if (CollectionUtils.isEmpty(layuiTableQuery.getSortOrders())) {
            QueryWrapper<T> query = BaseQuery.build(layuiTableQuery.getExtra(), layuiTableQuery.getOrder(), layuiTableQuery.getSort());
            query.and(extraQuery);
            entityPage = baseMapper.selectPage(entityPage, query);
        } else {
            QueryWrapper<T> query = BaseQuery.buildOrders(layuiTableQuery.getExtra(), layuiTableQuery.getSortOrders());
            query.and(extraQuery);
            entityPage = baseMapper.selectPage(entityPage, query);
        }
        return NotePressResult.createOkData(LayuiTable.success(entityPage));
    }

    /**
     * 通用方法,需要自定义分页查询操作的，请使用此方法
     *
     * @param entityPage
     * @param layuiTableQuery
     * @param function
     * @return
     */
    default NotePressResult findLayuiTableList(IPage<T> entityPage, LayuiTableQuery<T> layuiTableQuery, BiFunction<IPage<T>, LayuiTableQuery<T>, NotePressResult> function) {
        NotePressLayuiTableUtils.startPage(entityPage, layuiTableQuery);
        return function.apply(entityPage, layuiTableQuery);
    }
}
