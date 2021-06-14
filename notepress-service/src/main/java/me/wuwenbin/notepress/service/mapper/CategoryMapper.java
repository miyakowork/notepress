package me.wuwenbin.notepress.service.mapper;

import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import me.wuwenbin.notepress.api.model.entity.Category;
import me.wuwenbin.notepress.service.mapper.base.NotePressMapper;

/**
 * @author wuwenbin
 */
@MybatisMapper
public interface CategoryMapper extends NotePressMapper<Category> {
}
