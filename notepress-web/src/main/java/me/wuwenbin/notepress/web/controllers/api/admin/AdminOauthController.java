package me.wuwenbin.notepress.web.controllers.api.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.Oauth;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.IOauthService;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author wuwen
 */
@RestController
@RequestMapping("/admin/oauth")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AdminOauthController extends NotePressBaseController {

    private final IOauthService oauthService;

    @PostMapping
    public NotePressResult userList(Page<Oauth> sysUserPage, @RequestBody LayuiTableQuery<Oauth> layuiTableQuery) {
        //查询非管理员用户
        return writeJsonLayuiTable(oauthService.findOauthList(sysUserPage, layuiTableQuery));
    }

    @PostMapping("/update")
    public NotePressResult updateOauth(@Valid @NotNull Oauth oauth, BindingResult result) {
        if (!result.hasErrors()) {
            oauth.setGmtUpdate(LocalDateTime.now());
            oauth.setUpdateBy(NotePressSessionUtils.getSessionUser().getId());
            return writeJsonJudgedBool(oauthService.updateById(oauth), "修改成功！", "修改失败！");
        }
        return writeJsonJsr303(result.getFieldErrors());
    }

    @PostMapping("/add")
    public NotePressResult addOauth(@Valid @NotNull Oauth oauth, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            oauth.setGmtCreate(LocalDateTime.now());
            oauth.setCreateBy(NotePressSessionUtils.getSessionUser().getId());
            return writeJsonJudgedBool(oauthService.save(oauth), "添加成功！", "添加失败！");
        }
        return writeJsonJsr303(bindingResult.getFieldErrors());
    }

    @PostMapping("/delete")
    public NotePressResult deleteOauth(Long id) {
        boolean res = oauthService.removeById(id);
        return writeJsonJudgedBool(res, "删除成功！", " 删除失败！");
    }
}
