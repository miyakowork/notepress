package me.wuwenbin.notepress.api.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Res;
import me.wuwenbin.notepress.api.model.layui.query.LayuiTableQuery;
import me.wuwenbin.notepress.api.model.page.NotePressPage;
import me.wuwenbin.notepress.api.service.base.INotePressService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author wuwen
 */
public interface IResService extends INotePressService<Res> {


    /**
     * 资源数据
     *
     * @param categoryPage
     * @param layuiTableQuery
     * @return
     */
    NotePressResult findResList(IPage<Res> categoryPage, LayuiTableQuery<Res> layuiTableQuery);


    /**
     * 上传资源到七牛云
     *
     * @param multipartFile
     * @param cateIds
     * @return
     */
    NotePressResult uploadRes(MultipartFile multipartFile, Res res, List<String> cateIds);

    /**
     * 删除资源，七牛云的也一起删除
     *
     * @param id
     * @return
     */
    NotePressResult deleteRes(String id);

    /**
     * 根据条件查询对应的资源列表
     *
     * @param resPage
     * @param cateId
     * @param resName
     * @return
     */
    NotePressResult findResList(NotePressPage<Res> resPage, String cateId, String resName);

    /**
     * 购买资源
     *
     * @param resIds
     * @return
     */
    NotePressResult purchaseRes(List<String> resIds);

}
