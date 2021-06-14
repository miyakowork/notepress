package me.wuwenbin.notepress.api.query;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.entity.Refer;

/**
 * @author wuwenbin
 */
public class ReferQuery extends BaseQuery {

    public static QueryWrapper<Refer> buildBySelfIdAndType(String selfId, ReferTypeEnum referTypeEnum) {
        return Wrappers.<Refer>query()
                .eq("self_id", selfId)
                .eq("refer_type", referTypeEnum.getValue());
    }

    public static QueryWrapper<Refer> buildByUserAndRes(Long userId, String resId) {
        return Wrappers.<Refer>query()
                .eq("self_id", userId)
                .eq("refer_id", resId)
                .eq("refer_type", ReferTypeEnum.USER_RES.getValue());
    }

    public static QueryWrapper<Refer> buildByUser(String userId) {
        return Wrappers.<Refer>query()
                .eq("refer_id", userId)
                .eq("refer_type", ReferTypeEnum.THIRD_USER);
    }

    public static QueryWrapper<Refer> buildDeleteBySelfId(String selfId) {
        return Wrappers.<Refer>query().eq("self_id", selfId);
    }

    public static QueryWrapper<Refer> buildCountByReferId(String referId, ReferTypeEnum referTypeEnum) {
        return Wrappers.<Refer>query()
                .eq("refer_id", referId).eq("refer_type", referTypeEnum.getValue());
    }
}
