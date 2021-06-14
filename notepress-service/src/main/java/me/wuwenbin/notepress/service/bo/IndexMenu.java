package me.wuwenbin.notepress.service.bo;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import cn.hutool.setting.Setting;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wuwenbin
 */
@Data
@Builder
public class IndexMenu implements Serializable {

    static String[] level1 = new String[]{
            "console,管理控制台,layui-icon-console",
            "system,系统管理,layui-icon-engine",
            "settings,网站服务设置,layui-icon-set",
            "contents,内容管理,layui-icon-read"
    };
    static String[] level2 = new String[]{
            "console,dashboard,控制台,/",
            "system,user,用户管理,system/user",
            "system,oauth,第三方登录设置,system/oauth",
            "settings,info,个人资料,settings/info",
            "settings,website,偏好设置,settings/website",
            "settings,wxpay,微信付款码管理,settings/wxpay",
            "settings,server,其他服务设置,settings/server",
            "contents,dict,字典管理,contents/dict",
            "contents,notice,消息中心,contents/notice",
            "contents,category,分类管理,contents/category",
            "contents,rescate,资源分类管理,contents/rescate",
            "contents,res,资源管理,contents/res",
            "contents,content,内容管理,contents/content",
    };
    private String name;
    private String title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String icon;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String jump;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<IndexMenu> list;

    private static IndexMenu m(String name, String title, String icon, String jump) {
        IndexMenu menu = IndexMenu.builder().name(name).title(title).build();
        if (StrUtil.isNotEmpty(jump)) {
            menu.setJump(jump);
        }
        if (StrUtil.isNotEmpty(icon)) {
            menu.setIcon(icon);
        }
        return menu;
    }

    private static List<IndexMenu> defaultThemeMenu(String themeName) {
        List<IndexMenu> res = new ArrayList<>(3);
        for (String lv1 : level1) {
            String[] menu1 = lv1.split(",");
            //一级菜单
            IndexMenu m = m(menu1[0], menu1[1], menu1[2], "");
            for (String lv2 : level2) {
                String[] m2 = lv2.split(",");
                String group = m2[0];
                if (group.equals(menu1[0])) {
                    //二级菜单
                    IndexMenu menu2 = m(m2[1], m2[2], "", m2[3]);
                    m.addLevel2Menu(menu2);
                }
            }
            res.add(m);
        }
        //如果某些主题有额外的管理菜单，则在此添加
        if (StrUtil.isNotEmpty(themeName)) {
            IndexMenu themeMenu = m("theme", "主题设置", "layui-icon-theme", "");

            String[] themeLv2 = getThemeMenuByThemeName(themeName);
            for (String lv2 : themeLv2) {
                String[] m2 = lv2.split(",");
                IndexMenu menu2 = m(m2[1], m2[2], "", m2[3]);
                themeMenu.addLevel2Menu(menu2);
            }
            res.add(themeMenu);
        }
        return res;
    }

    private void addLevel2Menu(IndexMenu m) {
        if (CollectionUtils.isEmpty(this.list)) {
            this.list = new ArrayList<>(1);
        }
        this.list.add(m);
    }

    private static String[] getThemeMenuByThemeName(String themeName) {
        Setting settings = NotePressUtils.getThemeSetting(themeName);
        String indexMenuArray = settings.getStr("indexMenu");
        JSONArray jsonArray = JSONUtil.parseArray(indexMenuArray);
        List<String> res = new ArrayList<>(jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {
            String s = jsonArray.getStr(i);
            res.add(s);
        }
        return ArrayUtil.toArray(res, String.class);
    }


    /**
     * 根据主题名称（id）获取对应的后台管理菜，没有即默认，为空即可
     *
     * @param themeId
     * @return
     */
    public static List<IndexMenu> fetchMenuByTheme(String themeId) {
        return defaultThemeMenu(themeId);
    }
}
