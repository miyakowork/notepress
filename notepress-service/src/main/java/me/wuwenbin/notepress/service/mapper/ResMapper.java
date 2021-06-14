package me.wuwenbin.notepress.service.mapper;

import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import me.wuwenbin.notepress.api.model.entity.Res;
import me.wuwenbin.notepress.service.mapper.base.NotePressMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wuwen
 */
@MybatisMapper
public interface ResMapper extends NotePressMapper<Res> {

    /**
     * 查询硬币总计
     *
     * @param ids
     * @return
     */
    @Select({
            "<script>",
            "SELECT sum(coin) FROM np_res where id in",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</script>"
    })
    int selectSumCoinByIds(@Param("ids") List<String> ids);

}
