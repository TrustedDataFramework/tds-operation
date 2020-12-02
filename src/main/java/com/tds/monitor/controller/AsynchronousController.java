package com.tds.monitor.controller;

import com.tds.monitor.service.Impl.NodeServiceImpl;
import com.tds.monitor.utils.MapCacheUtil;
import com.tds.monitor.utils.ApiResult.APIResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AsynchronousController {
    private static final Logger logger = LoggerFactory.getLogger(AsynchronousController.class);

    @Autowired
    public NodeServiceImpl nodeService;

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
    public String block() {
        int blockStatus = Monitor.checkBlockIsStuck(false);
        if (blockStatus == -1) {
            return "异常";
        } else if (blockStatus == 0) {
            return "待确认";
        } else {
            return "正常";
        }
    }
}
