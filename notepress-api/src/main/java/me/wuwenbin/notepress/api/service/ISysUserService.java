package me.wuwenbin.notepress.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.base.INotePressService;

/**
 * created by Wuwenbin on 2019/11/22 at 11:18 上午
 *
 * @author wuwenbin
 */
public interface ISysUserService extends INotePressService<SysUser> {

    /**
     * 初始化管理员账号
     *
     * @param user
     * @return
     */
    NotePressResult initAdministrator(SysUser user);

    /**
     * 执行用户登录
     *
     * @param username
     * @param password
     * @param loginIp
     * @return
     */
    NotePressResult doLogin(String username, String password, String loginIp);

    /**
     * 管理用户列表
     *
     * @param userPage
     * @param layuiTableQuery
     * @return
     */
    NotePressResult findUserList(IPage<SysUser> userPage, LayuiTableQuery<SysUser> layuiTableQuery);

    /**
     * 更新用户信息
     *
     * @param sysUser
     * @return
     */
    NotePressResult updateUserById(SysUser sysUser);

    /**
     * 管理员新增一个用户
     *
     * @param sysUser
     * @return
     */
    NotePressResult addUser(SysUser sysUser);

    /**
     * 用户注册本站账号
     *
     * @param sysUser
     * @return
     */
    NotePressResult doReg(SysUser sysUser);

    /**
     * 用户更新信息
     *
     * @param nickname
     * @param pwd
     * @return
     */
    NotePressResult userUpdateInfo(String nickname, String pwd);
}
