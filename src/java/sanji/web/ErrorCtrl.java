/**
 * @(#)ErrorCtrl.java, 2012-10-26. 
 * 
 * Copyleft 2013 leo(leoyonn@gmail.com). All lefts released. 
 *  */
package sanji.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import sanji.utils.Logger;
import toolbox.web.CookieUtil;

/**
 * 处理发生错误时的页面<br>
 * 给出错误页面和对应的消息描述
 * 
 * @author leo
 */
public final class ErrorCtrl extends MultiActionController {
    private static final String VIEW_NAME = "error";
    private static final String COOKIE_ERROR_MSG_NAME = "errormsg";
    private static final int COOKIE_ERROR_MSG_AGE = 1;

    public ModelAndView onError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String msg = getErrorMsg(request, response);
        String type = request.getParameter("type");
        if (StringUtils.isEmpty(msg)) {
            if (type.equals("400")) {
                msg = "你已取消操作或操作过程中遇到了点问题，请重试";
            } else if (type.equals("403")) {
                msg = "非常抱歉，系统禁止了你的此次操作，请从反馈入口将此问反馈";
            } else if (type.equals("500")) {
                msg = "啊哦，你戳到了系统的痛点，我们会尽快修复，饭饭对给你带来的不便深感抱歉";
            } else {
                msg = "啊哦，你怎么到这里来了，啥都没有，赶紧回地球去吧";
            }
        }
        Logger.severe("{ErrorController} " + request.getRequestURL() + ": " + type + ", " + msg);
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("result", msg);
        return new ModelAndView(VIEW_NAME, model);
    }

    public static void addErrorMsg(HttpServletResponse response, String msg) {
        try {
            msg = URLEncoder.encode(msg, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Cookie cookie = new Cookie(COOKIE_ERROR_MSG_NAME, msg);
        cookie.setMaxAge(COOKIE_ERROR_MSG_AGE);
        response.addCookie(cookie);
    }

    public static String getErrorMsg(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = CookieUtil.findCookie(request, COOKIE_ERROR_MSG_NAME);
        if (cookie == null) {
            return null;
        }
        String msg = cookie.getValue();
        try {
            msg = URLDecoder.decode(msg, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return msg;
    }

}
