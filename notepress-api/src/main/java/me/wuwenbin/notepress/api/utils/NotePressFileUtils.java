package me.wuwenbin.notepress.api.utils;

import me.wuwenbin.notepress.api.model.Base64MultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

/**
 * created by Wuwenbin on 2019/11/25 at 5:12 下午
 *
 * @author wuwenbin
 */
public class NotePressFileUtils {

    /**
     * base64转multipart file
     *
     * @param base64
     * @return
     */
    public static MultipartFile base64ToMultipartFile(String base64) {
        String[] baseStr = base64.split(",");
        byte[] b;
        b = Base64.getDecoder().decode(baseStr[1]);
        for (int i = 0; i < b.length; ++i) {
            if (b[i] < 0) {
                b[i] += 256;
            }
        }
        return new Base64MultipartFile(b, baseStr[0]);
    }
}
