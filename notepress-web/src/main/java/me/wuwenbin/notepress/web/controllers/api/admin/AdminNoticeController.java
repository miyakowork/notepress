package me.wuwenbin.notepress.web.controllers.api.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.groups.ReplyComment;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.SysNotice;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.ISysNoticeService;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

/**
 * @author wuwenbin
 */
@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AdminNoticeController extends NotePressBaseController {

    private final ISysNoticeService sysNoticeService;

    @GetMapping("/tabs")
    public NotePressResult noticeTypes() {
        return writeJson(sysNoticeService::findNoticeTypes);
    }

    @GetMapping("/findById")
    public NotePressResult findById(@NotNull String id) {
        SysNotice sysNotice = sysNoticeService.getById(id);
        return writeJsonJudgedNull(sysNotice, "获取成功！", "未查询到任何信息！");
    }

    @PostMapping
    public NotePressResult noticeLayuiTable(Page<SysNotice> noticePage,
                                            @RequestBody LayuiTableQuery<SysNotice> layuiTableQuery) {
        return writeJsonLayuiTable(sysNoticeService.findNoticeList(noticePage, layuiTableQuery));
    }

    @PostMapping("/update")
    public NotePressResult readNotice(SysNotice sysNotice) {
        boolean res = sysNoticeService.updateById(sysNotice);
        return writeJsonJudgedBool(res, "", "");
    }

    @PostMapping("/read")
    public NotePressResult readNotice(@RequestBody List<SysNotice> sysNotices) {
        boolean res = sysNoticeService.updateBatchById(sysNotices);
        return writeJsonJudgedBool(res, "", "操作失败！");
    }

    @PostMapping("/readAll")
    public NotePressResult readAllNotice(String pageType) {
        boolean res = sysNoticeService.update(
                Wrappers.<SysNotice>update().set("is_read", true)
                        .eq("is_read", false)
                        .eq(StrUtil.isNotEmpty(pageType), "page_type", pageType));
        return writeJsonJudgedBool(res, "操作成功！", "消息全部已读！");
    }

    @PostMapping("/delete")
    public NotePressResult deleteNotice(@RequestParam("ids[]") String[] ids) {
        boolean res = sysNoticeService.removeByIds(Arrays.asList(ids));
        return writeJsonJudgedBool(res, "删除成功！", "删除失败！");
    }

    @PostMapping("/reply")
    public NotePressResult reply(@Validated(ReplyComment.class) SysNotice sysNotice, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            boolean res = sysNoticeService.save(sysNotice);
            return writeJsonJudgedBool(res, "回复成功！", "回复失败！");
        } else {
            return writeJsonJsr303(bindingResult.getFieldErrors());
        }
    }
}
