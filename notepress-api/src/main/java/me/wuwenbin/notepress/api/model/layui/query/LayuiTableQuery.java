package me.wuwenbin.notepress.api.model.layui.query;

import cn.hutool.json.JSONObject;
import lombok.Data;
import me.wuwenbin.notepress.api.model.page.SortOrder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * @author wuwen
 */
@Data
public class LayuiTableQuery<T> implements Serializable {

    private Integer page = 1;
    private Integer limit = 10;
    private String sort;
    private String order;

    /**
     * 优先此字段，如果此字段不为空
     */
    private List<SortOrder> sortOrders;
    private T extra;

    private JSONObject otherParams;

}
