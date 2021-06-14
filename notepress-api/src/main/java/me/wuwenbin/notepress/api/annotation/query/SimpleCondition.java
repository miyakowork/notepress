package me.wuwenbin.notepress.api.annotation.query;

/**
 * created by Wuwenbin on 2019/12/2 at 2:23 下午
 *
 * @author wuwenbin
 */
public enum SimpleCondition {
    /**
     * sql 的条件判断逻辑
     * 此处仅逻辑简单的，更复杂的此处不作处理
     */
    eq,
    ne,
    gt,
    gte,
    lt,
    lte,
    like,
    noteLike,
    likeLeft,
    likeRight,
    isNull,
    isNotNull;


}
