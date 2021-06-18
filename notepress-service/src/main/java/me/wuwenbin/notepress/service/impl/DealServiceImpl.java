package me.wuwenbin.notepress.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Deal;
import me.wuwenbin.notepress.api.service.IDealService;
import me.wuwenbin.notepress.service.mapper.DealMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author wuwen
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DealServiceImpl extends ServiceImpl<DealMapper, Deal> implements IDealService {

    @Autowired
    private DealMapper dealMapper;

    @Override
    public NotePressResult findCoinSumByUserId(Long userId) {
        String sql = "select sum(deal_amount) from np_deal where user_id = ?";
        int c = dealMapper.queryNumberByArray(sql, Integer.class, userId);
        return NotePressResult.createOkData(c);
    }

    /**
     * 充值硬币
     *
     * @param coin
     * @param remark
     * @return
     */
    @Override
    public NotePressResult rechargeCoin(long userId, long optUserId, int coin, String remark) {
        int c = dealMapper.insert(
                Deal.builder()
                        .dealAmount(coin).userId(userId)
                        .build().gmtCreate(LocalDateTime.now())
                        .createBy(userId)
                        .remark(remark));
        return NotePressResult.createOkData(c);
    }
}
