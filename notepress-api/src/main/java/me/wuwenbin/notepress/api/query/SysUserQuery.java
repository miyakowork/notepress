package me.wuwenbin.notepress.api.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 常用查询条件拼装
 * created by Wuwenbin on 2019/11/27 at 3:25 下午
 *
 * @author wuwenbin
 */
public class SysUserQuery extends BaseQuery {

    public static QueryWrapper<SysUser> build(String username, String password, boolean status) {
        return Wrappers.<SysUser>query().eq("username", username).eq("password", password).eq("status", status);
    }

    public static QueryWrapper<SysUser> buildByEmail(String email, String password, boolean status) {
        return Wrappers.<SysUser>query().eq("email", email).eq("password", password).eq("status", status);
    }

    public static UpdateWrapper<SysUser> buildUpdate(String columnLabel, String columnValue) {
        return Wrappers.<SysUser>update().set(columnLabel, columnValue);
    }

    public static QueryWrapper<SysUser> buildByAdmin(boolean admin) {
        return Wrappers.<SysUser>query().eq("admin", admin);
    }

    public static QueryWrapper<SysUser> buildTodayCount() {
        String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return Wrappers.<SysUser>query().like("gmt_create", nowDate).eq("admin", false);
    }

    public static UpdateWrapper<SysUser> buildByUsername(String username) {
        return Wrappers.<SysUser>update().eq("username", username);
    }
}
