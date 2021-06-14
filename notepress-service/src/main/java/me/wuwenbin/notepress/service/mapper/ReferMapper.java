package me.wuwenbin.notepress.service.mapper;

import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.entity.Refer;
import me.wuwenbin.notepress.service.mapper.base.NotePressMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * created by Wuwenbin on 2019/11/28 at 2:16 下午
 *
 * @author wuwenbin
 */
@MybatisMapper
public interface ReferMapper extends NotePressMapper<Refer> {

    /**
     * 插入第三方uuid和本站账号的对应关系
     *
     * @param uuid
     * @param userId
     * @param jsonExtra
     * @param gmtCreate
     * @param optUserId
     * @return
     */
    @Insert("insert into np_refer(self_id,refer_id,refer_type,refer_extra,gmt_create,create_by) " +
            "values(#{uuid},#{userId},'third_user',#{jsonExtra},#{gmtCreate},#{optUserId})")
    int insertThirdUser(@Param("uuid") String uuid, @Param("userId") String userId,
                        @Param("jsonExtra") String jsonExtra, @Param("gmtCreate") LocalDateTime gmtCreate, @Param("optUserId") Long optUserId);

    /**
     * 插入内容和分类对应关系
     *
     * @param contentId
     * @param categoryId
     * @param gmtCreate
     * @param optUserId
     */
    @Insert("insert into np_refer(self_id,refer_id,refer_type,gmt_create,create_by) " +
            "values(#{contentId},#{categoryId},'content_category',#{gmtCreate},#{optUserId})")
    void insertContentCategory(@Param("contentId") String contentId, @Param("categoryId") String categoryId,
                               @Param("gmtCreate") LocalDateTime gmtCreate, @Param("optUserId") Long optUserId);

    /**
     * 插入内容和分TAG对应关系
     *
     * @param contentId
     * @param tagId
     * @param gmtCreate
     * @param optUserId
     */
    @Insert("insert into np_refer(self_id,refer_id,refer_type,gmt_create,create_by) " +
            "values(#{contentId},#{tagId},'content_tag',#{gmtCreate},#{optUserId})")
    void insertContentTag(@Param("contentId") String contentId, @Param("tagId") String tagId,
                          @Param("gmtCreate") LocalDateTime gmtCreate, @Param("optUserId") Long optUserId);


    /**
     * 根据uuid和source获取refer映射关系
     *
     * @param source
     * @param uuid
     * @return
     */
    @Select("select * from np_refer where refer_type = 'third_user' and json_extract(refer_extra,'$.source') = #{source} and self_id = #{uuid};")
    Refer findUserReferBySourceAndUuid(@Param("source") String source, @Param("uuid") String uuid);


    /**
     * 根据类型和参考id查询对应的目标id
     * @param referId
     * @param referTypeEnum
     * @return
     */
    @Select("select * from np_refer where refer_id = #{referId} and refer_type = #{referType}")
    List<Refer> findRefersByTypeAndReferId(@Param("referId") String referId, @Param("referType") ReferTypeEnum referTypeEnum);

    /**
     * 根据类型和参考id查询对应的目标id
     * @param selfId
     * @param referTypeEnum
     * @return
     */
    @Select("select * from np_refer where self_id = #{selfId} and refer_type = #{referType}")
    List<Refer> findRefersByTypeAndSelfId(@Param("selfId") String selfId, @Param("referType") ReferTypeEnum referTypeEnum);

}
