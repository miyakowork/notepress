package me.wuwenbin.notepress.service.mapper;

import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import me.wuwenbin.notepress.api.model.entity.Content;
import me.wuwenbin.notepress.service.mapper.base.NotePressMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author wuwenbin
 */
@MybatisMapper
public interface ContentMapper extends NotePressMapper<Content> {

    /**
     * 统计内容的所有纯文本字数
     *
     * @return
     */
    @Select("select sum(if(text_content is null,0,length(trim(text_content)))) from np_content")
    Long sumContentWords();

    /**
     * 随机文章
     *
     * @param randomSize
     * @return
     */
    @Select("select * from np_content where history = 0 and visible = 1 ORDER BY rand()  LIMIT #{limit}")
    List<Content> randomContent(@Param("limit") int randomSize);

    /**
     * 刷新浏览量
     *
     * @param contentId
     * @return
     */
    @Update("update np_content set views = views + 1 where id = #{contentId}")
    int updateViews(@Param("contentId") String contentId);

    /**
     * 刷新点赞量
     *
     * @param contentId
     * @return
     */
    @Update("update np_content set approve_cnt = approve_cnt + 1 where id = #{contentId}")
    int updateApprove(@Param("contentId") String contentId);
}
