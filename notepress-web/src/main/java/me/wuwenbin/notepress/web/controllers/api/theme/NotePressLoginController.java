package me.wuwenbin.notepress.web.controllers.api.theme;

import cn.hutool.cache.Cache;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;
import com.google.code.kaptcha.Constants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.wuwenbin.notepress.api.constants.CacheConstant;
import me.wuwenbin.notepress.api.constants.NotePressConstants;
import me.wuwenbin.notepress.api.constants.ParamKeyConstant;
import me.wuwenbin.notepress.api.exception.NotePressException;
import me.wuwenbin.notepress.api.model.NotePressResult;
import me.wuwenbin.notepress.api.model.entity.Param;
import me.wuwenbin.notepress.api.model.entity.Refer;
import me.wuwenbin.notepress.api.model.entity.system.SysSession;
import me.wuwenbin.notepress.api.model.entity.system.SysUser;
import me.wuwenbin.notepress.api.query.BaseQuery;
import me.wuwenbin.notepress.api.query.ParamQuery;
import me.wuwenbin.notepress.api.query.SysUserQuery;
import me.wuwenbin.notepress.api.service.IOauthService;
import me.wuwenbin.notepress.api.service.IParamService;
import me.wuwenbin.notepress.api.service.IReferService;
import me.wuwenbin.notepress.api.service.ISysUserService;
import me.wuwenbin.notepress.api.utils.NotePressIpUtils;
import me.wuwenbin.notepress.api.utils.NotePressUtils;
import me.wuwenbin.notepress.service.bo.RegisterUserBo;
import me.wuwenbin.notepress.service.facade.MailFacade;
import me.wuwenbin.notepress.service.mapper.SysSessionMapper;
import me.wuwenbin.notepress.service.utils.NotePressSessionUtils;
import me.wuwenbin.notepress.web.controllers.api.NotePressBaseController;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * created by Wuwenbin on 2019/12/4 at 1:42 下午
 *
 * @author wuwenbin
 */
@Slf4j
@Controller
@RequestMapping
public class NotePressLoginController extends NotePressBaseController {

    private static final SysSessionMapper SESSION_MAPPER = NotePressUtils.getBean(SysSessionMapper.class);
    @Autowired
    private IParamService paramService;
    @Autowired
    private IOauthService oauthService;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private IReferService referService;
    @Autowired
    private MailFacade mailFacade;
    @Qualifier("mailCodeCache")
    @Autowired
    private Cache<String, String> mailCodeCache;
    @Qualifier("kaptchaCodeCache")
    @Autowired
    private Cache<String, String> kaptchaCodeCache;

    /**
     * 跳转登录页面
     *
     * @param redirectUrl
     * @param request
     * @return
     */
    @GetMapping("/np-login")
    public ModelAndView loginPage(String redirectUrl, HttpServletRequest request) {
        request.getSession().setAttribute(NotePressConstants.SESSION_LAST_VISIT_URL_KEY, StringUtils.isEmpty(redirectUrl) ? "/" : redirectUrl);
        SysUser sessionUser = NotePressSessionUtils.getSessionUser();
        if (sessionUser != null) {
            if (StrUtil.isNotEmpty(redirectUrl)) {
                return new ModelAndView(new RedirectView(URLUtil.decode(Base64Decoder.decodeStr(redirectUrl))));
            }
            return new ModelAndView(new RedirectView("/"));
        }
        ModelAndView mav = new ModelAndView("login");
        mav.addObject("isOpenQqLogin", isOpenOauth("qq"));
        mav.addObject("isOpenGithubLogin", isOpenOauth("github"));
        mav.addObject("isOpenGiteeLogin", isOpenOauth("gitee"));
        mav.addObject("isOpenRegister", isOpenRegister());
        return mav;
    }

