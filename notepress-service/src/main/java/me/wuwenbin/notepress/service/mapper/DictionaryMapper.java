package me.wuwenbin.notepress.service.mapper;

import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import me.wuwenbin.notepress.api.model.entity.Dictionary;
import me.wuwenbin.notepress.service.mapper.base.NotePressMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author wuwen
 */
@MybatisMapper
public interface DictionaryMapper extends NotePressMapper<Dictionary> {

    /**
     * 前{@param size}个tag集合
     *
     * @param size
     * @param ids
     * @return
     */
    @Select({
            "<script>",
            "SELECT",
            "d.id, d.dict_value, count(r.refer_id) as cnt ",
            "from np_refer r left join np_dictionary d on d.id = r.refer_id ",
            "where r.refer_type = 'content_tag' ",
            "<if test='ids != null'>",
            "and d.id not in",
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>",
            "#{id}",
            "</foreach>",
            "</if>",
            "group by r.refer_id order by cnt desc limit #{size}",
            "</script>"
    })
    List<Map<String, Object>> topTagList(@Param("size") int size, @Param("ids") List<Long> ids);
}
