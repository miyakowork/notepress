package me.wuwenbin.notepress.web.controllers.api.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Upload;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.IUploadService;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wuwenbin
 */
@RestController
@RequestMapping("/admin/upload")
public class AdminUploadController extends NotePressBaseController {

    @Autowired
    private IUploadService uploadService;

    @PostMapping("/imgList")
    public NotePressResult uploadImgList(Page<Upload> uploadImagePage, @RequestBody LayuiTableQuery<Upload> layuiTableQuery) {
        return writeJsonLayuiTable(uploadService.findUploadImageList(uploadImagePage, layuiTableQuery));
    }
}
