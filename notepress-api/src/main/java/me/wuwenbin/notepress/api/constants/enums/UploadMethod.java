package me.wuwenbin.notepress.api.constants.enums;

/**
 * @author wuwen
 */

public enum UploadMethod {
    /**
     * 本地上传
     */
    LOCAL,

    /**
     * 七牛云上传
     */
    QINIU,

    /**
     * 初始化的时候需要临时上传
     */
    INIT;

}
