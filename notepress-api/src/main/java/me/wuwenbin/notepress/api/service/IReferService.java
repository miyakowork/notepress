package me.wuwenbin.notepress.api.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Refer;
import me.wuwenbin.notepress.api.service.base.INotePressService;

/**
 * created by Wuwenbin on 2019/11/28 at 2:20 下午
 *
 * @author wuwenbin
 */
public interface IReferService extends INotePressService<Refer> {

    /**
     * 根据不同的type生成对应的refer_extra字段值
     *
     * @param referTypeEnum
     * @param obj
     * @return
     */
    default String genReferExtra(ReferTypeEnum referTypeEnum, Object... obj) {
        if (obj == null || obj.length == 0) {
            throw new NotePressException("参数不能为空！");
        }
        if (referTypeEnum == ReferTypeEnum.THIRD_USER) {
            return JSONUtil.toJsonStr(MapUtil.of("source", obj[0]));
        }
        return "{}";
    }


    /**
     * 是否已绑定本站账号
     *
     * @param source
     * @param uuid
     * @return
     */
    NotePressResult hasBind(String source, String uuid);

    /**
     * 第三方账号与本站账号的绑定
     *
     * @param userId
     * @param uuid
     * @param source
     * @param avatar
     * @return
     */
    NotePressResult bind(long userId, String uuid, String source,String avatar);
}
