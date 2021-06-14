package me.wuwenbin.notepress.api.model.query;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author wuwenbin
 */
@Data
@Builder
public class ContentPageQuery implements Serializable {

    /**
     * 多个参数以.间隔
     */
    private String cates;
    private String excludeCates;
    private String tags;
    private String contentIds;

    /**
     * 以下不能以.间隔普通传参
     */
    private String title;
    private String words;
    private SearchType searchType;


    public enum SearchType {
        /**
         * 四种搜索标签类型
         */
        HOT("hot"),
        RECOMMEND("recommend"),
        ALL(""),
        NONE("");

        @Getter
        @Setter
        private String name;

        SearchType(String s) {
            this.name = s;
        }

    }
}
