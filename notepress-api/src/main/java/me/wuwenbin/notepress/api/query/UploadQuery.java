package me.wuwenbin.notepress.api.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import me.wuwenbin.notepress.api.model.entity.Upload;

/**
 * @author wuwenbin
 */
public class UploadQuery extends BaseQuery {

    public static QueryWrapper<Upload> buildByLikeRightContentType(String contentType) {
        return Wrappers.<Upload>query().likeRight("content_type", contentType);
    }
}
