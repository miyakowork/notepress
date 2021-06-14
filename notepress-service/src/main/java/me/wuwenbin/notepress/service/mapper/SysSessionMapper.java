package me.wuwenbin.notepress.service.mapper;

import me.wuwenbin.notepress.api.annotation.MybatisMapper;
import me.wuwenbin.notepress.api.model.entity.system.SysSession;
import me.wuwenbin.notepress.service.mapper.base.NotePressMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

/**
 * @author wuwen
 */
@MybatisMapper
public interface SysSessionMapper extends NotePressMapper<SysSession> {

    /**
     * 删除admin jwtToken
     *
     * @param token
     * @return
     * @throws Exception
     */
    @Delete("delete from np_sys_session where jwt_token = #{token} and admin_req = 1")
    void deleteJwtToken(@Param("token") String token) throws Exception;

    /**
     * 删除过期的token
     *
     * @return
     * @throws Exception
     */
    @Delete("delete from np_sys_session where expired <= now() and admin_req = 1")
    void deleteExpiredJwtToken() throws Exception;

    /**
     * 删除用户session
     *
     * @param userId
     * @throws Exception
     */
    @Delete("delete from np_sys_session where session_user_id = #{userId} and admin_req = 0")
    void deleteBySessionUserId(@Param("userId") Long userId) throws Exception;
}
