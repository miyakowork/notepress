package me.wuwenbin.notepress.web.controllers.api.admin;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.UploadConstant;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Res;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.IResService;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

/**
 * @author wuwen
 */
@RestController
@RequestMapping("/admin/res")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AdminResController extends NotePressBaseController {

    private final IResService resService;

    @PostMapping
    public NotePressResult resList(Page<Res> categoryPage,
                                   @RequestBody LayuiTableQuery<Res> layuiTableQuery) {
        return writeJsonLayuiTable(resService.findResList(categoryPage, layuiTableQuery));
    }

    @PostMapping("/upload")
    public NotePressResult upload(@RequestParam(value = UploadConstant.FORM_NAME_QINIU) MultipartFile file,
                                  Res res, String cateIdsStr) {
        if (StrUtil.isEmpty(cateIdsStr)) {
            return writeJsonErrorMsg("请选择一个资源分类！");
        }
        if (file == null) {
            return writeJsonErrorMsg("上传文件不能为空！");
        }
        return writeJson(() -> resService.uploadRes(file, res, Arrays.asList(cateIdsStr.split(","))));
    }

    @PostMapping("/upload3")
    public NotePressResult upload3(Res res, String cateIdsStr) {
        if (StrUtil.isEmpty(cateIdsStr)) {
            return writeJsonErrorMsg("请选择一个资源分类！");
        }
        return writeJson(() -> resService.uploadRes(null, res, Arrays.asList(cateIdsStr.split(","))));
    }

    @PostMapping("/delete")
    public NotePressResult delete(@NotNull String id) {
        return writeJson(() -> resService.deleteRes(id));
    }

    @PostMapping("/updateFileProp")
    public NotePressResult updateFileName(String id, String coin, String remark, String authCode) {
        boolean res = resService.update(Wrappers.<Res>update()
                .set(StrUtil.isNotEmpty(coin) && NumberUtil.isInteger(coin), "coin", coin)
                .set(StrUtil.isNotEmpty(remark), "remark", remark)
                .set(StrUtil.isNotEmpty(authCode), "auth_code", authCode)
                .eq("id", id));
        return writeJsonJudgedBool(res, "修改成功！", "修改失败！");
    }
}
