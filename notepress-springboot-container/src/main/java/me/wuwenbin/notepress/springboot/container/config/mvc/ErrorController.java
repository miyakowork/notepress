package me.wuwenbin.notepress.springboot.container.config.mvc;

import cn.hutool.core.map.MapUtil;
import me.wuwenbin.notepress.api.exception.NotePressErrorCode;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

import static org.springframework.http.MediaType.*;

/**
 * 全局的异常处理
 * created by Wuwenbin on 2018/7/17 at 17:03
 *
 * @author wuwenbin
 */
@Controller
@RequestMapping("/error")
public class ErrorController extends NotePressBaseController implements org.springframework.boot.web.servlet.error.ErrorController {

    private static final String ERROR_PAGE = "templates/def/error/page";
    private static final String ERROR_ROUTER = "templates/def/error/router";

    private ErrorAttributes errorAttributes;
    private ServerProperties serverProperties;


    public ErrorController(ErrorAttributes errorAttributes, ServerProperties serverProperties) {
        Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
        this.errorAttributes = errorAttributes;
        this.serverProperties = serverProperties;
    }

    @Override
    public String getErrorPath() {
        return ERROR_PAGE;
    }

    /**
     * 非json的错误请求处理
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(produces = TEXT_HTML_VALUE)
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        Map<String, Object> model = Collections.synchronizedMap(getErrorAttributes(request, isIncludeStackTrace(request)));
        response.setStatus(status.value());
        request.setAttribute("status", status.value());
        request.setAttribute("errorCode", request.getAttribute("errorCode"));
        if (isRouter(request)) {
            return new ModelAndView(ERROR_ROUTER, model);
        }
        return new ModelAndView(ERROR_PAGE, model);
    }

    /**
     * json的请求错误处理
     *
     * @param request
     * @return
     */
    @RequestMapping(produces = {
            APPLICATION_JSON_VALUE,
            APPLICATION_FORM_URLENCODED_VALUE,
            MULTIPART_FORM_DATA_VALUE
    })
    @ResponseBody
    public NotePressResult error(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request));
        String message = body.get("message").toString();
        Integer errorCode = (Integer) request.getAttribute("errorCode");
        if (errorCode != null) {
            NotePressErrorCode notePressErrorCode = NotePressErrorCode.getErrorCodeEnumByCode(errorCode);
            if (notePressErrorCode != null) {
                return NotePressResult.createError(notePressErrorCode);
            }
        }
        return NotePressResult.createErrorMsg(message);
    }


    private HttpStatus getStatus(HttpServletRequest request) {
        String code = request.getParameter("errorCode");
        Integer statusCode = code == null ? null : Integer.valueOf(code);
        if (statusCode == null) {
            statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        }
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request, boolean includeStackTrace) {
        WebRequest requestAttributes = new ServletWebRequest(request);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }

    private boolean isIncludeStackTrace(HttpServletRequest request) {
        ErrorProperties.IncludeStacktrace include = this.serverProperties.getError().getIncludeStacktrace();
        return include == ErrorProperties.IncludeStacktrace.ALWAYS || include == ErrorProperties.IncludeStacktrace.ON_TRACE_PARAM && getTraceParameter(request);
    }

    private boolean getTraceParameter(HttpServletRequest request) {
        String parameter = request.getParameter("trace");
        return parameter != null && !"false".equals(parameter.toLowerCase());
    }

}
