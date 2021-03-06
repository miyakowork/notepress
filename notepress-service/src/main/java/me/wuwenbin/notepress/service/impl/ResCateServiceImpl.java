package me.wuwenbin.notepress.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.ResCate;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.service.IResCateService;
import me.wuwenbin.notepress.service.mapper.ResCateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wuwen
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ResCateServiceImpl extends ServiceImpl<ResCateMapper, ResCate> implements IResCateService {

    @Autowired
    private ResCateMapper categoryMapper;

    @Override
    public NotePressResult findCategoryList(IPage<ResCate> categoryPage, LayuiTableQuery<ResCate> layuiTableQuery) {
        return findLayuiTableList(categoryMapper, categoryPage, layuiTableQuery);
    }

    @Override
    public NotePressResult findCateTree() {
        List<ResCate> resCateList = categoryMapper.selectList(Wrappers.<ResCate>query().isNull("pid"));
        List<Map<String, Object>> resCateMapList = resCateList.stream().map(this::resCate2TreeMap).collect(Collectors.toList());
        for (Map<String, Object> resCateMap : resCateMapList) {
            List<ResCate> sonResCateList = categoryMapper.selectList(Wrappers.<ResCate>query().eq("pid", MapUtil.getStr(resCateMap, "id")));
            if (sonResCateList.size() > 0) {
                resCateMap.putIfAbsent("children", sonResCateList.stream().map(this::resCate2TreeMap).collect(Collectors.toList()));
            }
        }
        return NotePressResult.createOkData(resCateMapList);
    }


    //----------------------------------------------????????????-----------------------------------------------

    /**
     * bean???map
     *
     * @param resCate
     * @return
     */
    private Map<String, Object> resCate2TreeMap(ResCate resCate) {
        Map<String, Object> resCateMap = MapUtil.of("title", resCate.getName());
        resCateMap.putIfAbsent("id", resCate.getId());
        resCateMap.putIfAbsent("field", "cId");
        resCateMap.putIfAbsent("spread", true);
        return resCateMap;
    }
}
