package me.wuwenbin.notepress.api.constants.enums.plugin.pay;


import com.baomidou.mybatisplus.core.enums.IEnum;
import me.wuwenbin.notepress.api.exception.NotePressException;

/**
 * @author wuwen
 */

public enum PayType implements IEnum<String> {
    /**
     * 支付类型
     * 二维码类型
     * 订单支付类型
     */
    Alipay,

    Wxpay;

    public static PayType getType(String typeStr) {
        if ("alipay".contentEquals(typeStr)) {
            return PayType.Alipay;
        } else if ("wxpay".contentEquals(typeStr)) {
            return PayType.Wxpay;
        } else {
            throw new NotePressException("不支持的支付类型！");
        }
    }


    /**
     * 枚举数据库存储值
     */
    @Override
    public String getValue() {
        return this.name().toLowerCase();
    }
}
