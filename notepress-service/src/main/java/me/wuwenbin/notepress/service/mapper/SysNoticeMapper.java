package me.wuwenbin.notepress.service.mapper;

import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import me.wuwenbin.notepress.api.model.entity.system.SysNotice;
import me.wuwenbin.notepress.service.mapper.base.NotePressMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author wuwen
 */
@MybatisMapper
public interface SysNoticeMapper extends NotePressMapper<SysNotice> {

    /**
     * 留言排行
     *
     * @return
     */
    @Select("SELECT n.user_id,u.avatar,u.nickname, count(n.user_id) as msg_cnt " +
            "FROM np_sys_notice n " +
            "LEFT JOIN np_sys_user u ON u.id = n.user_id " +
            "GROUP BY n.user_id ORDER BY msg_cnt DESC LIMIT 15")
    List<Map<String, Object>> findRankList();

    /**
     * 查询最近N天的排行
     *
     * @param limitDay 在最近 几天的排行
     * @return
     */
    @Select("select n.user_id,u.avatar,u.nickname,count(n.user_id) as msg_cnt from np_sys_notice n " +
            "left join np_sys_user u on u.id = n.user_id where DATE_FORMAT(n.gmt_create,'%Y-%m-%d')>= DATE_SUB(DATE_FORMAT(NOW(),'%Y-%m-%d'),INTERVAL #{day} DAY) " +
            "group by n.user_id order by msg_cnt desc limit 15")
    List<Map<String, Object>> findRankListByDate(@Param("day") int limitDay);

    /**
     * 查找谋篇文章评论最大的楼层
     *
     * @param contentId
     * @return
     */
    @Select("select floor from np_sys_notice where content_id = #{contentId} order by floor desc limit 1")
    Integer findMaxFloorByContentId(@Param("contentId") String contentId);
}
