package me.wuwenbin.notepress.web.controllers.api.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.enums.DictionaryTypeEnum;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Dictionary;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.service.IDictionaryService;
import me.wuwenbin.notepress.api.service.IReferService;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wuwen
 */
@RestController
@RequestMapping("/admin/dictionary")
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class AdminDictionaryController extends NotePressBaseController {

    private final IDictionaryService dictionaryService;
    private final IReferService referService;

    @GetMapping("/list")
    public NotePressResult dictionaryList() {
        List<Map<String, Object>> dictionaries = new ArrayList<>(4);
        for (DictionaryTypeEnum dictionaryTypeEnum : DictionaryTypeEnum.values()) {
            Map<String, Object> map = new HashMap<>(2);
            map.put("k", dictionaryTypeEnum.getValue());
            map.put("v", dictionaryTypeEnum.getLabel());
            dictionaries.add(map);
        }
        return writeJsonOk(dictionaries);
    }

    @GetMapping("/{dictType}")
    public NotePressResult dictType(@PathVariable("dictType") String dictType, String search) {
        List<Dictionary> dictionaries = dictionaryService.list(
                Wrappers.<Dictionary>query()
                        .eq("dict_label", DictionaryTypeEnum.valueOf(dictType).getLabel())
                        .like(StrUtil.isNotEmpty(search), "dict_value", search)
                        .eq("status", 1)
                        .eq("dictionary_type", DictionaryTypeEnum.valueOf(dictType)));
        return writeJsonOk(dictionaries);
    }

    @PostMapping("/update")
    public NotePressResult updateDict(@Valid @NotNull Dictionary dictionary, BindingResult result) {
        if (!result.hasErrors()) {
            return dictionaryService.updateDictionaryById(dictionary);
        }
        return writeJsonJsr303(result.getFieldErrors());
    }


    @PostMapping("/add")
    public NotePressResult addDict(@Valid Dictionary dictionary, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            return dictionaryService.addDictionary(dictionary);
        }
        return writeJsonJsr303(bindingResult.getFieldErrors());
    }

    @PostMapping("/delete")
    public NotePressResult deleteDict(String id) {
        int cnt = referService.count(ReferQuery.buildCountByReferId(id, ReferTypeEnum.CONTENT_TAG));
        if (cnt > 0) {
            return writeJsonErrorMsg("请先删除关联的内容在做此项删除！");
        }
        boolean res = dictionaryService.removeById(id);
        return writeJsonJudgedBool(res, "删除成功！", " 删除失败！");
    }

}
