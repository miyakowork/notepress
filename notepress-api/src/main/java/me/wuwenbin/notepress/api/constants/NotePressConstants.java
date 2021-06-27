package me.wuwenbin.notepress.api.constants;

/**
 * 统一常量类型
 * created by Wuwenbin on 2019/11/15 at 3:11 下午
 *
 * @author wuwenbin
 */
public interface NotePressConstants {
    /**
     * 默认上传文件夹
     */
    String DEFAULT_UPLOAD_PATH = "upload";

    /**
     * 管理员页面后台 url 前缀
     */
    String PREFIX_ADMIN_URL = "/admin";

    /**
     * 存储用户 session 对象的 session key：__session_key_for_username
     */
    String SESSION_USER_KEY = "__session_key_for_username";

    /**
     * 存储在登陆之前最后一次访问的 URL
     */
    String SESSION_LAST_VISIT_URL_KEY = "session_last_visit_url_key";


    /**
     * 开启
     */
    String OPEN = "1";

    /**
     * 侧边栏：左侧布局还是右侧布局
     */
    String PAGE_STYLE_LEFT = "-1";
    String PAGE_STYLE_RIGHT = "1";

}
