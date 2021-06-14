package me.wuwenbin.notepress.api.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;
import me.wuwenbin.notepress.api.annotation.query.SimpleCondition;
import me.wuwenbin.notepress.api.annotation.query.WrapperCondition;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;


/**
 * 所有内容发布都存在这个实体类/表中
 * created by Wuwenbin on 2019/11/19 at 3:38 下午
 *
 * @author wuwenbin
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content extends BaseEntity<Content> {

    @TableId(type = IdType.INPUT)
    @NotEmpty
    private String id;

    /**
     * 作者ID,也就是创建这的ID
     */
    private Long authorId;

    /**
     * 内容的标题
     */
    @NotEmpty(message = "标题不能为空")
    @WrapperCondition(SimpleCondition.like)
    private String title;

    /**
     * 自定义内容访问链接
     */
    private String urlSeq;

    /**
     * 发布的内容封面图
     */
    private String cover;

    /**
     * 发布的内容中包含的所有图片地址集合
     */
    @TableField(exist = false)
    private List<String> imageList;
    /**
     * 数据库字段，逗号分隔
     */
    private String images;

    /**
     * 标签
     */
    @TableField(exist = false)
    private List<Dictionary> tagList;
    @TableField(exist = false)
    private List<String> tags;

    /**
     * 分类
     */
    @TableField(exist = false)
    private List<Category> cateList;
    @TableField(exist = false)
    private List<String> categories;

    /**
     * html富文本内容
     */
    @NotEmpty(message = "内容不能为空")
    private String htmlContent;

    /**
     * 纯文本内容
     * SEO的description设置可取此字段的内容
     * 文章搜索也可like此字段
     */
    private String textContent;

    /**
     * markdown文本内容
     */
    private String mdContent;

    /**
     * 文章关键字
     * 由程序自动提取或者人工输入
     * 页面的meta标签SEO可取此属性
     */
    private String seoKeywords;

    /**
     * 文章的描述
     * 由程序自动提取或者人工输入
     * 页面的meta标签SEO可取此字段
     */
    private String seoDescription;


    /**
     * 是否显示赞赏码
     */
    private Boolean appreciable;

    /**
     * 是否为转载
     * 标记为原创还是转载,转载的话有个额外的原文链接
     */
    private Boolean reprinted;

    /**
     * 如果是转载,此字段需要填充
     */
    private String originUrl;

    /**
     * 是否为历史版本,有自动保存的功能,
     * 刷新页面之后也能够保存之前编辑的内容所以可能会有多个历史版本
     * 同时每次修改一次都会增加一个历史版本,也就是一条记录,以便以后的退回和查看历史版本
     */
    private Boolean history;

    /**
     * 内容被浏览次数
     */
    private Integer views;

    /**
     * 点赞的数量
     */
    private Integer approveCnt;

    /**
     * 是否允许评论,以全局设置为先
     * 如果全局关闭,则此字段无用
     */
    private Boolean commented;

    /**
     * 是否属于以下:
     * 置顶文章
     * 新内容分类
     * 推荐
     * 热门
     */
    @TableField("`top`")
    private Boolean top;
    private Boolean recommend;
    private Boolean hot;

    /**
     * 文章状态
     * 显示true，隐藏false
     */
    @NotNull
    private Boolean visible;
}
