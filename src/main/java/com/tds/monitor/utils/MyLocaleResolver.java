package com.tds.monitor.utils;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

public class MyLocaleResolver implements LocaleResolver {
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // 获取 请求（a标签）中携带的地区参数。
        String language = request.getParameter("l");
        // 获取默认的地区参数
        Locale locale = Locale.getDefault();
        // 如果不为空地区参数为获取的地区参数
        if (!StringUtils.isEmpty(language)){
            String[] split = language.split("_");
            locale = new Locale(split[0],split[1]);
        }
        return locale;
    }
    // 这个不写
    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
    }
}
