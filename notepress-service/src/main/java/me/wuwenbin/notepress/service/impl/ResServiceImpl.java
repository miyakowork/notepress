package me.wuwenbin.notepress.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.constants.enums.ReferTypeEnum;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Deal;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.Refer;
import me.wuwenbin.notepress.api.model.entity.Res;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.layui.LayuiTable;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.model.page.NotePressPage;
import me.wuwenbin.notepress.api.query.ReferQuery;
import me.wuwenbin.notepress.api.service.IResService;
import me.wuwenbin.notepress.api.utils.NotePressIdUtils;
import me.wuwenbin.notepress.service.mapper.*;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.service.utils.NotePressUploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wuwen
 */
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class ResServiceImpl extends ServiceImpl<ResMapper, Res> implements IResService {

    @Autowired
    private ResMapper resMapper;
    @Autowired
    private ResCateMapper resCateMapper;
    @Autowired
    private ReferMapper referMapper;
    @Autowired
    private ParamMapper paramMapper;
    @Autowired
    private DealMapper dealMapper;

    @Override
    public NotePressResult findResList(IPage<Res> resPage, LayuiTableQuery<Res> layuiTableQuery) {
        NotePressResult r = findLayuiTableList(resMapper, resPage, layuiTableQuery);
        if (r.isSuccess()) {
            //noinspection rawtypes
            LayuiTable p = r.getDataBean(LayuiTable.class);
            if (p != null) {
                if (p.getData() != null && p.getData().size() > 0) {
                    List<Map<String, Object>> beanMapList = new ArrayList<>(p.getData().size());
                    for (Object o : p.getData()) {
                        Map<String, Object> beanMap = BeanUtil.beanToMap(o);
                        List<String> cateNameList = referMapper.findRefersByTypeAndSelfId(beanMap.get("id").toString(), ReferTypeEnum.RES_RESCATE)
                                .stream().map(refer -> resCateMapper.selectById(refer.getReferId()).getName()).collect(Collectors.toList());
                        beanMap.putIfAbsent("cateNames", cateNameList);
                        beanMapList.add(beanMap);
                    }
                    //noinspection unchecked
                    p.setData(beanMapList);
                    return NotePressResult.createOkData(p);
                }
            }
            return NotePressResult.createErrorMsg("?????????");
        }
        return NotePressResult.createErrorMsg("???????????????");
    }

    @Override
    public NotePressResult uploadRes(MultipartFile multipartFile, Res resFile, List<String> cateIds) {
        Res res;
        if (multipartFile != null) {
            res = this.doQiniuUpload(multipartFile, resFile);
        } else {
            res = this.doThirdUpload(resFile);
        }
        if (res != null) {
            int r = resMapper.insert(res);
            if (r == 1) {
                cateIds.forEach(cateId -> referMapper.insert(Refer.builder().selfId(res.getId()).referId(cateId).referType(ReferTypeEnum.RES_RESCATE).build()));
                return NotePressResult.createOkData(res);
            }
        }
        return NotePressResult.createErrorMsg("???????????????");
    }


    @Override
    public NotePressResult deleteRes(String id) {
        Configuration cfg = new Configuration(Region.region0());
        BucketManager bucketManager = new BucketManager(NotePressUploadUtils.getQiniuAuth(), cfg);
        try {
            Res res = resMapper.selectById(id);
            if (res == null) {
                throw new NotePressException("??????????????????");
            }
            Response qiniuRes = bucketManager.delete(NotePressUploadUtils.getBucketName(), res.getResHash());
            if (qiniuRes.isOK()) {
                int c = resMapper.deleteById(res.getId());
                if (c > 0) {
                    referMapper.delete(ReferQuery.buildBySelfIdAndType(res.getId(), ReferTypeEnum.RES_RESCATE));
                    return NotePressResult.createOkMsg("???????????????");
                }
            }
            return NotePressResult.createErrorMsg("???????????????");
        } catch (QiniuException ex) {
            //???????????????????????????????????????
            throw new NotePressException(NotePressErrorCode.QiniuError, ex.response.toString());
        }
    }

    @Override
    public NotePressResult findResList(NotePressPage<Res> resPage, String cateId, String resName) {
        String sql = "SELECT * FROM np_res WHERE id in ";
        if (StrUtil.isNotEmpty(cateId)) {
            sql += "(SELECT self_id FROM np_refer WHERE refer_id = ? and refer_type = 'res_rescate')";
            if (StrUtil.isNotEmpty(resName)) {
                sql += " AND (res_hash LIKE ? OR remark LIKE ?)";
                String resNameParam = "%" + resName + "%";
                resPage = resMapper.findPageListBeanByArray(sql, Res.class, resPage, cateId, resNameParam, resNameParam);
            } else {
                resPage = resMapper.findPageListBeanByArray(sql, Res.class, resPage, cateId);
            }
        } else {
            sql += "(SELECT self_id FROM np_refer WHERE refer_type = 'res_rescate')";
            if (StrUtil.isNotEmpty(resName)) {
                sql += " AND (res_hash LIKE ? OR remark LIKE ?)";
                String resNameParam = "%" + resName + "%";
                resPage = resMapper.findPageListBeanByArray(sql, Res.class, resPage, resNameParam, resNameParam);
            } else {
                resPage = resMapper.findPageListBeanByArray(sql, Res.class, resPage);
            }
        }
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();

        List<Res> newResList = resPage.getTResult().stream().peek(res -> {
            if (StrUtil.isNotEmpty(res.getAuthCode())) {
                if (sessionUser == null) {
                    res.setAuthCode(StrUtil.repeat("*", 4));
                } else {
                    int cnt = referMapper.selectCount(ReferQuery.buildByUserAndRes(sessionUser.getId(), res.getId()));
                    if (!sessionUser.getAdmin() && cnt == 0) {
                        res.setAuthCode(StrUtil.repeat("*", 4));
                    }
                }
            }
        }).collect(Collectors.toList());
        resPage.setResult(newResList);
        resPage.setTResult(newResList);
        resPage.setRawResult(newResList);

        return NotePressResult.createOkData(resPage);
    }

    @Override
    public NotePressResult purchaseRes(List<String> resIds) {
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        if (sessionUser != null) {
            try {
                int sumCoin = resMapper.selectSumCoinByIds(resIds);
                int userCoin = dealMapper.selectSumCoinByIds(sessionUser.getId());
                if (userCoin >= sumCoin) {
                    resIds.stream().map(id -> Refer.builder()
                            .referType(ReferTypeEnum.USER_RES).selfId(sessionUser.getId().toString()).referId(id).build()
                            .gmtCreate(LocalDateTime.now()).createBy(sessionUser.getId())).forEach(referMapper::insert);
                    resIds.stream().map(resMapper::selectById)
                            .forEach(res -> dealMapper.insert(
                                    Deal.builder().
                                            dealAmount(-res.getCoin()).userId(sessionUser.getId()).dealTargetId(res.getId())
                                            .build()));
                    return NotePressResult.createOkMsg("???????????????");
                } else {
                    return NotePressResult.createErrorMsg("????????????????????????????????????????????????????????????????????????????????????????????????");
                }
            } catch (Exception e) {
                return NotePressResult.createErrorMsg("????????????????????????" + e.getMessage());
            }
        }
        return NotePressResult.createErrorMsg("???????????????");
    }

    //============================????????????==============================

    /**
     * ?????????????????????upload??????
     *
     * @param file ??????
     * @return Response
     */
    private Res doQiniuUpload(MultipartFile file, Res resFile) {
        try {
            String fileName = file.getOriginalFilename();
            //?????????????????????Zone??????????????????
            Configuration cfg = new Configuration(Region.autoRegion());
            String extend = Objects.requireNonNull(fileName).substring(fileName.lastIndexOf("."));
            //??????put????????????
            String fileHashKey = fileName;
            int c = resMapper.selectCount(Wrappers.<Res>query().eq("res_hash", fileName));
            if (c > 0) {
                fileHashKey = fileName.substring(0, fileName.lastIndexOf(".")) + System.currentTimeMillis() + extend;
            }
            Response res = new UploadManager(cfg).put(file.getBytes(), fileHashKey, NotePressUploadUtils.getQiniuUpToken(fileHashKey));
            log.info("[??????????????????] - [{}] - ???????????????{}", res.isOK(), res.bodyString());
            if (res.isOK()) {
                JSONObject respObj = JSONUtil.parseObj(res.bodyString());
                String generateFileName = respObj.getStr("key");
                String qiniuDomain = paramMapper.selectOne(Wrappers.<Param>query().eq("name", ParamKeyConstant.QINIU_DOMAIN)).getValue();
                String src = qiniuDomain + "/" + generateFileName;

                //?????????????????????
                return newUpload(generateFileName, src, file.getSize(), resFile);
            } else {
                throw new NotePressException("==> ??????????????????????????????????????????" + res.error);
            }
        } catch (QiniuException e) {
            Response re = e.response;
            log.error("==> [??????????????????] - [{}] - ???????????????{}", re.isOK(), re.toString());
            try {
                log.error("==> ???????????????????????????{}", re.bodyString());
            } catch (QiniuException ignored) {
            }
            throw new NotePressException(NotePressErrorCode.QiniuError, e.getMessage());
        } catch (Exception ex) {
            log.error("==> ??????IO??????????????????????????????{}", ex.getMessage());
            throw new NotePressException(NotePressErrorCode.InternalServerError, ex.getMessage());
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????
     *
     * @param resFile
     * @return
     */
    private Res doThirdUpload(Res resFile) {
        String fileName = resFile.getResHash();
        int c = resMapper.selectCount(Wrappers.<Res>query().eq("res_hash", fileName));
        if (c > 0) {
            fileName = System.currentTimeMillis() + fileName;
        }
        return newUpload(fileName, resFile.getResUrl(), -1, resFile);
    }

    //=============================??????????????????=======================

    /**
     * ???????????????????????????
     *
     * @param fileHash
     * @param fileUrl
     * @param fileByteSize
     * @param resFile
     * @return
     */
    private Res newUpload(String fileHash, String fileUrl, double fileByteSize, Res resFile) {
        Res res = Res.builder().id(NotePressIdUtils.nextId()).resHash(fileHash)
                .resFsizeBytes(fileByteSize).resUrl(fileUrl).coin(resFile.getCoin())
                .resIntroUrl(resFile.getResIntroUrl()).authCode(resFile.getAuthCode())
                .build();
        //?????????????????????
        if (StringUtils.isEmpty(resFile.getRemark())) {
            res.setRemark(resFile.getRemark());
        }
        //?????????????????????
        Long userId = Objects.requireNonNull(NotePressSessionUtils.getSessionUser()).getId();
        return res.createBy(userId).gmtCreate(LocalDateTime.now()).remark(resFile.getRemark());
    }
}
