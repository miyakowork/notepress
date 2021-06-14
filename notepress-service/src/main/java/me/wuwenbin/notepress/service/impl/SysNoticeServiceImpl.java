package me.wuwenbin.notepress.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.constants.enums.DictionaryTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Dictionary;
import me.wuwenbin.notepress.api.model.entity.system.SysNotice;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.model.page.SortOrder;
import me.wuwenbin.notepress.api.query.DictionaryQuery;
import me.wuwenbin.notepress.api.query.SysNoticeQuery;
import me.wuwenbin.notepress.api.service.ISysNoticeService;
import me.wuwenbin.notepress.api.utils.NotePressIpUtils;
import me.wuwenbin.notepress.api.utils.NotePressRequestUtils;
import me.wuwenbin.notepress.api.utils.NotePressServletUtils;
import me.wuwenbin.notepress.service.mapper.DictionaryMapper;
import me.wuwenbin.notepress.service.mapper.SysNoticeMapper;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author wuwen
 */
@Service
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

    private final SysNoticeMapper sysNoticeMapper;
    private final DictionaryMapper dictionaryMapper;

    @Override
    public NotePressResult findNoticeList(IPage<SysNotice> noticePage, LayuiTableQuery<SysNotice> layuiTableQuery) {
        SortOrder so1 = SortOrder.me("asc", "is_read");
        SortOrder so2 = SortOrder.me("desc", "gmt_create");
        layuiTableQuery.setSortOrders(SortOrder.our(so1, so2));
        return findLayuiTableList(sysNoticeMapper, noticePage, layuiTableQuery);
    }

    @Override
    public NotePressResult findNoticeTypes() {
        List<Map<String, Object>> result = new ArrayList<>(4);
        String tabSql = "select distinct page_type from np_sys_notice";
        List<String> tabs = sysNoticeMapper.findListPrimitiveByArray(tabSql, String.class);
        tabs.add(0, "全部消息");
        for (String tab : tabs) {
            Map<String, Object> map = new HashMap<>(4);
            map.put("tabName", tab);
            if ("全部消息".equals(tab)) {
                String tipSql = "select count(1) from np_sys_notice where is_read = 0 ";
                int tip = sysNoticeMapper.queryNumberByArray(tipSql, Integer.class);
                map.put("tabTip", tip);
            } else {
                String tipSql = "select count(1) from np_sys_notice where is_read = 0 and page_type = ?";
                int tip = sysNoticeMapper.queryNumberByArray(tipSql, Integer.class, tab);
                map.put("tabTip", tip);
            }
            result.add(map);
        }
        return NotePressResult.createOkData(result);
    }


    @Override
    public NotePressResult findMessageRankList() {
        List<Map<String, Object>> rankList = sysNoticeMapper.findRankListByDate(10);
        if (CollectionUtil.isEmpty(rankList) || rankList.size() < 4) {
            List<Map<String, Object>> rl = sysNoticeMapper.findRankList();
            rl.forEach(rlMap -> {
                Long userId = MapUtil.getLong(rlMap, "user_id");
                List<SysNotice> latestNotices = sysNoticeMapper.selectList(SysNoticeQuery.build("user_id", userId));
                rlMap.put("gmt_create", latestNotices.get(0).getGmtCreate());
            });
            return NotePressResult.createOkData(rl);
        }
        rankList.forEach(rlMap -> {
            Long userId = MapUtil.getLong(rlMap, "user_id");
            List<SysNotice> latestNotices = sysNoticeMapper.selectList(SysNoticeQuery.build("user_id", userId));
            rlMap.put("gmt_create", latestNotices.get(0).getGmtCreate());
        });
        return NotePressResult.createOkData(rankList);
    }

    @Override
    public NotePressResult subMessage(SysNotice notice) {
        HttpServletRequest request = NotePressServletUtils.getRequest();
        notice.setIpAddr(NotePressIpUtils.getRemoteAddress(request));
        notice.setIpInfo(NotePressIpUtils.getIpInfo(notice.getIpAddr()).getAddress());
        notice.setUserAgent(request.getHeader("user-agent"));
        notice.setCommentText(HtmlUtil.cleanHtmlTag(notice.getCommentHtml()));
        notice.setCommentHtml(
                HtmlUtil.removeHtmlTag(
                        NotePressRequestUtils.stripSqlXss(notice.getCommentHtml()), false, "style", "link", "meta", "script"));
        notice.setGmtCreate(LocalDateTime.now());
        List<Dictionary> keywords = dictionaryMapper.selectList(DictionaryQuery.build("dictionary_type", DictionaryTypeEnum.SENSITIVE_WORD));
        keywords.forEach(
                kw -> notice.setCommentHtml(notice.getCommentHtml().replace(kw.getDictValue(), StrUtil.repeat("*", kw.getDictValue().length()))));
        notice.setGmtCreate(LocalDateTime.now());
        notice.setCreateBy(Objects.requireNonNull(NotePressSessionUtils.getSessionUser()).getId());
        int floor = Convert.toInt(sysNoticeMapper.findMaxFloorByContentId(notice.getContentId()), 0);
        notice.setFloor(floor + 1);
        return NotePressResult.createOkData(sysNoticeMapper.insert(notice));
    }

    @Override
    public NotePressResult findMessagePage(Page<SysNotice> messagePage, String contentId, String pageType) {
        IPage<SysNotice> noticePage = sysNoticeMapper.selectPage(messagePage, SysNoticeQuery.<SysNotice>buildNotEmpty("content_id", contentId)
                .eq("status", true).eq(StrUtil.isNotEmpty(pageType), "page_type", pageType));
        return NotePressResult.createOkData(noticePage);
    }

}