    /**
     * 跳转注册页面
     *
     * @return
     */
    @GetMapping("/np-register")
    public ModelAndView register() {
        if (!isOpenRegister() || NotePressSessionUtils.getSessionUser() != null) {
            return new ModelAndView(new RedirectView("/"));
        }
        request.setAttribute("isOpenRegister", isOpenRegister());
        return new ModelAndView("reg");
    }

    /**
     * 第三方账号绑定本站账号的页面
     *
     * @return
     */
    @GetMapping("/np-bind")
    public ModelAndView bind(String p) {
        if (StringUtils.isEmpty(p)) {
            return new ModelAndView(new RedirectView("/np-login"));
        }
        try {
            p = Base64.decodeStr(p);
        } catch (Exception e) {
            return new ModelAndView(new RedirectView("/np-login"));
        }
        ModelAndView mav = new ModelAndView("bind");
        //noinspection rawtypes
        Map pMap = JSONUtil.toBean(p, Map.class);
        String source = MapUtil.getStr(pMap, "source");
        String uuid = MapUtil.getStr(pMap, "uuid");
        String avatar = MapUtil.getStr(pMap, "avatar");
        String email = MapUtil.getStr(pMap, "email");
        String nickname = MapUtil.getStr(pMap, "nickname");
        String username = MapUtil.getStr(pMap, "username");
        mav.addObject("isOpenRegister", isOpenRegister());
        mav.addObject("source", source);
        mav.addObject("uuid", uuid);
        mav.addObject("avatar", avatar);
        mav.addObject("email", email);
        mav.addObject("nickname", nickname);
        mav.addObject("username", username);
        return mav;
    }


    /**
     * 提交注册
     *
     * @param userBo
     * @param bindingResult
     * @return
     */
    @PostMapping("/registration")
    @ResponseBody
    public NotePressResult doRegister(@Valid RegisterUserBo userBo, BindingResult bindingResult) {
        if (!bindingResult.hasErrors()) {
            String sessionMailCode = mailCodeCache.get(userBo.getNpMail() + "-" + CacheConstant.MAIL_CODE_KEY);
            if (userBo.getCode().equalsIgnoreCase(sessionMailCode)) {
                if (ReUtil.isMatch("[`~!@#$^&*()=|{}':;',\\[\\].<>《》/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？ ]", userBo.getNpUsername())) {
                    return writeJsonErrorMsg("用户名不能包含特殊字符！");
                }
                SysUser reqUser = SysUser.builder().username(userBo.getNpUsername()).email(userBo.getNpMail()).build();
                reqUser.setPassword(userBo.getNpPassword());
                reqUser.setNickname(userBo.getNpNickname());
                reqUser.setAvatar("/static/assets/img/noavatar.png");
                return sysUserService.doReg(reqUser);
            } else {
                return writeJsonErrorMsg("注册失败，验证码失效！");
            }
        } else {
            return writeJsonJsr303(bindingResult.getFieldErrors());
        }
    }


    /**
     * 注册时，发送的email操作
     *
     * @param email
     * @return
     */
    @PostMapping("/sendMailCode")
    @ResponseBody
    public NotePressResult sendMailCode(String email) {
        if (StrUtil.isNotEmpty(email) &&
                ReUtil.isMatch("^[A-Za-z\\d]+([-_.][A-Za-z\\d]+)*@([A-Za-z\\d]+[-.])+[A-Za-z\\d]{2,4}$", email)) {
            int emailCnt = sysUserService.count(SysUserQuery.build("email", email));
            if (emailCnt == 0) {
                mailFacade.sendMailCode(email);
                return NotePressResult.createOkMsg("发送成功，请至邮箱查收！");
            } else {
                return NotePressResult.createErrorMsg("邮箱已被注册！");
            }
        } else {
            return NotePressResult.createErrorMsg("邮箱格式有误！");
        }
    }


