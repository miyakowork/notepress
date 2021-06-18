package me.wuwenbin.notepress.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Refer;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.service.IReferService;
import me.wuwenbin.notepress.service.mapper.ReferMapper;
import me.wuwenbin.notepress.service.mapper.SysUserMapper;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * created by Wuwenbin on 2019/11/28 at 2:24 下午
 *
 * @author wuwenbin
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ReferServiceImpl extends ServiceImpl<ReferMapper, Refer> implements IReferService {

    @Autowired
    private ReferMapper referMapper;
    @Autowired
    private SysUserMapper userMapper;

    @Override
    public NotePressResult hasBind(String source, String uuid) {
        String findIsBindSql = "select count(1) from np_refer where refer_type = 'third_user' and self_id = ? and json_extract(refer_extra,'$.source') = ?;";
        int cnt = referMapper.queryNumberByArray(findIsBindSql, Integer.class, uuid, source);
        if (cnt == 0) {
            return NotePressResult.createOk(StrUtil.format("【{}】类型登录未绑定本站账号，可以操作！", source), true);
        } else {
            Refer refer = referMapper.findUserReferBySourceAndUuid(source, uuid);
            return NotePressResult.createOk(StrUtil.format("此帐号【{}】类型登录已被绑定，请更换之后再做操作！", source), refer);
        }
    }

    @Override
    public NotePressResult bind(long userId, String uuid, String source, String avatar) {
        //检测本站账号绑定
        String findIsBindSql = "select count(1) from np_refer where refer_type = 'third_user' and refer_id = ? and json_extract(refer_extra,'$.source') = ?;";
        int cnt = referMapper.queryNumberByArray(findIsBindSql, Integer.class, userId, source);
        //如果本站账号还未绑定任何第三方
        if (cnt == 0) {
            //检测第三方账号绑定情况
            NotePressResult s = hasBind(source, uuid);
            //如果第三方账号也未绑定任何本站账号，即双向未绑定，则开始处理绑定
            if (s.isSuccess() && Boolean.TRUE.equals(s.getBoolData())) {
                SysUser sessionUser = NotePressSessionUtils.getSessionUser();
                int referCnt = referMapper.insertThirdUser(uuid, String.valueOf(userId),
                        genReferExtra(ReferTypeEnum.THIRD_USER, source),
                        LocalDateTime.now(), sessionUser != null ? sessionUser.getId() : null);
                if (referCnt == 1) {
                    userMapper.updateById(SysUser.builder().id(userId).avatar(avatar).build());
                    return NotePressResult.createOk("绑定成功！", true);
                } else {
                    return NotePressResult.createErrorMsg("绑定失败！");
                }
            } else {
                return NotePressResult.createErrorMsg(s.getMsg());
            }
        } else {
            return NotePressResult.createErrorMsg("账号已被绑定，请勿重复绑定！");
        }
    }
}
