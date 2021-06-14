package me.wuwenbin.notepress.api.model.entity.system;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.constants.groups.ReplyComment;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * created by Wuwenbin on 2018/7/15 at 11:52
 *
 * @author wuwenbin
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class SysNotice extends BaseEntity<SysNotice> {

    private Long id;
    @NotNull
    private String contentId;
    @NotNull
    private Long userId;
    @NotNull(groups = ReplyComment.class)
    private Long replyId;
    private String commentText;
    @NotNull
    @Length(min = 1, max = 1000, message = "字数必须在1000字以内")
    private String commentHtml;
    private Boolean status;
    private String ipAddr;
    private String ipInfo;
    private String userAgent;
    private Integer floor;
    /**
     * 消息是否已读
     */
    private Boolean isRead;

    /**
     * 区分评论来自哪种页面
     */
    @NotNull(message = "页面类型不能为空！")
    private String pageType;

}
