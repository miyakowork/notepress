package me.wuwenbin.notepress.api.constants.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * created by Wuwenbin on 2019/11/25 at 5:48 下午
 *
 * @author wuwenbin
 */
public enum FileTypeEnum {

    /**
     * 上传文件Content-Type主类型
     */
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    TEXT_CSS("text/css"),
    TEXT_JAVASCRIPT("text/javascript"),
    IMAGE_GIF("image/gif"),
    IMAGE_PNG("image/png"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_BMP("image/bmp"),
    IMAGE_WEBP("image/webp"),
    IMAGE_XICON("image/xicon"),
    AUDIO_MIDI("audio/midi"),
    AUDIO_MPEG("audio/mpeg"),
    AUDIO_WEBM("audio/webm"),
    AUDIO_OGG("audio/ogg"),
    AUDIO_WAV("audio/wav"),
    VIDEO_WEBM("video/webm"),
    VIDEO_OGG("video/ogg"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    APPLICATION_PKCS12("application/pkcs12"),
    APPLICATION_XHTML_XML("application/xhtml+xml"),
    APPLICATION_XML("application/xml"),
    APPLICATION_PDF("application/pdf"),
    UNKNOWN("unknown");

    @Getter
    private String type;

    FileTypeEnum(String type) {
        this.type = type;
    }

    public static FileTypeEnum getFileTypeByContentType(String contentType) {
        if (StringUtils.isEmpty(contentType)) {
            return UNKNOWN;
        }
        for (FileTypeEnum fileTypeEnum : FileTypeEnum.values()) {
            if (fileTypeEnum.getType().equalsIgnoreCase(contentType)) {
                return fileTypeEnum;
            }
        }
        return UNKNOWN;
    }

    /**
     * 判断是否为图片
     *
     * @return
     */
    public boolean isImg() {
        return this.type.startsWith("image/");
    }


}
