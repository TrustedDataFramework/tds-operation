package com.tds.monitor.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.cglib.core.Local;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@Controller
public class LocaleController {
    @GetMapping("/locale")
    public String localeHandler(HttpServletRequest request){
        String lastUrl = request.getHeader("referer");
        // 向界面返回这个字符串
        return "redirect:"+lastUrl;
    }

    @PostMapping("/changeLang")
    @ResponseBody
    public String i18nChange(String lang ,HttpServletRequest request){
        if("zh_cn".equals(lang)){
            Locale locale = new Locale("zh","CN");
            request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,locale);
            request.getSession().setAttribute("lan","zh_CN");
        }else if("en_us".equals(lang)){
            Locale locale = new Locale("en","US");
            request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME,locale);
            request.getSession().setAttribute("lan","en_US");
        }else{
            request.getSession().setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, LocaleContextHolder.getLocale());
            request.getSession().setAttribute("lan","zh_CN");
        }
        return JSON.toJSONString("success");
    }
}
