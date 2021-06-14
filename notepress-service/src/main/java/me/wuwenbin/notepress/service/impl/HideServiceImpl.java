package me.wuwenbin.notepress.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.wuwenbin.notepress.api.model.entity.Hide;
import me.wuwenbin.notepress.api.service.IHideService;
import me.wuwenbin.notepress.service.mapper.HideMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author wuwenbin
 */
@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Transactional(rollbackFor = Exception.class)
public class HideServiceImpl extends ServiceImpl<HideMapper, Hide> implements IHideService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 是否购买的
     *
     * @param contentId
     * @param userId
     * @param hideId
     * @return
     */
    @Override
    public boolean userIsPurchased(String contentId, long userId, String hideId) {
        String dealTargetId = contentId + "," + hideId;
        String sql = "select count(1) from np_deal where deal_target_id = ? and user_id = ?";
        String c = jdbcTemplate.queryForObject(sql, String.class, dealTargetId, userId);
        return c != null && Integer.parseInt(c) == 1;
    }

    /**
     * 用户购买隐藏内容
     *
     * @param contentId
     * @param hideId
     * @param userId
     * @param price
     * @return
     */
    @Override
    public int purchaseContentHideContent(String contentId, String hideId, Long userId, double price) {
        String dealTargetId = contentId + "," + hideId;
        String sql = "insert into np_deal(user_id,deal_target_id,deal_amount,gmt_create) VALUES (?,?,?,now())";
        return jdbcTemplate.update(sql, userId, dealTargetId, (0 - price));
    }
}
