package me.wuwenbin.notepress.api.constants;

/**
 * 上传相关的常量
 * created by Wuwenbin on 2018/7/16 at 15:26
 *
 * @author wuwenbin
 */
public interface UploadConstant {

    /**
     * 图片
     */
    String PATH_PREFIX_IMAGE = "img";

    /**
     * 非图片文件
     */
    String PATH_PREFIX_FILE = "file";

    /**
     * 访问文件的虚拟路径前缀
     */
    String PATH_PREFIX_VISIT = "/upfiles";

    /**
     * 上传类型的 input 表单的 name 值
     * layui 的上传
     */
    String FORM_NAME_LAY = "layFile";

    /**
     * 上传的文件对象参数名，通用的
     */
    String FORM_NAME_COMMON = "notepressFile";

    /**
     * 上传资源文件至七牛
     */
    String FORM_NAME_QINIU = "qiniu-res-file";

    /**
     * 上传文件对象参数名，editormd
     */
    String FORM_NAME_EDITORMD = "editormd-image-file";
    /**
     * 上传组件类型，layui 组件
     */
    String UPLOAD_TYPE_LAY = "lay";

    /**
     * 上传组件类型，nkeditor 组件
     */
    String UPLOAD_TYPE_NK = "nk";

    /**
     * 上传组件类型，editorMD 组件
     */
    String UPLOAD_TYPE_MD = "md";

    /**
     * 上传参数 上传类型 code 请求参数 key
     */
    String REQUEST_PARAM_CODE = "code";

    /**
     * 上传参数用户 id 请求参数 key
     */
    String REQUEST_PARAM_USER_ID = "userId";

    /**
     * 上传参数对象所在内容 ID，如文章内容 ID 请求参数 key
     */
    String REQUEST_PARAM_OBJECT_KEY_ID = "objectKeyId";

    /**
     * 上传参数备注信息 请求参数 key
     */
    String REQUEST_PARAM_REMARK = "remark";
}
