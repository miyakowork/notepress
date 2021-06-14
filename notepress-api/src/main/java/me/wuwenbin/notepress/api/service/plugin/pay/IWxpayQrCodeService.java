package me.wuwenbin.notepress.api.service.plugin.pay;

import com.baomidou.mybatisplus.core.metadata.IPage;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.plugin.pay.PayQrCode;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.base.INotePressService;

/**
 * @author wuwen
 */
public interface IWxpayQrCodeService extends INotePressService<PayQrCode> {


    /**
     * 分类数据
     *
     * @param qrcodePage
     * @param layuiTableQuery
     * @return
     */
    NotePressResult findQrCodeList(IPage<PayQrCode> qrcodePage, LayuiTableQuery<PayQrCode> layuiTableQuery);

    /**
     * 查找一共有多少种金额
     *
     * @return
     */
    NotePressResult findWxPrices();

}
