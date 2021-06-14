package me.wuwenbin.notepress.service.impl.plugin.pay;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.plugin.pay.PayQrCode;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.plugin.pay.IWxpayQrCodeService;
import me.wuwenbin.notepress.service.mapper.plugin.pay.IPayQrCodeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wuwen
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class WxpayQrcodeServiceImpl extends ServiceImpl<IPayQrCodeMapper, PayQrCode> implements IWxpayQrCodeService {

    private final IPayQrCodeMapper payQrCodeMapper;

    @Override
    public NotePressResult findQrCodeList(IPage<PayQrCode> categoryPage, LayuiTableQuery<PayQrCode> layuiTableQuery) {
        return findLayuiTableList(payQrCodeMapper, categoryPage, layuiTableQuery);
    }

    /**
     * 查找一共有多少种金额
     *
     * @return
     */
    @Override
    public NotePressResult findWxPrices() {
        List<Object> prices = payQrCodeMapper.selectObjs(Wrappers.<PayQrCode>query().select("round(qr_price)").groupBy("round(qr_price)"));
        return NotePressResult.createOkData(prices);
    }
}
