package me.wuwenbin.notepress.web.controllers.api.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Category;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.ICategoryService;
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
import java.util.stream.Collectors;

/**
 * @author wuwenbin
 */
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AdminCategoryController extends NotePressBaseController {

    private final ICategoryService categoryService;

    @PostMapping
    public NotePressResult resCateList(Page<Category> categoryPage,
                                       @RequestBody LayuiTableQuery<Category> layuiTableQuery) {
        return writeJsonLayuiTable(categoryService.findCategoryList(categoryPage, layuiTableQuery));
    }

    @GetMapping("/list")
    public NotePressResult list(String xmSelect) {
        List<Category> categories = categoryService.list();
        if (StringUtils.isEmpty(xmSelect)) {
            return writeJsonOk(categories);
        } else {
            List<Map<String, Object>> cates = categories.stream().map(c -> {
                Map<String, Object> cc = new HashMap<>(4);
                cc.put("name", c.getNickname());
                cc.put("value", c.getId());
                cc.put("selected", "");
                cc.put("disabled", "");
                return cc;
            }).collect(Collectors.toList());

            return writeJsonOk(cates);
        }
    }


    @PostMapping("/update")
    public NotePressResult updateCategory(@NotNull Category category) {
        category.setUpdateBy(NotePressSessionUtils.getSessionUser().getId());
        category.setGmtUpdate(LocalDateTime.now());
        boolean res = categoryService.updateById(category);
        return writeJsonJudgedBool(res, "修改成功", "修改失败");
    }

    @PostMapping("/add")
    public NotePressResult addCategory(Category category) {
        if (category.getStatus() == null) {
            category.setStatus(true);
        }
        if (category.getOrderIndex() == null) {
            category.setOrderIndex(0);
        }
        category.setGmtCreate(LocalDateTime.now());
        category.setCreateBy(NotePressSessionUtils.getSessionUser().getId());
        boolean res = categoryService.save(category);
        return writeJsonJudgedBool(res, "添加成功", "添加失败");
    }
}
