package me.wuwenbin.notepress.web.controllers.api.admin;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.service.ISysUserService;
import me.wuwenbin.notepress.api.utils.NotePressCacheUtils;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author wuwen
 */
@RestController
@RequestMapping("/admin/profile")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AdminProfileController extends NotePressBaseController {

    private final ISysUserService sysUserService;
    private final IParamService paramService;

    @PostMapping("/update")
    public NotePressResult updateAdminProfile(@NotNull SysUser user, String oldPass, String alipay, String wechatPay) {
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        if (sessionUser != null) {

            if (StrUtil.isEmpty(oldPass)) {
                return writeJsonErrorMsg("旧密码不能为空！");
            }

            if (StrUtil.isNotEmpty(user.getPassword())) {
                user.setPassword(SecureUtil.md5(user.getPassword()));
                if (!SecureUtil.md5(oldPass).equals(sessionUser.getPassword())) {
                    return writeJsonErrorMsg("旧密码错误！");
                }
            }

            user.setId(sessionUser.getId());
            user.setGmtUpdate(LocalDateTime.now());
            boolean res = sysUserService.updateById(user);
            if (res) {
                if (StrUtil.isNotEmpty(user.getNickname())) {
                    paramService.update(ParamQuery.buildUpdate(ParamKeyConstant.ADMIN_GLOBAL_NICKNAME, user.getNickname()));
                }
                if (StrUtil.isNotEmpty(user.getAvatar())) {
                    paramService.update(ParamQuery.buildUpdate(ParamKeyConstant.ADMIN_GLOBAL_AVATAR, user.getAvatar()));
                }
                if (StrUtil.isNotEmpty(alipay)) {
                    boolean r1 = paramService.update(ParamQuery.buildUpdate(ParamKeyConstant.ADMIN_QRCODE_ALIPAY, alipay));
                    ifTrueDo(r1, NotePressCacheUtils::remove, ParamKeyConstant.ADMIN_QRCODE_ALIPAY);
                }
                if (StrUtil.isNotEmpty(wechatPay)) {
                    boolean r2 = paramService.update(ParamQuery.buildUpdate(ParamKeyConstant.ADMIN_QRCODE_WECHAT, wechatPay));
                    ifTrueDo(r2, NotePressCacheUtils::remove, ParamKeyConstant.ADMIN_QRCODE_WECHAT);
                }
                NotePressSessionUtils.invalidSessionUser();
                return writeJsonOkMsg("修改成功，请重新登录！");
            }
            return writeJsonErrorMsg("修改失败，未成功修改任何信息！");
        } else {
            return writeJsonErrorMsg("非正常请求或请求出现异常！");
        }
    }

    @GetMapping("/payInfo")
    public NotePressResult payInfo() {
        Param alipay = paramService.fetchParamByName(ParamKeyConstant.ADMIN_QRCODE_ALIPAY).getDataBean(Param.class);
        Param wechatPay = paramService.fetchParamByName(ParamKeyConstant.ADMIN_QRCODE_WECHAT).getDataBean(Param.class);
        return writeJsonMap(alipay != null ? alipay.getValue() : "", wechatPay != null ? wechatPay.getValue() : "");
    }
}
