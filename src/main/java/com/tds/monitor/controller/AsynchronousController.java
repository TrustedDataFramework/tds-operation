package com.tds.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tds.monitor.dao.MailDao;
import com.tds.monitor.model.Mail;
import com.tds.monitor.service.Impl.NodeServiceImpl;
import com.tds.monitor.utils.HttpRequestUtil;
import com.tds.monitor.utils.MapCacheUtil;
import com.tds.monitor.utils.ApiResult.APIResult;
import com.tds.monitor.utils.SendMailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
public class AsynchronousController {

    @Autowired
    public NodeServiceImpl nodeService;

    @Autowired
    private MailDao mailDao;

    @GetMapping(value = {"/bifurcation"})
    public Object bifurcation() {
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            Monitor monitor = new Monitor();
            return monitor.checkBifurcate();
        }
        return APIResult.newFailResult(-2,"");
    }

    @GetMapping(value = {"/block"})
    public int block() {
        try {
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if (mapCacheUtil.getCacheItem("bindNode") != null) {
                //获取当前高度
                String ip = mapCacheUtil.getCacheItem("bindNode").toString();
                String heightUrl = "http://" + ip + "/rpc/stat";
                JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl, ""));
                if (result == null) {
                    return 0;
                }
                JSONObject resultAee = result.getJSONObject("data");
                String allowUnauthorized = resultAee.getString("allowUnauthorized");
                int averageBlockInterval = resultAee.getInteger("averageBlockInterval");
                String timeUrl = "http://" + ip + "/rpc/block/-1";
                JSONObject resultTime = JSON.parseObject(HttpRequestUtil.sendGet(timeUrl, ""));
                JSONObject result1 = resultTime.getJSONObject("data");
                if (result1 == null) {
                    return 0;
                }
                if (!allowUnauthorized.equals("true")) {
                    return 1;
                } else {
                    String time = result1.getString("createdAt");
                    Instant ins = Instant.parse(time);
                    // unix 秒
                    long now = System.currentTimeMillis() / 1000;
                    if(now - ins.getEpochSecond()>averageBlockInterval*10){
                        return -1;
                    }
                    return 1;
                }
            }else{
                return 0;
            }
        }catch (Exception e){
            throw e;
        }
    }
}
