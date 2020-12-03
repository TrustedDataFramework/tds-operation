package com.tds.monitor.controller;

import com.alibaba.fastjson.JSONObject;
import com.tds.monitor.dao.NodeDao;
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

    @Autowired
    private NodeDao nodeDao;

    JavaShellUtil javaShellUtil;

    private static final Logger logger = LoggerFactory.getLogger(NodeController.class);

    @GetMapping(value = {"/stop"})
    public Object stop(){
        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        try {
            if (mapCacheUtil.getCacheItem("bindNode") != null){
                String ipPort = mapCacheUtil.getCacheItem("bindNode").toString();
                String ip = mapCacheUtil.getCacheItem("bindNode").toString().split(":")[0];
                if(ip.equals(LocalHostUtil.getLocalIP())){
                    Nodes node = nodeDao.findNodesByNodeIPAndNodePort(ipPort.split(":")[0], ipPort.split(":")[1]).get();
                    String usepassword = node.getPassword();
                    System.out.println(usepassword);
                    String kill = JavaShellUtil.ProcessKillShell("sunflower",usepassword);
                    if(kill != null || !kill.equals("")){
                        result.setMessage("成功");
                        result.setCode(ResultCode.SUCCESS);
                        return result;
                    }
                }
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
            System.out.println("================="+LocalHostUtil.getLocalIP());
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
        String version = restTemplateUtil.getBrowserInfo(ip,8080);
        if(version != null){
            JSONObject jsonObject = JSONObject.parseObject(version);
            String ver = jsonObject.getString("data");
            result.setData(ver);
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
            String version = restTemplateUtil.getBrowserInfo(ip,8080);
            JSONObject jsonObject = JSONObject.parseObject(version);
            String ver = jsonObject.getString("data");
            result.setData(ver);
            result.setMessage("成功");
            result.setCode(ResultCode.SUCCESS);
            return result;
        }
    }

    //停止浏览器
    @GetMapping(value = {"/stopWeb"})
    public Object stopWeb(@RequestParam("password") String password) throws Exception {
        Result result = new Result();
        String res = javaShellUtil.ProcessBrowserShell(2,password);
        if(res == ""){
            result.setMessage("失败");
            result.setCode(ResultCode.FAIL);
            return result;
        }else{
            String ip = LocalHostUtil.getLocalIP();
            String version = restTemplateUtil.getBrowserInfo(ip,8080);
            JSONObject jsonObject = JSONObject.parseObject(version);
            String ver = jsonObject.getString("data");
            result.setData(ver);
            result.setMessage("成功");
            result.setCode(ResultCode.SUCCESS);
            return result;
        }
    }

    //重启浏览器
    @GetMapping(value = {"/restartWeb"})
    public Object restartWeb(@RequestParam("password") String password) throws Exception {
        Result result = new Result();
        String res = javaShellUtil.ProcessBrowserShell(3,password);
        if(res == ""){
            result.setMessage("失败");
            result.setCode(ResultCode.FAIL);
            return result;
        }else{
            String ip = LocalHostUtil.getLocalIP();
            String version = restTemplateUtil.getBrowserInfo(ip,8080);
            JSONObject jsonObject = JSONObject.parseObject(version);
            String ver = jsonObject.getString("data");
            result.setData(ver);
            result.setMessage("成功");
            result.setCode(ResultCode.SUCCESS);
            return result;
        }
    }
}
