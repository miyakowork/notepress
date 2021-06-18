package me.wuwenbin.notepress.service.impl;

import cn.hutool.cache.Cache;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.query.SysUserQuery;
import me.wuwenbin.notepress.api.service.ISysUserService;
import me.wuwenbin.notepress.api.utils.NotePressCacheUtils;
import me.wuwenbin.notepress.api.utils.NotePressIpUtils;
import me.wuwenbin.notepress.service.mapper.ParamMapper;
import me.wuwenbin.notepress.service.mapper.SysUserMapper;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static me.wuwenbin.notepress.api.constants.ParamKeyConstant.*;

/**
 * created by Wuwenbin on 2019/11/22 at 11:21 上午
 *
 * @author wuwenbin
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private ParamMapper paramMapper;
    @Qualifier("passwordRetryCache")
    @Autowired
    private Cache<String, Integer> passwordRetryCache;

    /**
     * 初始化管理员账号
     *
     * @param user
     * @return
     */
    @Override
    public NotePressResult initAdministrator(SysUser user) {
        user.setAdmin(true);
        user.setStatus(true);
        user.setGmtCreate(LocalDateTime.now());
        user.remark("系统初始化设置管理员账号");
        int res = userMapper.insert(user);
        if (res == 1) {
            int statusRes = paramMapper.updateValueByName(SYSTEM_INIT_STATUS, 1);
            if (statusRes == 1) {
                NotePressCacheUtils.remove(SYSTEM_INIT_STATUS);
            }
            paramMapper.updateValueByName(ADMIN_GLOBAL_NICKNAME, user.getNickname());
            paramMapper.updateValueByName(ADMIN_GLOBAL_AVATAR, user.getAvatar());
            return NotePressResult.createOkData(user);
        }
        return NotePressResult.createErrorMsg("初始化管理员账号失败！");
    }

    /**
     * 执行用户登录
     *
     * @param usernameOrEmail
     * @param password
     * @param loginIp
     * @return
     */
    @Override
    public NotePressResult doLogin(String usernameOrEmail, String password, String loginIp) {
        //先阻止多次错误请求，防止脚本滥用
        Integer retryCount = passwordRetryCache.get(usernameOrEmail);
        //清洗上一次的，准备插入新的
        passwordRetryCache.remove(usernameOrEmail);
        if (retryCount == null) {
            retryCount = 0;
        }
        if (retryCount > 5) {
            return NotePressResult.createErrorMsg("错误次数过多，请10分钟后尝试");
        }
        String addr = NotePressIpUtils.getIpInfo(loginIp).getAddress();
        String md5Again = SecureUtil.md5(password);
        SysUser resUser;
        if (ReUtil.isMatch("^[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}$", usernameOrEmail)) {
            resUser = userMapper.selectOne(SysUserQuery.buildByEmail(usernameOrEmail, md5Again, true));
        } else {
            resUser = userMapper.selectOne(SysUserQuery.build(usernameOrEmail, md5Again, true));
        }
        if (resUser != null) {
            SysUser updateUser = SysUser.builder()
                    .lastLoginIp(loginIp).lastLoginAddr(addr).lastLoginTime(LocalDateTime.now()).build();
            //更新用户登录信息
            userMapper.update(updateUser, SysUserQuery.buildByUsername(usernameOrEmail));
            return NotePressResult.createOkData(resUser);
        }
        //数据库没有找到匹配的用户，说明输入有误，输入错误次数+1
        else {
            passwordRetryCache.put(usernameOrEmail, retryCount + 1);
            return NotePressResult.createErrorMsg("用户不存在或者密码错误或已被锁定");
        }
    }

    /**
     * 管理用户列表
     *
     * @param layuiTableQuery
     * @return
     */
    @Override
    public NotePressResult findUserList(IPage<SysUser> userPage, LayuiTableQuery<SysUser> layuiTableQuery) {
        return findLayuiTableList(userMapper, userPage, layuiTableQuery);
    }

    /**
     * 更新用户信息
     *
     * @param sysUser
     * @return
     */
    @Override
    public NotePressResult updateUserById(SysUser sysUser) {
        //不管什么情况都不能修改此字段，所以此处强行设置 false
        sysUser.setAdmin(false);
        sysUser.setUpdateBy(NotePressSessionUtils.getSessionUser().getId());
        sysUser.setGmtUpdate(LocalDateTime.now());
        int res = userMapper.updateById(sysUser);
        if (res > 0) {
            return NotePressResult.createOkMsg("修改用户信息成功！");
        }
        return NotePressResult.createErrorMsg("修改用户信息失败！");
    }

    /**
     * 管理员新增一个用户
     *
     * @param sysUser
     * @return
     */
    @Override
    public NotePressResult addUser(SysUser sysUser) {
        //填充新增用户其他信息
        sysUser.gmtCreate(LocalDateTime.now());
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        sysUser.createBy(sessionUser != null ? sessionUser.getId() : null);
        sysUser.remark(sessionUser != null ? sessionUser.getAdmin() ? "管理员新增用户" : "普通用户注册" : "游客注册用户");
        //不能添加管理员，管理员只有站长一位
        sysUser.setAdmin(false);
        sysUser.setStatus(true);
        //插入用户表中并且新增用户角色关系（也插入到对应的中间表中）
        int userInsertCnt = userMapper.insert(sysUser);
        if (userInsertCnt > 0) {
            return NotePressResult.createOk("管理员新增用户成功", sysUser);
        }
        return NotePressResult.createErrorMsg("管理员新增用户失败");
    }

    @Override
    public NotePressResult doReg(SysUser sysUser) {
        SysUser queryUser = SysUser.builder()
                .username(sysUser.getUsername())
                .email(sysUser.getEmail()).build();
        int cnt = userMapper.selectCount(SysUserQuery.build(queryUser));
        if (cnt > 0) {
            return NotePressResult.createErrorMsg("已存在用户！");
        }
        sysUser.setPassword(SecureUtil.md5(sysUser.getPassword()));
        return addUser(sysUser);
    }

    @Override
    public NotePressResult userUpdateInfo(String nickname, String pwd) {
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        if (sessionUser != null) {
            if (!sessionUser.getPassword().contentEquals(SecureUtil.md5(pwd))) {
                boolean res = this.update(
                        Wrappers.<SysUser>update()
                                .set(StrUtil.isNotEmpty(nickname), "nickname", nickname)
                                .set(StrUtil.isNotEmpty(pwd), "password", SecureUtil.md5(pwd))
                                .eq("id", sessionUser.getId()));
                if (res) {
                    return NotePressResult.createOkMsg("修改成功！");
                }
            }
        }
        return NotePressResult.createErrorMsg("修改未成功，请稍后重试！");
    }
}
