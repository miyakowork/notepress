package me.wuwenbin.notepress.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.constants.UploadConstant;
import me.wuwenbin.notepress.api.constants.enums.UploadMethod;
import me.wuwenbin.notepress.api.constants.enums.UploadTypeEnum;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.Upload;
import me.wuwenbin.notepress.api.model.layui.LayuiTable;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.model.uploader.LayUploader;
import me.wuwenbin.notepress.api.model.uploader.MdUploader;
import me.wuwenbin.notepress.api.model.uploader.NkUploader;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.query.UploadQuery;
import me.wuwenbin.notepress.api.service.IUploadService;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.mapper.ParamMapper;
import me.wuwenbin.notepress.service.mapper.UploadMapper;
import me.wuwenbin.notepress.service.utils.NotePressUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author wuwen
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UploadServiceImpl extends ServiceImpl<UploadMapper, Upload> implements IUploadService {

    @Autowired
    private UploadMapper uploadMapper;
    @Qualifier("notePressSetting")
    @Autowired
    private Setting notePressSetting;
    @Autowired
    private ParamMapper paramMapper;

    /**
     * 根据上传的额外参数以及生成的参数生成新的upload对象插入数据库中
     *
     * @param uploadParam
     * @param uploadFilePath
     * @param virtualPath
     * @param contentType
     * @return
     */
    private static Upload newUpload(Map<String, Object> uploadParam, String uploadFilePath, String virtualPath, String contentType) {
        Integer uploadTypeCode = MapUtil.getInt(uploadParam, UploadConstant.REQUEST_PARAM_CODE);
        if (uploadTypeCode == null) {
            throw new NotePressException(NotePressErrorCode.RequestError, "上传参数 code 不能为空！");
        }
        UploadTypeEnum uploadTypeEnum = UploadTypeEnum.parseCodeToEnumType(uploadTypeCode);
        //当前上传文件的用户ID
        Long userId = MapUtil.getLong(uploadParam, UploadConstant.REQUEST_PARAM_USER_ID);
        //业务id，如果在内容内上传那就是内容的ID，可以为空
        String objectKeyId = MapUtil.getStr(uploadParam, UploadConstant.REQUEST_PARAM_OBJECT_KEY_ID);
        //上传的一些说明
        String remark = MapUtil.getStr(uploadParam, UploadConstant.REQUEST_PARAM_REMARK);
        if (StringUtils.isEmpty(remark)) {
            remark = uploadTypeEnum.getDesc();
        }
        //插入到数据库中
        return Upload.builder()
                .diskPath(uploadFilePath).virtualPath(virtualPath).upload(LocalDateTime.now())
                .type(uploadTypeEnum).userId(userId).objectKeyId(objectKeyId).contentType(contentType).build()
                .createBy(userId).gmtCreate(LocalDateTime.now()).remark(remark);
    }

    /**
     * 统一处理返回结果
     *
     * @param ur
     * @param reqType
     * @param virtualPath
     * @return
     */
    private static NotePressResult result(int ur, String reqType, String virtualPath) {
        final LayUploader layUploader = NotePressUtils.getBean(LayUploader.class);
        final NkUploader nkUploader = NotePressUtils.getBean(NkUploader.class);
        final MdUploader mdUploader = NotePressUtils.getBean(MdUploader.class);
        //如果是使用layui的组件上传
        if (UploadConstant.UPLOAD_TYPE_LAY.equalsIgnoreCase(reqType)) {
            if (ur == 1) {
                return NotePressResult.createOkData(layUploader.ok("上传成功！", virtualPath));
            } else {
                return NotePressResult.createOkData(layUploader.err("上传失败！"));
            }
        }
        //nkeditor或者kindeditor组件上传
        else if (UploadConstant.UPLOAD_TYPE_NK.equalsIgnoreCase(reqType)) {
            if (ur == 1) {
                return NotePressResult.createOkData(nkUploader.ok("上传成功！", virtualPath));
            } else {
                return NotePressResult.createOkData(nkUploader.err("上传失败！"));
            }
        }
        //editorMD组件上传
        else if (UploadConstant.UPLOAD_TYPE_MD.equalsIgnoreCase(reqType)) {
            if (ur == 1) {
                return NotePressResult.createOkData(mdUploader.ok("上传成功！", virtualPath));
            } else {
                return NotePressResult.createOkData(mdUploader.err("上传失败！"));
            }
        }
        //其他组件上传
        else {
            if (ur == 1) {
                return NotePressResult.createOk("上传成功！", virtualPath);
            } else {
                return NotePressResult.createErrorMsg("上传失败！");
            }
        }
    }


    //======================上传的一些私有方法=================

    /**
     * 上传方法
     *
     * @param multiPartFile 文件对象
     * @param reqType       以什么方式上传的如 layui的上传：lay
     * @param uploadParam   上传的其他参数
     * @return
     * @throws IOException
     */
    @Override
    public NotePressResult doUpload(MultipartFile multiPartFile, String reqType, Map<String, Object> uploadParam) throws IOException {
        String fileName = multiPartFile.getOriginalFilename();
        log.info("==> 上传[{}]类型文件，文件名：[{}}]", multiPartFile.getContentType(), fileName);
        if (StringUtils.isEmpty(fileName)) {
            throw new NotePressException(NotePressErrorCode.RequestError, "上传文件名为空！");
        } else {
            String ext = fileName.substring(fileName.lastIndexOf("."));
            Param extParam = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.UPLOAD_EXTS));
            if (extParam != null && !ArrayUtil.contains(extParam.getValue().split("\\|"), ext.substring(1))) {
                return NotePressResult.createErrorMsg("不允许上传该类型文件！");
            }
            String newFileName = IdUtil.randomUUID().concat(ext);
            UploadMethod uploadMethod = NotePressUploadUtils.getUploadMethod();

            //本地服务器上传
            if (UploadMethod.LOCAL == uploadMethod || UploadMethod.INIT == uploadMethod) {
                Upload u = this.doLocalUpload(multiPartFile, newFileName, uploadParam);
                int ur = uploadMapper.insert(u);
                return result(ur, reqType, u.getVirtualPath());
            }

            //七牛云服务器上传
            else if (UploadMethod.QINIU == uploadMethod) {
                Upload qu = this.doQiniuUpload(multiPartFile, NotePressUploadUtils.getQiniuUpToken(), uploadParam);
                int ur = uploadMapper.insert(qu);
                return result(ur, reqType, qu.getVirtualPath());
            }

            //留给以后的扩展，如AliOss
            else {
                return NotePressResult.createErrorMsg("仅支持【服务器上传】和【七牛云上传】两种方式，其他云上传敬请期待！");
            }
        }
    }

    @Override
    public NotePressResult findUploadImageList(IPage<Upload> uploadImagePage, LayuiTableQuery<Upload> layuiTableQuery) {
        QueryWrapper<Upload> queryWrapper = UploadQuery.buildByLikeRightContentType("image/");
        String sort = StrUtil.toUnderlineCase(layuiTableQuery.getSort());
        String order = layuiTableQuery.getOrder();
        if (StrUtil.isNotEmpty(sort)) {
            if ("asc".equalsIgnoreCase(order)) {
                queryWrapper.orderByAsc(sort);
            } else {
                queryWrapper.orderByDesc(sort);
            }
        }
        return findLayuiTableList(uploadImagePage, layuiTableQuery, (uploadPage, uploadLayuiTableQuery) -> {
            IPage<Upload> up = baseMapper.selectPage(uploadPage, queryWrapper);
            return NotePressResult.createOkData(LayuiTable.success(up));
        });
    }

    //=============================静态私有方法=======================

    /**
     * 本地上传方法，生成upload对象
     *
     * @param multiPartFile
     * @param newFileName
     * @param uploadParam
     * @return
     * @throws IOException
     */
    private Upload doLocalUpload(MultipartFile multiPartFile, String newFileName, Map<String, Object> uploadParam) throws IOException {
        String prefix = notePressSetting.get("app", "uploadPath");
        String datePrefix = LocalDate.now().toString();
        String uploadPathPre = NotePressUploadUtils.getPathPrefixByContentType(multiPartFile.getContentType());
        String completePrefix = prefix + uploadPathPre + "/" + datePrefix + "/";
        //剔除字符串前缀 ==> [file:]
        File targetFile = new File(completePrefix.substring(5));
        boolean m = true;
        if (!targetFile.exists()) {
            m = targetFile.mkdirs();
        }
        String uploadFilePath;
        if (m) {
            uploadFilePath = FileUtil.getAbsolutePath(completePrefix + newFileName);
        } else {
            throw new NotePressException(NotePressErrorCode.DevError, "创建目录：" + completePrefix + "失败！");
        }
        FileOutputStream out = new FileOutputStream(uploadFilePath);
        out.write(multiPartFile.getBytes());
        out.flush();
        out.close();
        String virtualPath = UploadConstant.PATH_PREFIX_VISIT.concat("/").concat(uploadPathPre).concat("/" + datePrefix + "/").concat(newFileName);
        //插入到数据库中
        return newUpload(uploadParam, uploadFilePath, virtualPath, multiPartFile.getContentType());
    }

    /**
     * 七牛上传，生成upload对象
     *
     * @param file    文件
     * @param upToken 上传密钥
     * @return Response
     */
    private Upload doQiniuUpload(MultipartFile file, String upToken, Map<String, Object> uploadParam) {
        try {
            String resId = IdUtil.randomUUID();
            String fileName = file.getOriginalFilename();
            assert fileName != null;
            //构造一个带指定Zone对象的配置类
            Configuration cfg = new Configuration(Region.autoRegion());
            String extend = fileName.substring(fileName.lastIndexOf("."));
            //调用put方法上传
            Response res = new UploadManager(cfg).put(file.getBytes(), resId.concat(extend), upToken);
            log.info("[七牛上传文件] - [{}] - 返回信息：{}", res.isOK(), res.bodyString());
            if (res.isOK()) {
                JSONObject respObj = JSONUtil.parseObj(res.bodyString());
                String generateFileName = respObj.getStr("key");
                String qiniuDomain = paramMapper.selectOne(Wrappers.<Param>query().eq("name", ParamKeyConstant.QINIU_DOMAIN)).getValue();
                String src = qiniuDomain + "/" + generateFileName;

                //插入到数据库中
                return newUpload(uploadParam, "", src, file.getContentType());
            } else {
                throw new NotePressException("==> 上传文件至七牛云失败，信息：" + res.error);
            }
        } catch (QiniuException e) {
            Response re = e.response;
            log.error("==> [七牛上传文件] - [{}] - 异常信息：{}", re.isOK(), re.toString());
            try {
                log.error("==> 响应异常文本信息：{}", re.bodyString());
            } catch (QiniuException ignored) {
            }
            throw new NotePressException(NotePressErrorCode.QiniuError, e.getMessage());
        } catch (Exception ex) {
            log.error("==> 文件IO读取异常，异常信息：{}", ex.getMessage());
            throw new NotePressException(NotePressErrorCode.InternalServerError, ex.getMessage());
        }
    }
}
