package me.wuwenbin.notepress.api.service;

import me.wuwenbin.notepress.api.model.entity.Hide;
import me.wuwenbin.notepress.api.service.base.INotePressService;

/**
 * @author wuwenbin
 */
public interface IHideService extends INotePressService<Hide> {

    /**
     * 是否购买的
     *
     * @param articleId
     * @param userId
     * @param hideId
     * @return
     */
    boolean userIsPurchased(String articleId, long userId, String hideId);


    /**
     * 用户购买隐藏内容
     *
     * @param contentId
     * @param hideId
     * @param userId
     * @param price
     * @return
     */
    int purchaseContentHideContent(String contentId, String hideId, Long userId,double price);
}
