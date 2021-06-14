package me.wuwenbin.notepress.api.model.layui;

import com.baomidou.mybatisplus.core.metadata.IPage;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.model.page.NotePressPage;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * layui2.x版本推出的dataTable插件,与LayTable的区别在于msg字段。推荐使用此类
 * created by Wuwenbin on 2017/8/30 at 11:41
 *
 * @author wuwenbin
 */
public class LayuiTable<T> implements Serializable {

    /**
     * 状态码，0代表成功，其它失败
     */
    private int code;
    /**
     * 状态信息，一般可为空
     */
    private String msg;
    /**
     * 数据总量
     */
    private long count;
    /**
     * 数据，字段是任意的。如：[{"id":1,"username":"贤心"}, {"id":2,"username":"佟丽娅"}]
     */
    private List<T> data;

    public LayuiTable(int code, String msg, long count, List<T> data) {
        this.code = code;
        this.msg = msg;
        this.count = count;
        this.data = data;
    }

    public LayuiTable(long count, List<T> data) {
        this.code = 0;
        this.msg = "获取数据成功！";
        this.count = count;
        this.data = data;
    }

    public static <T> LayuiTable<T> success(NotePressPage<T> notePressPage) {
        return new LayuiTable<>(notePressPage.getTotalCount(), notePressPage.getTResult());
    }

    public static <T> LayuiTable<T> success(IPage<T> page) {
        return new LayuiTable<>(page.getTotal(), page.getRecords());
    }

    public static <T> LayuiTable<T> error(NotePressErrorCode codeEnum, String message) {
        return new LayuiTable<>(codeEnum.getCode(), message, 0, Collections.emptyList());
    }

    public static <T> LayuiTable<T> error(Exception e) {
        return new LayuiTable<>(NotePressErrorCode.NormalError.getCode(), e.getMessage(), 0, Collections.emptyList());
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

}
