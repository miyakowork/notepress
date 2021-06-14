package me.wuwenbin.notepress.api.model.page;

import java.io.Serializable;

/**
 * created by Wuwenbin on 2019/11/18 at 2:52 下午
 *
 * @author wuwenbin
 */
public class NotePressOrder implements Serializable {


    /**
     * 排序字段
     */
    protected String orderField;

    /**
     * 排序方式
     */
    protected String orderDirection;


    /**
     * getters and setters
     */

    public String getOrderField() {
        return orderField;
    }

    public void setOrderField(String orderField) {
        this.orderField = orderField;
    }

    public String getOrderDirection() {
        return orderDirection;
    }

    public void setOrderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
    }

}
