package me.wuwenbin.notepress.api.model;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.exception.NotePressException;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 网络传输对象
 * created by Wuwenbin on 2017/12/20 at 下午3:39
 *
 * @author wuwenbin
 */
public class NotePressResult extends ConcurrentHashMap<String, Object> {

    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String DATA = "data";

    public static final int SUCCESS = 200;
    public static final String SUCCESS_MESSAGE = "操作成功！";

    private NotePressResult() {
    }

    public static NotePressResult create(int code, String msg, Object data) {
        NotePressResult notePressResult = new NotePressResult();
        notePressResult.put(CODE, code);
        notePressResult.put(MESSAGE, msg);
        if (data != null) {
            notePressResult.put(DATA, data);
        }
        return notePressResult;
    }


    public static NotePressResult createOk() {
        return create(SUCCESS, SUCCESS_MESSAGE, null);
    }

    public static NotePressResult createOkMsg(String message) {
        return create(SUCCESS, message, null);
    }

    public static NotePressResult createOkData(Object data) {
        return create(SUCCESS, SUCCESS_MESSAGE, data);
    }

    public static NotePressResult createOk(String message, Object data) {
        return create(SUCCESS, message, data);
    }


    public static NotePressResult createError(NotePressErrorCode errorCode) {
        return create(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static NotePressResult createErrorMsg(String message) {
        return create(NotePressErrorCode.NormalError.getCode(), message, null);
    }

    public static NotePressResult createErrorFormatMsg(String message, Object... msgParam) {
        return createErrorMsg(StrUtil.format(message, msgParam));
    }

    public static NotePressResult createError(NotePressErrorCode errorCode, String message) {
        return create(errorCode.getCode(), message, null);
    }

    public static NotePressResult createError(NotePressErrorCode errorCode, String message, Object data) {
        return create(errorCode.getCode(), message, data);
    }

    public static NotePressResult createError(NotePressErrorCode errorCode, Object data) {
        return create(errorCode.getCode(), errorCode.getMessage(), data);
    }

    public static NotePressResult createError(NotePressException e) {
        NotePressErrorCode ece = NotePressErrorCode.getErrorCodeEnumByCode(e.getErrorCode());
        if (ece != null) {
            return createError(ece);
        } else {
            return create(e.getErrorCode(), e.getMessage(), null);
        }
    }

    public static NotePressResult createErrorData(Object data) {
        return create(NotePressErrorCode.NormalError.getCode(), NotePressErrorCode.NormalError.getMessage(), data);
    }


    /**
     * 添加额外的返回对象
     *
     * @param extraMap
     * @return
     */
    public NotePressResult addExtra(Map<String, Object> extraMap) {
        this.putAll(extraMap);
        return this;
    }

    public NotePressResult addExtra(String extraKey, Object extraObj) {
        this.put(extraKey, extraObj);
        return this;
    }

    public boolean isSuccess() {
        return MapUtil.getInt(this, CODE) == SUCCESS;
    }

    public Integer getIntData() {
        return MapUtil.getInt(this, DATA);
    }

    public String getStrData() {
        return MapUtil.getStr(this, DATA);
    }

    public Long getLongData() {
        return MapUtil.getLong(this, DATA);
    }

    public Double getDoubleData() {
        return MapUtil.getDouble(this, DATA);
    }

    public char getCharData() {
        return MapUtil.getChar(this, DATA);
    }

    public Boolean getBoolData() {
        return MapUtil.getBool(this, DATA);
    }

    public Float getFloatData() {
        return MapUtil.getFloat(this, DATA);
    }

    public Short getShortData() {
        return MapUtil.getShort(this, DATA);
    }

    public Object getData() {
        return this.get(DATA);
    }

    public <T> T getDataBean(Class<T> clazz) {
        return MapUtil.get(this, DATA, clazz);
    }

    public <T> List<T> getDataListBean() {
        //noinspection unchecked
        return MapUtil.get(this, DATA, List.class);
    }

    public Boolean getDataBool() {
        return Convert.toBool(getData());
    }

    public String getDataStr() {
        return Convert.toStr(getData());
    }

    public Integer getDataInt() {
        return Convert.toInt(getData());
    }

    public Long getDataLong() {
        return Convert.toLong(getData());
    }

    public Double getDataDouble() {
        return Convert.toDouble(getData());
    }

    public Float getDataFloat() {
        return Convert.toFloat(getData());
    }

    public Byte getDataByte() {
        return Convert.toByte(getData());
    }

    public char getDataChar() {
        return Convert.toChar(getData());
    }

    public BigDecimal getDataBigDecimal() {
        return Convert.toBigDecimal(getData());
    }

    public Date getDataDate() {
        return Convert.toDate(getData());
    }

    public Short getDataShort() {
        return Convert.toShort(getData());
    }

    public <K, V> Map<K, V> getDataMap(Class<K> keyClass, Class<V> valueClass) {
        return Convert.toMap(keyClass, valueClass, getData());
    }

    public String getMsg() {
        return MapUtil.getStr(this, MESSAGE);
    }

    public void setMsg(String message) {
        this.put(MESSAGE, message);
    }

    public Integer getCode() {
        return MapUtil.getInt(this, CODE);
    }

}
