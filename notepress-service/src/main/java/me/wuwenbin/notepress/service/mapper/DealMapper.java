package me.wuwenbin.notepress.service.mapper;

import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import me.wuwenbin.notepress.api.model.entity.Deal;
import me.wuwenbin.notepress.service.mapper.base.NotePressMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author wuwen
 */
@MybatisMapper
public interface DealMapper extends NotePressMapper<Deal> {

    /**
     * 查询用户硬币总计
     *
     * @param userId
     * @return
     */
    @Select("select sum(deal_amount) from np_deal where user_id = #{userId}")
    int selectSumCoinByIds(@Param("userId") Long userId);
}
