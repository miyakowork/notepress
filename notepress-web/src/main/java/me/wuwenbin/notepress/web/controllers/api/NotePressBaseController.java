package me.wuwenbin.notepress.web.controllers.api;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.constants.NotePressConstants;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.model.layui.LayuiTable;
import me.wuwenbin.notepress.api.utils.NotePressServletUtils;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.utils.NotePressWebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * created by Wuwenbin on 2019/11/8 at 9:50 上午
 *
 * @author wuwenbin
 */
@Slf4j
public class NotePressBaseController implements NotePressConstants {

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    protected HttpSession session;


    /**
     * 基路径
     *
     * @param request
     * @return
     */
    protected static String basePath(HttpServletRequest request) {
        return NotePressWebUtils.basePath(request);
    }

    /**
     * 返回值类型为Map<String, Object>
     *
     * @param properties
     * @return
     */
    protected static Map<String, Object> getParameterMap(Map<String, String[]> properties) {
        return NotePressWebUtils.getParameterMap(properties);
    }

    /**
     * jsr303验证处理的错误信息
     *
     * @param fieldErrors
     * @return
     */
    protected static NotePressResult writeJsonJsr303(List<FieldError> fieldErrors) {
        StringBuilder message = new StringBuilder();
        for (FieldError error : fieldErrors) {
            message.append(error.getField()).append(":").append(error.getDefaultMessage()).append("<br/>");
        }
        return NotePressResult.createErrorMsg(message.toString());
    }

    protected static <T> T writeJsonOk(NotePressResult response, Class<T> clazz) {
        return response.getDataBean(clazz);
    }

    protected static Object writeJsonOk(NotePressResult response) {
        return response.getData();
    }

    protected static NotePressResult writeJsonOk(Object o) {
        return NotePressResult.createOkData(o);
    }

    protected static NotePressResult writeJsonOkMsg(String msg) {
        return NotePressResult.createOkMsg(msg);
    }

