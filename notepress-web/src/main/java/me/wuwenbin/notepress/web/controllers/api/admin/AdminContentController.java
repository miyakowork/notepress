package me.wuwenbin.notepress.web.controllers.api.admin;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Content;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.query.ContentQuery;
import me.wuwenbin.notepress.api.service.IContentService;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import me.wuwenbin.notepress.web.controllers.utils.NotePressWebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Objects;

/**
 * @author wuwenbin
 */
@RestController
@RequestMapping("/admin/content")
public class AdminContentController extends NotePressBaseController {

    @Autowired
    private IContentService contentService;

    @GetMapping("/nextContentId")
    public NotePressResult nextContentId() {
        return writeJsonOk("c" + IdUtil.objectId());
    }

    @GetMapping("/nextHideId")
    public NotePressResult nextHideId() {
        return writeJsonOk("h" + IdUtil.objectId());
    }

    @PostMapping
    public NotePressResult contentList(Page<Content> contentPage,
                                       @RequestBody LayuiTableQuery<Content> layuiTableQuery) {
        JSONObject otherParams = layuiTableQuery.getOtherParams();
        String cateIds = null;
        if (otherParams != null) {
            cateIds = otherParams.getStr("cateIds");
        }
        return writeJsonLayuiTable(contentService.findContentList(contentPage, layuiTableQuery, cateIds));
    }

    @GetMapping("/findById")
    public NotePressResult findById(String id) {
        return writeJson(() -> contentService.findContentById(id));
    }

    @PostMapping("/create")
    public NotePressResult create(@Valid Content content, BindingResult result,
                                  @RequestParam("cates[]") String[] cates, @RequestParam("tagNames[]") String[] tagNames) {
        if (!result.hasErrors()) {
            List<String> images = NotePressWebUtils.getImageSrc(content.getHtmlContent());
            content.setImageList(images);
            content.setImages(String.join(",", images));
            SysUser sessionUser = NotePressSessionUtils.getSessionUser();
            if (sessionUser == null) {
                return writeJsonErrorMsg("非法操作！");
            }
            Long userId = sessionUser.getId();
            return contentService.createContent(content, userId, cates, tagNames);
        }
        return writeJsonJsr303(result.getFieldErrors());
    }

    @PostMapping("/modify")
    public NotePressResult modify(@Valid Content content, BindingResult result,
                                  @RequestParam("cates[]") String[] cates, @RequestParam("tagNames[]") String[] tagNames) {
        if (!result.hasErrors()) {
            List<String> images = NotePressWebUtils.getImageSrc(content.getHtmlContent());
            content.setImageList(images);
            content.setImages(String.join(",", images));
            Long userId = Objects.requireNonNull(NotePressSessionUtils.getSessionUser()).getId();
            return writeJson(() -> contentService.modifyContent(content, userId, cates, tagNames));
        }
        return writeJsonJsr303(result.getFieldErrors());
    }

    @PostMapping("/tagModify")
    public NotePressResult tagModify(@NotEmpty String cid, @NotEmpty String name, @NotEmpty Boolean value) {
        boolean res = contentService.update(ContentQuery.buildTagModify(cid, name, value));
        return writeJsonJudgedBool(res, "修改成功", "修改失败！");
    }

    @PostMapping("/delete")
    public NotePressResult delete(String id) {
        boolean res = contentService.update(ContentQuery.buildDelete(id, false));
        return writeJsonJudgedBool(res, "删除成功！", "删除失败！");
    }
}
