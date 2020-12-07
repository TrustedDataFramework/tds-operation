package com.tds.monitor.controller;

import com.alibaba.fastjson.JSONObject;
import com.tds.monitor.dao.NodeDao;
import com.tds.monitor.model.Nodes;
import com.tds.monitor.model.Result;
import com.tds.monitor.model.ResultCode;
import com.tds.monitor.model.User;
import com.tds.monitor.service.Impl.ApplicationRunnerImpl;
import com.tds.monitor.service.Impl.NodeServiceImpl;
import com.tds.monitor.utils.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.net.UnknownHostException;

@RestController
public class NodeController {

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
                String ip = mapCacheUtil.getCacheItem("bindNode").toString().split(":")[0];
                if(ip.equals(LocalHostUtil.getLocalIP())){
                    String kill = JavaShellUtil.ProcessKillShell("sunflower");
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
                String ip = mapCacheUtil.getCacheItem("bindNode").toString().split(":")[0];
                if(ip.equals(LocalHostUtil.getLocalIP())){
                    JavaShellUtil.ProcessKillShell("sunflower");
                        String[] cmds = new String[]{
                                "nohup", ApplicationRunnerImpl.getJavaBin(), "-jar", Constants.TDS_JAR_PATH,
                                "--spring.config.location=" + Constants.YML_PATH,
                        };
                        Thread t = new Thread(() -> {
                            try {
                                Process process = Runtime.getRuntime().exec(cmds);
                                // 把子进程日志打到当前进程
                                IOUtils.copy(process.getInputStream(), new FileOutputStream(Constants.TDS_LOG));
                                // 把子进程错误日志打到当前进程
                                IOUtils.copy(process.getErrorStream(), new FileOutputStream(Constants.TDS_ERROR));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        t.start();
                        result.setMessage("成功");
                        result.setCode(ResultCode.SUCCESS);
                        return result;
                    }else {
                    return nodeService.restart(mapCacheUtil.getCacheItem("bindNode").toString());
                }
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
        String version = restTemplateUtil.getBrowserInfo(ip,8181);
        if(version != null){
            JSONObject jsonObject = JSONObject.parseObject(version);
            String ver = jsonObject.getString("data");
            result.setData(ver);
            result.setMessage("运行中");
        }else{
            result.setData("");
            result.setMessage("未运行");
        }
        result.setCode(ResultCode.SUCCESS);
        return result;
    }

    //启动浏览器
    @GetMapping(value = {"/startWeb"})
    public String startWeb(@RequestParam("password") String password) throws Exception {
        javaShellUtil.ProcessBrowserShell(1,password);
        return "";
    }

    //查看浏览器是否启动
    @GetMapping(value = {"/webStartOrNot"})
    public Object webStartOrNot() throws UnknownHostException {
        Result result = new Result();
        String ip = LocalHostUtil.getLocalIP();
        String version = restTemplateUtil.getBrowserInfo(ip,8080);
        JSONObject jsonObject = JSONObject.parseObject(version);
        String ver = jsonObject.getString("data");
        result.setData(ver);
        result.setMessage("成功");
        result.setCode(ResultCode.SUCCESS);
        return result;
    }

    //查看节点是否启动
    @GetMapping(value = {"/nodeStartOrNot"})
    public Object nodeStartOrNot() throws UnknownHostException {
        Result result = new Result();
        String ip = LocalHostUtil.getLocalIP();
        JSONObject jsonObject = restTemplateUtil.getNodeInfo(ip,7010);
        if(jsonObject != null) {
            result.setMessage("成功");
            result.setCode(ResultCode.SUCCESS);
        }
        return result;
    }

    //停止浏览器
    @GetMapping(value = {"/stopWeb"})
    public Object stopWeb(@RequestParam("password") String password) throws Exception {
        javaShellUtil.ProcessBrowserShell(2,password);
        return "";
    }

    //重启浏览器
    @GetMapping(value = {"/restartWeb"})
    public Object restartWeb(@RequestParam("password") String password) throws Exception {
        javaShellUtil.ProcessBrowserShell(3,password);
        return "";
    }

    @GetMapping(value = {"/getBindNode"})
    public JSONObject getBindNode() {
        JSONObject jsonObject = new JSONObject();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            Nodes nodes = nodeService.searchNode(mapCacheUtil.getCacheItem("bindNode").toString());
            jsonObject.put("ip",nodes.getNodeIP()+":"+nodes.getNodePort());
            JSONObject jo = (JSONObject) JSONObject.toJSON(NodeinfoController.getVersion());
            if (jo != null){
                JSONObject result = (JSONObject) jo.get("data");
                String version = result.get("version").toString();
                jsonObject.put("code","2000");
                jsonObject.put("status","运行中");
                jsonObject.put("version",version);
                jsonObject.put("type",nodes.getNodeType());
                return jsonObject;
            }
            jsonObject.put("code","3000");
            return jsonObject;
        }
        jsonObject.put("code","5000");
        return jsonObject;
    }
}
