package me.wuwenbin.notepress.web.controllers.api.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.ResCate;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.IResCateService;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wuwenbin
 */
@RestController
@RequestMapping("/admin/rescate")
public class AdminResCateController extends NotePressBaseController {

    @Autowired
    private IResCateService categoryService;

    @PostMapping
    public NotePressResult resCateList(Page<ResCate> categoryPage,
                                       @RequestBody LayuiTableQuery<ResCate> layuiTableQuery) {
        return writeJsonLayuiTable(categoryService.findCategoryList(categoryPage, layuiTableQuery));
    }

    @GetMapping("/list")
    public NotePressResult list(String xmSelect) {
        List<ResCate> categories = categoryService.list();
        if (StringUtils.isEmpty(xmSelect)) {
            return writeJsonOk(categories);
        } else {
            List<Map<String, Object>> cates = categories.stream().map(c -> {
                Map<String, Object> cc = new HashMap<>(4);
                cc.put("name", c.getName());
                cc.put("value", c.getId());
                cc.put("selected", "");
                cc.put("disabled", "");
                return cc;
            }).collect(Collectors.toList());

            return writeJsonOk(cates);
        }
    }


    @PostMapping("/update")
    public NotePressResult updateCategory(@NotNull ResCate category) {
        category.setUpdateBy(Objects.requireNonNull(NotePressSessionUtils.getSessionUser()).getId());
        category.setGmtUpdate(LocalDateTime.now());
        boolean res = categoryService.updateById(category);
        return writeJsonJudgedBool(res, "????????????", "????????????");
    }

    @PostMapping("/add")
    public NotePressResult addCategory(ResCate category) {
        if (category.getOrderIndex() == null) {
            category.setOrderIndex(0);
        }
        category.setGmtCreate(LocalDateTime.now());
        category.setCreateBy(Objects.requireNonNull(NotePressSessionUtils.getSessionUser()).getId());
        boolean res = categoryService.save(category);
        return writeJsonJudgedBool(res, "????????????", "????????????");
    }
}
