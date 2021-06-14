package me.wuwenbin.notepress.api.model.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.constants.enums.UploadTypeEnum;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

import java.time.LocalDateTime;

/**
 * created by Wuwenbin on 2018/7/15 at 12:11
 *
 * @author wuwenbin
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class Upload extends BaseEntity<Upload> {

    private Long id;
    /**
     * 文件物理存储路径
     */
    private String diskPath;

    /**
     * 文件url访问地址
     */
    private String virtualPath;
    private LocalDateTime upload;
    private UploadTypeEnum type;
    private String contentType;
    private Long userId;

    /**
     * 通常为文章id,如果是文章的创建与修改,此处为文章的id
     * 或者与文章创建修改类似的,如商品的创建与修改,此处则为商品的id
     * 等等如此
     */
    private String objectKeyId;
}