    protected static void writeJson(HttpServletResponse response, NotePressResult notePressResult) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(notePressResult));
    }

    protected static void writeJsonError(HttpServletResponse response, String message) throws IOException {
        NotePressResult notePressResult = NotePressResult.createErrorMsg(message);
        writeJson(response, notePressResult);
    }

    protected static NotePressResult writeJsonErrorMsg(String errMsg, Object... msgParam) {
        return NotePressResult.createErrorMsg(StrUtil.format(errMsg, msgParam));
    }

    protected static void writeJsonOk(HttpServletResponse response, String successMsg) throws IOException {
        NotePressResult notePressResult = NotePressResult.createOkMsg(successMsg);
        writeJson(response, notePressResult);
    }

    /**
     * 返回LayuiTable的数据
     *
     * @param result
     * @param <T>
     * @return
     */
    protected static <T> NotePressResult writeJsonLayuiTable(NotePressResult result) {
        //noinspection unchecked
        LayuiTable<T> layuiTable = result.getDataBean(LayuiTable.class);
        if (!result.isSuccess()) {
            result.put("msg", result.getMsg());
            result.put("count", 0);
            result.put("data", Collections.emptyList());
        } else {
            result.put("msg", layuiTable.getMsg());
            result.put("count", layuiTable.getCount());
            result.put("data", layuiTable.getData());
        }
        return result;
    }

    /**
     * 提供给哪些service层返回NotePressResult的方法，并且处理他们可能会抛出的异常
     *
     * @param resultSupplier
     * @return
     */
    protected static NotePressResult writeJson(Supplier<NotePressResult> resultSupplier) {
        try {
            return resultSupplier.get();
        } catch (Exception e) {
            return NotePressResult.createError(NotePressErrorCode.ControllerError, e.getMessage());
        }
    }

    /**
     * 根据null返回不同的结果
     *
     * @param obj
     * @param successMsg
     * @param errMsg
     * @return
     */
    protected static NotePressResult writeJsonJudgedNull(Object obj, String successMsg, String errMsg) {
        if (ObjectUtil.isNotNull(obj)) {
            return NotePressResult.createOk(StringUtils.isEmpty(successMsg) ? "操作成功！" : successMsg, obj);
        } else {
            return NotePressResult.createErrorMsg(errMsg);
        }
    }

    /**
     * 根据empty返回不同的结果
     *
     * @param obj
     * @param successMsg
     * @param errMsg
     * @return
     */
    protected static NotePressResult writeJsonJudgedEmpty(Object obj, String successMsg, String errMsg) {
        if (ObjectUtil.isNotEmpty(obj)) {
            return NotePressResult.createOk(StringUtils.isEmpty(successMsg) ? "操作成功！" : successMsg, obj);
        } else {
            return NotePressResult.createErrorMsg(errMsg);
        }
    }

    /**
     * 根据布尔值判断
     *
     * @param obj
     * @param successMsg
     * @param errMsg
     * @return
     */
    protected static NotePressResult writeJsonJudgedBool(boolean obj, String successMsg, String errMsg) {
        if (obj) {
            return NotePressResult.createOkMsg(StringUtils.isEmpty(successMsg) ? "操作成功！" : successMsg);
        } else {
            return NotePressResult.createErrorMsg(errMsg);
        }
    }

    /**
     * 对于返回参数的键值不敏感的结果可以使用此方法
     * 此方法将自动生成map，其键值为key1、key2依次自动递增
     *
     * @param objects
     * @return
     */
    protected static NotePressResult writeJsonMap(Object... objects) {
        Map<String, Object> map = new HashMap<>(4);
        for (int i = 0; i < objects.length; i++) {
            map.put("key" + (i + 1), objects[i]);
        }
        return writeJsonOk(map);
    }

    /**
     * result转bean的判断
     *
     * @param result
     * @param <T>
     * @return
     */
    protected static <T> List<T> toListBeanNull(NotePressResult result) {
        if (result.isSuccess()) {
            return result.getDataListBean();
        } else {
            return null;
        }
    }

    protected static <T> Page<T> toPageBeanNull(NotePressResult result) {
        if (result.isSuccess()) {
            //noinspection unchecked
            return result.getDataBean(Page.class);
        } else {
            return null;
        }
    }

    protected static <T> T toBeanNull(NotePressResult result, Class<T> clazz) {
        if (result.isSuccess()) {
            return result.getDataBean(clazz);
        } else {
            return null;
        }
    }

    protected static <T, R> R toRNull(NotePressResult result, Class<T> clazz, Function<T, R> applyTo) {
        if (result.isSuccess()) {
            return applyTo.apply(result.getDataBean(clazz));
        } else {
            return null;
        }
    }


    /**
     * 设置访问的url
     *
     * @param sessionUser
     * @return
     */
    protected String setSessionReturnLastVisitUrl(SysUser sessionUser, String jwtToken) {
        NotePressSessionUtils.setSessionUser(sessionUser, jwtToken);
        //获取登录之前最后一次访问的页面 URL
        String lastVisitUrl = (String) session.getAttribute(SESSION_LAST_VISIT_URL_KEY);
        lastVisitUrl = StrUtil.isEmpty(lastVisitUrl) ? "/" : lastVisitUrl;
        return URLUtil.decode(Base64Decoder.decodeStr(lastVisitUrl));
    }

    /**
     * 移除访问的url
     */
    protected void removeSessionLastVisitUrl() {
        session.removeAttribute(SESSION_LAST_VISIT_URL_KEY);
    }

    /**
     * 判断是否为ajax请求
     *
     * @param request
     * @return
     */
    protected boolean isAjaxRequest(HttpServletRequest request) {
        return NotePressServletUtils.isAjaxRequest(request);
    }

    /**
     * 是否为 json 请求
     *
     * @param request
     * @return
     */
    protected boolean isJson(HttpServletRequest request) {
        String headerAccept = request.getHeader("Accept");
        return !isEmpty(headerAccept) && headerAccept.contains("application/json");
    }

    /**
     * 是否为get请求
     *
     * @param request
     * @return
     */
    protected boolean isGetRequest(HttpServletRequest request) {
        String method = request.getMethod();
        return "GET".equalsIgnoreCase(method);
    }

    /**
     * 判断是不是请求后台的链接
     *
     * @param request
     * @return
     */
    protected boolean isAdminRequest(HttpServletRequest request) {
        return request.getRequestURI().contains(PREFIX_ADMIN_URL.concat("/"));
    }

    /**
     * 判断是否为ajax请求
     *
     * @param request
     * @return
     */
    protected boolean isRouter(HttpServletRequest request) {
        String headerAccept = request.getHeader("Accept");
        return !isEmpty(headerAccept) && headerAccept.contains("text/html") && !isJson(request) && isAjaxRequest(request) && isGetRequest(request);
    }

    protected <T> void ifTrueDo(boolean res, Consumer<T> consumer, T t) {
        if (res) {
            consumer.accept(t);
        }
    }
}
