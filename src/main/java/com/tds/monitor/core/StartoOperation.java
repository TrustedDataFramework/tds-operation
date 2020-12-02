package com.tds.monitor.core;

import com.alibaba.fastjson.JSONObject;
import com.tds.monitor.utils.LocalHostUtil;
import com.tds.monitor.utils.RestTemplateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class StartoOperation {

    @Autowired
    RestTemplateUtil restTemplateUtil;

    public StartoOperation() throws UnknownHostException {
        //查看本地节点是否启动
        //没有启动，查看java version是否安装，没有安装异常，查看是否有权限,正常到指定默认路径shell java -jar xxx.jar --启动节点，
        // 获取节点数据，初始化在h2节点信息中

        //查看浏览器是否启动
        //docker 启动浏览器
        //初始化在节h2浏览器信息中

    }
}
