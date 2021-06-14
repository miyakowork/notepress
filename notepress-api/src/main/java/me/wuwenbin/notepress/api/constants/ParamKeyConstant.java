package me.wuwenbin.notepress.api.constants;

/**
 * @author wuwen
 */
public interface ParamKeyConstant {

    /**
     * 系统初始化时间参数key
     */
    String SYSTEM_INIT_DATETIME = "system_init_datetime";

    /**
     * 系统运营开始时间
     */
    String SYSTEM_OPERATION_DATETIME = "system_operation_datetime";

    /**
     * 系统启动时间参数key
     */
    String SYSTEM_STARTED_DATETIME = "system_started_datetime";

    /**
     * 上传参数key
     */
    String UPLOAD_TYPE = "upload_type";
    String UPLOAD_EXTS = "upload_exts";
    String UPLOAD_MAX_SIZE = "upload_max_size";

    /**
     * 系统初始化状态参数 key
     */
    String SYSTEM_INIT_STATUS = "system_init_status";

    /**
     * 主题配置参数key
     */
    String THEME_NAME = "theme_name";

    /**
     * 页面数量大小
     */
    String CONTENT_PAGE_SIZE = "content_page_size";

    /**
     * 下面是邮件相关的参数
     */
    String MAIL_SMPT_SERVER_ADDR = "mail_smpt_server_addr";
    String MAIL_SMPT_SERVER_PORT = "mail_smpt_server_port";
    String MAIL_SERVER_ACCOUNT = "mail_server_account";
    String MAIL_SENDER_NAME = "mail_sender_name";
    String MAIL_SERVER_PASSWORD = "mail_server_password";

    /**
     * 网站标题参数key
     */
    String WEBSITE_NAME = "website_name";
    String WEBSITE_SUBTITLE = "website_subtitle";
    String WEBSITE_LOGO_SMALL = "website_logo_small";
    String WEBSITE_LOGO_LARGE = "website_logo_large";
    String WEBSITE_LOGO_FONTICON = "website_logo_fonticon";
    String WEBSITE_INFO_LABEL = "website_info_label";
    String WEBSITE_DOMAIN ="website_domain";

    /**
     * 统计方式
     */
    String STATISTICS_METHOD = "statistics_method";

    /**
     * 七牛相关key值
     */
    String QINIU_ACCESS_KEY = "qiniu_accessKey";
    String QINIU_SECRET_KEY = "qiniu_secretKey";
    String QINIU_BUCKET = "qiniu_bucket";
    String QINIU_DOMAIN = "qiniu_domain";

    /**
     * 管理员显示的一些参数key
     */
    String ADMIN_GLOBAL_NICKNAME = "admin_global_nickname";
    String ADMIN_GLOBAL_AVATAR = "admin_global_avatar";
    String ADMIN_QRCODE_ALIPAY = "admin_qrcode_alipay";
    String ADMIN_QRCODE_WECHAT = "admin_qrcode_wechat";


    /**
     * 一些开关设置参数的key
     */
    String SWITCH_UPLOAD = "switch_upload";
    String SWITCH_COMMENT = "switch_comment";
    String SWITCH_COMMENT_NOTICE_MAIL = "switch_comment_notice_mail";
    String SWITCH_USER_REG = "switch_user_reg";
    String SWITCH_VISIT_LOG = "switch_visit_log";
    String SWITCH_QQ_LOGIN = "switch_qq_login";
    String SWITCH_GITHUB_LOGIN = "switch_github_login";
    String SWITCH_HOMEPAGE_INDEX = "switch_homepage_index";

    /**
     * seo
     */
    String SEO_DESCRIPTION = "seo_description";
    String SEO_KEYWORDS = "seo_keywords";

    /**
     * 支付插件参数key
     */
    String RECHARGE_SERVER_DOMAIN = "recharge_server_domain";
    String SWITCH_RECHARGE_SERVER = "switch_recharge_server";
    String RECHARGE_SIGN_SECRET_KEY = "recharge_sign_secretKey";
    String RECHARGE_RATE = "recharge_rate";

}
