package me.wuwenbin.notepress.web.controllers.api;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author wuwen
 */
@Controller
@RequestMapping("/init")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class InitController extends NotePressBaseController {

    private final IParamService paramService;
    private final ISysUserService sysUserService;

    @GetMapping("/uploadPath")
    @ResponseBody
    public NotePressResult uploadPath() {
        return paramService.fetchFileUploadInfo();
    }

    @SneakyThrows
    @PostMapping
    @ResponseBody
    public NotePressResult init(SysUser sysUser, String websiteDomain) {
        String npAvatar = request.getParameter("np_avatar");
        if (StrUtil.isNotEmpty(npAvatar)) {
            sysUser.setAvatar(npAvatar);
        }
        Param param = Param.builder().name(ParamKeyConstant.WEBSITE_DOMAIN).value(websiteDomain).group("1").orderIndex(0).build().remark("前台主站url");
        paramService.upsertParam(param);
        return writeJson(() -> sysUserService.initAdministrator(sysUser));
    }

    @GetMapping("/status")
    @ResponseBody
    public NotePressResult status() {
        return writeJson(paramService::fetchInitStatus);
    }

    @GetMapping("/tips")
    public String tips(Model model) {
        model.addAttribute("", "");
        return "templates/tips";
    }
}
