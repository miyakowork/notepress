package me.wuwenbin.notepress.api.model.uploader;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * NKindeditor上传文件返回的对象
 * created by Wuwenbin on 2018/8/4 at 10:28
 *
 * @author wuwenbin
 */
@Data
@Component
public class NkUploader implements Serializable {

    public static final String SUCCESS = "000";
    public static final String ERROR = "001";

    private String code;
    private String message;
    private Map<String, Object> item = new HashMap<>();

    public NkUploader ok(String message, String url) {
        NkUploader json = new NkUploader();
        json.setCode(SUCCESS);
        json.setMessage(message);
        item.put("url", url);
        json.setItem(item);
        return json;
    }

    public NkUploader err(String message) {
        NkUploader json = new NkUploader();
        json.setCode(ERROR);
        json.setMessage(message);
        return json;
    }
}
