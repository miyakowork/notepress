package me.wuwenbin.notepress.web.controllers.api.theme;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author wuwenbin
 */
@Controller
@RequestMapping()
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class NotePressIndexController extends NotePressBaseController {

    private final IParamService paramService;

    @GetMapping("/")
    public ModelAndView indexPage(SitePreference sitePreference) {
        Param param = toBeanNull(paramService.fetchParamByName(ParamKeyConstant.SWITCH_HOMEPAGE_INDEX), Param.class);
        if (param != null && StrUtil.isNotEmpty(param.getValue()) && "1".equals(param.getValue())) {
            return new ModelAndView(new RedirectView("/index"));
        } else if (sitePreference == SitePreference.MOBILE || sitePreference == SitePreference.TABLET) {
            return new ModelAndView(new RedirectView("/index"));
        } else {
            return new ModelAndView("index");
        }
    }
}
