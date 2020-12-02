package com.tds.monitor.controller;

import com.tds.monitor.model.Nodes;
import com.tds.monitor.model.Result;
import com.tds.monitor.model.ResultCode;
import com.tds.monitor.model.User;
import com.tds.monitor.service.Impl.NodeServiceImpl;
import com.tds.monitor.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;

@RestController
public class NodeController {

//    @Value("${Image}")
//    private String image;

    @Autowired
    public NodeServiceImpl nodeService;

    @Autowired
    RestTemplateUtil restTemplateUtil;

    JavaShellUtil javaShellUtil;

    private static final Logger logger = LoggerFactory.getLogger(NodeController.class);

    @GetMapping(value = {"/stop"})
    public Object stop(){
        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        try {
            if (mapCacheUtil.getCacheItem("bindNode") != null){
                return nodeService.stop(mapCacheUtil.getCacheItem("bindNode").toString());
            }
        }catch (Exception e){
            result.setMessage("失败");
            result.setCode(ResultCode.FAIL);
            return result;
        }
        result.setMessage("请绑定节点");
        result.setCode(ResultCode.FAIL);
        return result;
    }

    @GetMapping(value = {"/restart"})
    public Object restart() {
        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        try {
            if (mapCacheUtil.getCacheItem("bindNode") != null){
                return nodeService.restart(mapCacheUtil.getCacheItem("bindNode").toString());
            }
        }catch (Exception e){
            result.setMessage("失败");
            result.setCode(ResultCode.FAIL);
            return result;
        }
        result.setMessage("请绑定节点");
        result.setCode(ResultCode.FAIL);
        return result;
    }

    @GetMapping(value = {"/connection"})
    @ResponseBody
    public Object connection(String ip,String username,String password) {
        Result result = new Result();
        ConnectionUtil connectionUtil = new ConnectionUtil(ip, username, password);
        if (connectionUtil.login()) {
            result.setMessage("成功");
            result.setCode(ResultCode.SUCCESS);
        } else {
            result.setMessage("连接失败");
            result.setCode(ResultCode.FAIL);
        }
        return result;
    }

    @GetMapping(value = {"/searchNode"})
    public Nodes searchNode(@RequestParam("ipPort") String ipPort) {
        return nodeService.searchNode(ipPort);
    }

    @GetMapping(value = {"/searchUser"})
    public User searchUser(@RequestParam("username") String username) {
        return nodeService.searchUser(username);
    }

    @GetMapping(value = {"/updateUser"})
    public boolean updateUser(@ModelAttribute User user) {
        return nodeService.updateUser(user);
    }

    @GetMapping(value = {"/updateNode"})
    public boolean updateNode(@ModelAttribute Nodes node) {
        return nodeService.updateNode(node);
    }

    @GetMapping(value = {"/getLocalIp"})
    public Object getLocalIp(){
        Result result = new Result();
        try {
            result.setMessage("成功");
            result.setCode(ResultCode.SUCCESS);
            result.setData(LocalHostUtil.getLocalIP());
            return result;
        }catch (Exception e){
            result.setMessage("失败");
            result.setCode(ResultCode.FAIL);
            return result;
        }
    }
    //检测浏览器
    @GetMapping(value = {"/detectExplorer"})
    public Object detectExplorer() throws UnknownHostException {
        Result result = new Result();
        String ip = LocalHostUtil.getLocalIP();
        String version = restTemplateUtil.getBrowserInfo(ip,8181);
        if(version != null){
            result.setData(version);
        }else{
            result.setData("");
        }
        result.setMessage("成功");
        result.setCode(ResultCode.SUCCESS);
        return result;
    }

    //启动浏览器
    @GetMapping(value = {"/startWeb"})
    public Object startWeb(@RequestParam("password") String password) throws Exception {
        Result result = new Result();
        String res = javaShellUtil.ProcessBrowserShell(1,password);
        if(res == ""){
            result.setMessage("失败");
            result.setCode(ResultCode.FAIL);
            return result;
        }else{
            String ip = LocalHostUtil.getLocalIP();
            String version = restTemplateUtil.getBrowserInfo(ip,8181);
            result.setData(version);
            result.setMessage("成功");
            result.setCode(ResultCode.SUCCESS);
            return result;
        }
    }
}
