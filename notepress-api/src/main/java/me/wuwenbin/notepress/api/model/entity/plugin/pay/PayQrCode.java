package me.wuwenbin.notepress.api.model.entity.plugin.pay;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import me.wuwenbin.notepress.api.constants.enums.plugin.pay.PayType;
import me.wuwenbin.notepress.api.model.entity.base.BaseEntity;

import javax.validation.constraints.NotEmpty;

/**
 * @author wuwen
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("np_pay_qrcode")
public class PayQrCode extends BaseEntity<PayQrCode> {

    @TableId(type = IdType.INPUT)
    private String id;
    @NotEmpty
    private PayType qrType;
    @NotEmpty
    private String qrUrl;
    @NotEmpty
    private Double qrPrice;

}
