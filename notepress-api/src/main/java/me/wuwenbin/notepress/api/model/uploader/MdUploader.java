package me.wuwenbin.notepress.api.model.uploader;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * editorMd上传文件返回的对象
 * @author wuwen
 */
@Data
@Component
public class MdUploader implements Serializable {

    public static final int SUCCESS = 1;
    public static final int ERROR = 0;

    private int success;
    private String message;
    private String url;

    public MdUploader ok(String msg, String url) {
        MdUploader json = new MdUploader();
        json.setSuccess(SUCCESS);
        json.setMessage(msg);
        json.setUrl(url);
        return json;
    }

    public MdUploader err(String msg) {
        MdUploader json = new MdUploader();
        json.setSuccess(ERROR);
        json.setMessage(msg);
        return json;
    }
}