    /**
     * 第三方登录/注册
     *
     * @param type
     * @param response
     * @throws IOException
     */
    @GetMapping("/api/login/{type}")
    public void thirdLoginPage(@PathVariable String type, HttpServletResponse response) throws IOException {
        NotePressResult authRequestResult = oauthService.getAuthRequest(type);
        if (authRequestResult.isSuccess()) {
            AuthRequest authRequest = authRequestResult.getDataBean(AuthRequest.class);
            response.sendRedirect(authRequest.authorize(AuthStateUtils.createState()));
        } else {
            throw new NotePressException(authRequestResult.getMsg());
        }
    }

    /**
     * 第三方登录注册callback
     *
     * @param type
     * @param callback
     * @param httpResponse
     * @throws IOException
     */
    @SneakyThrows
    @GetMapping("/api/{type}Callback")
    public void doThirdLogin(@PathVariable String type, AuthCallback callback, HttpServletResponse httpResponse) throws IOException {
        //请求登录结果
        NotePressResult authRequestResult = oauthService.getAuthRequest(type);
        //登录失败，抛出错误
        if (!authRequestResult.isSuccess()) {
            throw new NotePressException(authRequestResult.getMsg());
        }

        AuthRequest authRequest = authRequestResult.getDataBean(AuthRequest.class);
        //返回登录结果
        //noinspection rawtypes
        AuthResponse response = authRequest.login(callback);
        //授权失败，抛出错误
        if (!response.ok()) {
            throw new NotePressException(response.getMsg());
        }

        AuthUser authUser = (AuthUser) response.getData();
        //检测是否绑定了本站账号
        NotePressResult br = referService.hasBind(authUser.getSource(), authUser.getUuid());
        //么有板顶或绑定错误，抛出错误
        if (!br.isSuccess()) {
            throw new NotePressException(br.getMsg());
        }

        //绑定了就直接登录，设置相关 session 对象
        if (br.getData() != null && !br.getBoolData()) {
            Refer referUser = br.getDataBean(Refer.class);
            String userId = referUser.getReferId();
            //根据用户 id 查询本站的账号信息，并设置相关 session
            SysUser sessionUser = sysUserService.getById(userId);
            String lastVisitUrl = setSessionReturnLastVisitUrl(sessionUser, null);
            lastVisitUrl = StrUtil.isEmpty(lastVisitUrl) ? Base64Encoder.encode("/") : lastVisitUrl;
            long cnt = SESSION_MAPPER.selectCount(BaseQuery.build("session_user_id", sessionUser.getId()));
            if (cnt == 0) {
                SESSION_MAPPER.insert(SysSession.user(sessionUser));
            }
            removeSessionLastVisitUrl();
            httpResponse.sendRedirect(lastVisitUrl);
        }
        //如果没绑定，提示绑定并转发至绑定页面
        else {
            String avatar = authUser.getAvatar();
            String source = authUser.getSource();
            String uuid = authUser.getUuid();
            String nickname = authUser.getNickname();
            String email = authUser.getEmail();
            String username = authUser.getUsername();
            log.info("==> 授权用户：{}", JSONUtil.toJsonPrettyStr(authUser));
            Map<String, Object> pMap = new HashMap<>(3);
            pMap.put("avatar", avatar);
            pMap.put("source", source);
            pMap.put("uuid", uuid);
            pMap.put("nickname", nickname);
            pMap.put("email", email);
            pMap.put("username", username);
            httpResponse.sendRedirect("/np-bind?p=" + Base64.encode(JSONUtil.toJsonStr(pMap)));
        }
    }


