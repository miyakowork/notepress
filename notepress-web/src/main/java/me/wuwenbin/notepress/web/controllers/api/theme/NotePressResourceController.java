package me.wuwenbin.notepress.web.controllers.api.theme;

import cn.hutool.core.util.StrUtil;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Refer;
import me.wuwenbin.notepress.api.model.entity.Res;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.page.NotePressPage;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.service.IReferService;
import me.wuwenbin.notepress.api.service.IResCateService;
import me.wuwenbin.notepress.api.service.IResService;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.service.utils.NotePressUploadUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuwen
 */
@Controller
@RequestMapping("/res")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class NotePressResourceController extends NotePressBaseController {

    private final IResService resService;
    private final IReferService referService;
    private final IResCateService resCateService;

    @GetMapping
    public String index(Model model) {
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        if (sessionUser != null) {
            List<String> resIds = referService.list(ReferQuery.buildBySelfIdAndType(sessionUser.getId().toString(), ReferTypeEnum.USER_RES))
                    .stream().map(Refer::getReferId).collect(Collectors.toList());
            model.addAttribute("userResIds", resIds);
        }
        model.addAttribute("srn", request.getParameter("srn"));
        return "netdisk";
    }

    @PostMapping("/list")
    @ResponseBody
    public NotePressResult list(NotePressPage<Res> resPage, String cid, String rn) {
        resPage.setOrderDirection("desc");
        resPage.setOrderField("gmt_create");
        return writeJson(() -> resService.findResList(resPage, cid, rn));
    }

    @PostMapping("/token/purchase")
    @ResponseBody
    public NotePressResult purchase(String ids) {
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        if (sessionUser != null) {
            if (StrUtil.isNotEmpty(ids)) {
                return writeJson(() -> resService.purchaseRes(Arrays.asList(ids.split(","))));
            }
        }
        return writeJsonErrorMsg("购买失败！");
    }

    @GetMapping("/cateTree")
    @ResponseBody
    public NotePressResult cateTree() {
        return writeJson(resCateService::findCateTree);
    }

    @GetMapping("/getUrl")
    @ResponseBody
    public NotePressResult getUrl(String hash) {
        Auth auth = NotePressUploadUtils.getQiniuAuth();
        //半小时有效期
        long expireSec = 1800;
        String finalUrl = auth.privateDownloadUrl(hash, expireSec);
        return writeJsonOk(finalUrl);
    }
}
