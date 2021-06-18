package me.wuwenbin.notepress.web.controllers.api.common;

import cn.hutool.cache.Cache;
import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.annotation.JwtIgnore;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * created by Wuwenbin on 2018/7/31 at 20:43
 *
 * @author wuwenbin
 */
@Slf4j
@Controller
public class KaptchaController extends NotePressBaseController {

    @Qualifier("kaptchaCodeCache")
    @Autowired
    private Cache<String, String> kaptchaCodeCache;

    @GetMapping("/image/code")
    @JwtIgnore
    public void kaptcha(HttpServletResponse response) throws IOException {
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");

        Producer captchaProducer = NotePressUtils.getBean(Producer.class);
        String capText = captchaProducer.createText();
        kaptchaCodeCache.put(Constants.KAPTCHA_SESSION_KEY, capText);

        log.info("输出验证码：[{}]", capText);

        BufferedImage bi = captchaProducer.createImage(capText);
        ServletOutputStream out = response.getOutputStream();
        ImageIO.write(bi, "jpg", out);
        out.flush();
        out.close();
    }
}