    /**
     * 第三方登录的账号绑定本站账号的操作
     *
     * @param username
     * @param password
     * @param source
     * @param uuid
     * @return
     */
    @PostMapping("/bind")
    @ResponseBody
    public NotePressResult bind(String username, String password, String nickname, String email,
                                String source, String uuid, String avatar, @RequestParam String code) {
        String googleCode = kaptchaCodeCache.get(Constants.KAPTCHA_SESSION_KEY);
        kaptchaCodeCache.clear();
        if (code == null) {
            return NotePressResult.createErrorMsg("请输入验证码");
        }
        if (!code.equalsIgnoreCase(googleCode)) {
            return NotePressResult.createErrorMsg("验证码不匹配，请刷新页面后重试！");
        }
        if (ReUtil.isMatch("[`~!@#$^&*()=|{}':;',\\[\\].<>《》/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？ ]", username)) {
            return writeJsonErrorMsg("用户名不能包含特殊字符！");
        }
        SysUser reqUser = SysUser.builder()
                .username(username).email(email)
                .password(password).nickname(nickname)
                .avatar(avatar.replace("http://", "https://")).build();
        NotePressResult notePressResult = sysUserService.doReg(reqUser);
        if (!notePressResult.isSuccess()) {
            return notePressResult;
        }

        //能到绑定页面的说明都是没有注册过的
        //检测用户名和密码是否正确
        NotePressResult loginResult = sysUserService.doLogin(username, password, NotePressIpUtils.getRemoteAddress(request));
        if (!loginResult.isSuccess()) {
            return loginResult;
        }
        Long userId = loginResult.getDataBean(SysUser.class).getId();
        //开始执行绑定
        NotePressResult isSavedR = referService.bind(userId, uuid, source, HtmlUtil.unescape(HtmlUtil.unescape(avatar)));
        if (!isSavedR.isSuccess()) {
            return NotePressResult.createErrorFormatMsg("绑定失败：{}", isSavedR.getMsg());
        } else {
            //绑定成功跳转至登录前的最后访问的页面
            Object lastVisitUrl = session.getAttribute(SESSION_LAST_VISIT_URL_KEY);
            removeSessionLastVisitUrl();
            return NotePressResult.createOkMsg("绑定成功")
                    .addExtra("url",
                            URLUtil.decode(ObjectUtil.isNotEmpty(lastVisitUrl) ? URLUtil.decode(Base64Decoder.decodeStr(lastVisitUrl.toString())) : "/"));
        }

    }


    //================================私有方法=====================================

    /**
     * 是否开放注册
     *
     * @return
     */
    private boolean isOpenRegister() {
        NotePressResult serverConfigResult = paramService.fetchMailServer();
        if (serverConfigResult.isSuccess()) {
            NotePressResult userRegR = paramService.fetchParamByName(ParamKeyConstant.SWITCH_USER_REG);
            return userRegR.isSuccess() && "1".contentEquals(userRegR.getDataBean(Param.class).getValue());
        }
        return false;
    }

    /**
     * 是否开放第三方登录
     *
     * @return
     */
    private boolean isOpenOauth(String type) {
        NotePressResult qqR = oauthService.getAuthRequest(type);
        if (qqR.isSuccess()) {
            if ("qq".contentEquals(type)) {
                Param qqParam = paramService.getOne(ParamQuery.build(ParamKeyConstant.SWITCH_QQ_LOGIN));
                log.info("==> qq login open status:" + JSONUtil.toJsonStr(qqParam));
                return qqParam != null && "1".contentEquals(qqParam.getValue());
            } else if ("github".contentEquals(type)) {
                Param githubParam = paramService.getOne(ParamQuery.build(ParamKeyConstant.SWITCH_GITHUB_LOGIN));
                log.info("==> github login open status:" + JSONUtil.toJsonStr(githubParam));
                return githubParam != null && "1".contentEquals(githubParam.getValue());
            } else if ("gitee".contentEquals(type)) {
                Param giteeParam = paramService.getOne(ParamQuery.build(ParamKeyConstant.SWITCH_GITEE_LOGIN));
                log.info("==> gitee login open status:" + JSONUtil.toJsonStr(giteeParam));
                return giteeParam != null && "1".contentEquals(giteeParam.getValue());
            }
        }
        return false;
    }


}
