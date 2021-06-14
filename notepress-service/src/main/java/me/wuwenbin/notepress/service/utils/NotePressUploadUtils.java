package me.wuwenbin.notepress.service.utils;

import cn.hutool.core.map.MapUtil;
import com.qiniu.util.Auth;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.constants.UploadConstant;
import me.wuwenbin.notepress.api.constants.enums.FileTypeEnum;
import me.wuwenbin.notepress.api.constants.enums.UploadMethod;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.utils.NotePressServletUtils;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.mapper.ParamMapper;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wuwen
 */
public class NotePressUploadUtils {


    /**
     * 根据设定的上传方式（本地服务器上传还是七牛云上传）来匹配相应的service实例
     *
     * @return
     */
    public static UploadMethod getUploadMethod() {
        String uri = NotePressServletUtils.getRequest().getRequestURI();
        if ("/init/upload".contentEquals(uri)) {
            return UploadMethod.INIT;
        }
        Param uploadMethodParam = NotePressUtils.getBean(IParamService.class).getOne(ParamQuery.build(ParamKeyConstant.UPLOAD_TYPE));
        if (uploadMethodParam != null) {
            String uploadMethodInDb = uploadMethodParam.getValue();
            if (UploadMethod.LOCAL.name().equalsIgnoreCase(uploadMethodInDb)) {
                return UploadMethod.LOCAL;
            } else if (UploadMethod.QINIU.name().equalsIgnoreCase(uploadMethodInDb)) {
                return UploadMethod.QINIU;
            }
        }
        throw new NotePressException("未知上传方式类型，请检查您的设置！");
    }

    /**
     * 根据上传文件contentType获取文件存储路径文件夹（前缀）
     *
     * @param contentType
     * @return
     */
    public static String getPathPrefixByContentType(String contentType) {
        if (StringUtils.isEmpty(contentType)) {
            return UploadConstant.PATH_PREFIX_FILE;
        } else {
            if (FileTypeEnum.getFileTypeByContentType(contentType).isImg()) {
                return UploadConstant.PATH_PREFIX_IMAGE;
            } else {
                return UploadConstant.PATH_PREFIX_FILE;
            }
        }
    }

    /**
     * 设置请求上传的额外的一些参数
     *
     * @return
     */
    public static Map<String, Object> setUploadParams(Map<String, Object> otherParam) {
        Map<String, Object> param = new HashMap<>(4);
        HttpServletRequest request = NotePressServletUtils.getRequest();
        param.put(UploadConstant.REQUEST_PARAM_CODE, request.getParameter(UploadConstant.REQUEST_PARAM_CODE));
        param.put(UploadConstant.REQUEST_PARAM_REMARK, request.getParameter(UploadConstant.REQUEST_PARAM_REMARK));
        param.put(UploadConstant.REQUEST_PARAM_OBJECT_KEY_ID, request.getParameter(UploadConstant.REQUEST_PARAM_OBJECT_KEY_ID));
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        if (sessionUser != null) {
            param.put(UploadConstant.REQUEST_PARAM_USER_ID, sessionUser.getId());
        }
        if (MapUtil.isNotEmpty(otherParam)) {
            param.putAll(otherParam);
        }
        return param;
    }

    /**
     * 获取bucketName
     *
     * @return
     */
    public static String getBucketName() {
        ParamMapper paramMapper = NotePressUtils.getBean(ParamMapper.class);
        return paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.QINIU_BUCKET)).getValue();
    }

    /**
     * 简单上传获取uptoken
     *
     * @return
     */
    public static String getQiniuUpToken() {
        ParamMapper paramMapper = NotePressUtils.getBean(ParamMapper.class);
        String bucketName = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.QINIU_BUCKET)).getValue();
        //简单上传，使用默认策略，只需要设置上传的空间名就可以了
        return getQiniuAuth().uploadToken(bucketName);
    }

    /**
     * 覆盖上传获取uptoken
     *
     * @param fileKey
     * @return
     */
    public static String getQiniuUpToken(String fileKey) {
        ParamMapper paramMapper = NotePressUtils.getBean(ParamMapper.class);
        String bucketName = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.QINIU_BUCKET)).getValue();
        //简单上传，使用默认策略，只需要设置上传的空间名就可以了
        return getQiniuAuth().uploadToken(bucketName, fileKey);
    }

    /**
     * 获取 Auth
     *
     * @return
     */
    public static Auth getQiniuAuth() {
        ParamMapper paramMapper = NotePressUtils.getBean(ParamMapper.class);
        String accessKey = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.QINIU_ACCESS_KEY)).getValue();
        String secretKey = paramMapper.selectOne(ParamQuery.build(ParamKeyConstant.QINIU_SECRET_KEY)).getValue();
        //密钥配置
        //简单上传，使用默认策略，只需要设置上传的空间名就可以了
        return Auth.create(accessKey, secretKey);
    }


}
