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
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.*;

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

    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);

    @GetMapping(value = {"/stop"})
    public Object stop(){
        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        try {
            String ip = mapCacheUtil.getCacheItem("bindNode").toString().split(":")[0];
            if(ip.equals(LocalHostUtil.getLocalIP())){
                String pwd = Constants.getSudoPassword();
                String kill = JavaShellUtil.ProcessKillShell(1,pwd);
                if(kill != null || !kill.equals("")){
                    result.setMessage("成功");
                    result.setCode(ResultCode.SUCCESS);
                    return result;
                }
            }else {
                if (mapCacheUtil.getCacheItem("bindNode") != null) {
                    return nodeService.stop(mapCacheUtil.getCacheItem("bindNode").toString());
                }else{
                    result.setMessage("请绑定节点");
                    result.setCode(ResultCode.FAIL);
                    return result;
                }
            }
        }catch (Exception e){
            result.setMessage("失败");
            result.setCode(ResultCode.FAIL);
        }
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
                    String pwd = Constants.getSudoPassword();
                    JavaShellUtil.ProcessKillShell(1,pwd);
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
    public Object detectExplorer() throws Exception {
        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        String ip = mapCacheUtil.getCacheItem("bindNode").toString().split(":")[0];
        String version = restTemplateUtil.getBrowserInfo(ip,8080);
        if(version != null){
            JSONObject jsonObject = JSONObject.parseObject(version);
            String ver = jsonObject.getString("data");
            result.setData(ver);
            result.setMessage("运行中");
        }else{
            result.setMessage("未运行");
        }
        result.setCode(ResultCode.SUCCESS);
        return result;
    }

    //启动浏览器
    @GetMapping(value = {"/startWeb"})
    public String startWeb(){
        String password = Constants.getSudoPassword();
        javaShellUtil.ProcessBrowserShell(1,password);
        return "";
    }

    //初始化节点
    @GetMapping(value = {"/initNode"})
    public String initNode(){
        try {
            String pwd = Constants.getSudoPassword();
            javaShellUtil.ProcessKillShell(2,pwd);
            // 5 秒后退出运维工具
            EXECUTOR.schedule(() -> System.exit(0), 7, TimeUnit.SECONDS);
            return "";
        }catch (Exception e) {
            throw e;
        }
    }

    //查看节点和浏览器进程是否清除
    @GetMapping(value = {"/searchNodeAndWeb"})
    public Object searchNodeAndWeb() throws SocketException, UnknownHostException {
        Result result = new Result();
        String ip;
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            ip = mapCacheUtil.getCacheItem("bindNode").toString().split(":")[0];
        }else{
            ip = LocalHostUtil.getLocalIP();
        }
        String version = restTemplateUtil.getBrowserInfo(ip,8080);
        JSONObject jsonObject = restTemplateUtil.getNodeInfo(ip,7010);
        if(version != null && !version.isEmpty() && jsonObject != null && !jsonObject.isEmpty()){
            result.setData("");
            result.setCode(ResultCode.FAIL);
        }else{
            result.setCode(ResultCode.SUCCESS);
        }
        return result;
    }

    //查看浏览器是否启动
    @GetMapping(value = {"/webStartOrNot"})
    public Object webStartOrNot() throws Exception {

        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        String ip = mapCacheUtil.getCacheItem("bindNode").toString().split(":")[0];
        String version = restTemplateUtil.getBrowserInfo(ip,8080);
        if(version != null){
            JSONObject jsonObject = JSONObject.parseObject(version);
            String ver = jsonObject.getString("data");
            result.setData(ver);
            result.setMessage("成功");
            result.setCode(ResultCode.SUCCESS);
        }else{
            result.setMessage("未运行");
        }
        return result;
    }

    //查看浏览器容器是否清除
    @GetMapping(value = {"/webKillOrNot"})
    public Object webKillOrNot(){
        String password = Constants.getSudoPassword();
        String resu = javaShellUtil.exlporerShell(password);
//        resu = resu.substring(0,resu.indexOf("\\"));
//        int i = Integer.getInteger(resu);
        Result result = new Result();
        if(resu.equals("1\n")){
            result.setCode(ResultCode.SUCCESS);
        }else{
            result.setCode(ResultCode.FAIL);
        }
        return result;
    }

    //查看节点是否启动
    @GetMapping(value = {"/nodeStartOrNot"})
    public Object nodeStartOrNot() throws Exception {
        Result result = new Result();
        String ip = LocalHostUtil.getLocalIP();
        JSONObject jsonObject = restTemplateUtil.getNodeInfo(ip,7010);
        if(jsonObject != null) {
            result.setMessage("成功");
            result.setCode(ResultCode.SUCCESS);
        }
        result.setCode(ResultCode.FAIL);
        return result;
    }

    //停止浏览器
    @GetMapping(value = {"/stopWeb"})
    public Object stopWeb(){
        String password = Constants.getSudoPassword();
        javaShellUtil.ProcessBrowserShell(2,password);
        return "";
    }

    //重启浏览器
    @GetMapping(value = {"/restartWeb"})
    public Object restartWeb(){
        String password = Constants.getSudoPassword();
        javaShellUtil.ProcessBrowserShell(3,password);
        return "";
    }

    @GetMapping(value = {"/getBindNode"})
    public JSONObject getBindNode() {
        try {
            JSONObject jsonObject = new JSONObject();
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if (mapCacheUtil.getCacheItem("bindNode") != null){
                Nodes nodes = nodeService.searchNode(mapCacheUtil.getCacheItem("bindNode").toString());
                if(nodes == null){
                    jsonObject.put("code","5000");
                    return jsonObject;
                }
                jsonObject.put("ip",nodes.getNodeIP()+":"+nodes.getNodePort());
                String version1 = restTemplateUtil.getBrowserInfo(mapCacheUtil.getCacheItem("bindNode").toString().split(":")[0],8080);
                if(version1 != null) {
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
                }else{
                    jsonObject.put("code","5000");
                    return jsonObject;
                }
            }
            jsonObject.put("code","5000");
            return jsonObject;
        }catch (Exception e){
            throw e;
        }
    }
}
