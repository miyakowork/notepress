package me.wuwenbin.notepress.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Upload;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.base.INotePressService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * @author wuwen
 */
public interface IUploadService extends INotePressService<Upload> {

    /**
     * 上传方法
     *
     * @param file        文件对象
     * @param reqType     以什么方式上传的如 layui的上传：lay
     * @param uploadParam 上传的其他参数
     * @return
     * @throws IOException
     */
    NotePressResult doUpload(MultipartFile file, String reqType, Map<String, Object> uploadParam) throws IOException;

    /**
     * 上传文件列表管理
     *
     * @param uploadImagePage
     * @param layuiTableQuery
     * @return
     */
    NotePressResult findUploadImageList(IPage<Upload> uploadImagePage, LayuiTableQuery<Upload> layuiTableQuery);
}
