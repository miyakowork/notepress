package me.wuwenbin.notepress.api.constants.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import lombok.Getter;

/**
 * created by Wuwenbin on 2019/11/18 at 10:58 上午
 *
 * @author wuwenbin
 */
public enum UploadTypeEnum implements IEnum<Integer> {
    /**
     * 上传功能的信息备注
     */
    UNKNOWN(0, "未知方式上传"),
    INIT_AVATAR(1, "初始化界面管理员头像上传"),
    ARTICLE_CREATE(2, "内容上传"),
    ADMIN_MODIFY_AVATAR(3, "管理员修改用户头像"),
    ADMIN_MODIFY_QRCODE_ALIPAY(4, "管理员修改个人支付宝二维码"),
    ADMIN_MODIFY_QRCODE_WECHATPAY(5, "管理员修改个人微信支付二维码"),
    ADMIN_SETTINGS_LOGO(6, "管理员网站设置中上传网站logo"),
    ADMIN_CATEGORY_UPLOAD(7, "管理员分类图标上传"),
    ADMIN_CONTENT_ADD(8, "添加内容封面上传"),
    ADMIN_CONTENT_EDITORMD_UPLOAD(9, "editorMD上传文件/图片"),
    ADMIN_CONTENT_KINDEDITOR_UPLOAD(10, "KindEditor上传文件/图片"),
    THEME_DEF_CONFIG(11, "默认主题上传的文件/图片"),
    USER_MODIFY_AVATAR(12, "用户自己修改头像");

    @Getter
    private int code;

    @Getter
    private String desc;

    UploadTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public static UploadTypeEnum parseCodeToEnumType(int code) {
        switch (code) {
            case 1:
                return ARTICLE_CREATE;
            case 2:
                return INIT_AVATAR;
            case 3:
                return ADMIN_MODIFY_AVATAR;
            case 4:
                return ADMIN_MODIFY_QRCODE_ALIPAY;
            case 5:
                return ADMIN_MODIFY_QRCODE_WECHATPAY;
            case 6:
                return ADMIN_SETTINGS_LOGO;
            case 7:
                return ADMIN_CATEGORY_UPLOAD;
            case 8:
                return ADMIN_CONTENT_ADD;
            case 9:
                return ADMIN_CONTENT_EDITORMD_UPLOAD;
            case 10:
                return ADMIN_CONTENT_KINDEDITOR_UPLOAD;
            case 11:
                return THEME_DEF_CONFIG;
            case 12:
                return USER_MODIFY_AVATAR;
            default:
                return UNKNOWN;
        }
    }

    /**
     * 枚举的返回值
     *
     * @return
     */
    @Override
    public Integer getValue() {
        return this.code;
    }


}
