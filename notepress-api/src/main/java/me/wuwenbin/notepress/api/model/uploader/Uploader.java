package me.wuwenbin.notepress.api.model.uploader;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * created by Wuwenbin on 2019/11/26 at 9:58 上午
 *
 * @author wuwenbin
 */
public interface Uploader extends Serializable {

    /**
     * 上传成功返回json
     *
     * @param message
     * @param visitUrl
     * @return
     */
    default Map<String, Object> ok(String message, String visitUrl) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("message", message);
        param.put("visitUrl", visitUrl);
        return param;
    }

    /**
     * 上传错误返回json
     *
     * @param message
     * @return
     */
    default Map<String, Object> err(String message) {
        Map<String, Object> param = new HashMap<>(2);
        param.put("message", message);
        return param;
    }
}
