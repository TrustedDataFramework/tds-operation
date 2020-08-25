package com.tds.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.tds.monitor.model.Result;
import com.tds.monitor.utils.HttpRequestUtil;
import com.tds.monitor.utils.MapCacheUtil;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NodeinfoController {

    public static Object getVersion(){
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            String iphost = mapCacheUtil.getCacheItem("bindNode").toString();
            String heightUrl = "http://"+iphost+"/rpc/stat";
            if (HttpRequestUtil.sendGet(heightUrl,"") != ""){
                return JSON.parseObject(HttpRequestUtil.sendGet(heightUrl,""));
            }
            return Result.newFailed("连接超时");
        }
        return Result.newFailed("请绑定节点");
    }
}
