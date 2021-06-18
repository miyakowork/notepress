package me.wuwenbin.notepress.web.controllers.api.admin.plugin.pay;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.qrcode.QrCodeUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.wuwenbin.notepress.api.constants.enums.plugin.pay.PayType;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.plugin.pay.PayQrCode;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.query.BaseQuery;
import me.wuwenbin.notepress.api.service.plugin.pay.IWxpayQrCodeService;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author wuwenbin
 */
@RestController
public class AdminWxpayQrCodeController extends NotePressBaseController {

    @Autowired
    private IWxpayQrCodeService wxpayQrCodeService;

    @PostMapping("/admin/wxpay")
    public NotePressResult qrCodeList(Page<PayQrCode> categoryPage,
                                      @RequestBody LayuiTableQuery<PayQrCode> layuiTableQuery) {
        return writeJsonLayuiTable(wxpayQrCodeService.findQrCodeList(categoryPage, layuiTableQuery));
    }


    @PostMapping("/admin/wxpay/update")
    public NotePressResult updateQrCode(@NotNull PayQrCode payQrCode) {
        int c = wxpayQrCodeService.count(BaseQuery.build("qr_price", payQrCode.getQrPrice()));
        if (c == 0) {
            payQrCode.setUpdateBy(Objects.requireNonNull(NotePressSessionUtils.getSessionUser()).getId());
            payQrCode.setGmtUpdate(LocalDateTime.now());
            boolean res = wxpayQrCodeService.updateById(payQrCode);
            return writeJsonJudgedBool(res, "修改成功", "修改失败");
        } else {
            return writeJsonErrorMsg("已存在此数目的金额，请使用其他金额！");
        }
    }

    @PostMapping("/admin/wxpay/add")
    public NotePressResult addQrCode(PayQrCode payQrCode) {
        payQrCode.setQrType(PayType.Wxpay);
        payQrCode.setGmtCreate(LocalDateTime.now());
        payQrCode.setCreateBy(Objects.requireNonNull(NotePressSessionUtils.getSessionUser()).getId());
        payQrCode.setId(IdUtil.objectId());
        boolean res = wxpayQrCodeService.save(payQrCode);
        return writeJsonJudgedBool(res, "添加成功", "添加失败");
    }

    @PostMapping("/admin/wxpay/delete")
    public NotePressResult deleteQrCode(String id) {
        boolean res = wxpayQrCodeService.removeById(id);
        return writeJsonJudgedBool(res, "删除成功！", "删除失败！");
    }

    @GetMapping(value = "/wxpay/testQrCode", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] testQrCode(String url) throws IOException {
        byte[] bytes;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage bi = QrCodeUtil.generate(url, 300, 300);
        ImageIO.write(bi, "png", baos);
        bytes = baos.toByteArray();
        baos.close();
        return bytes;
    }
}
