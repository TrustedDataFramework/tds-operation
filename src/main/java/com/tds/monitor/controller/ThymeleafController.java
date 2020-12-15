package com.tds.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tds.monitor.dao.MailDao;
import com.tds.monitor.dao.NodeDao;
import com.tds.monitor.dao.UserDao;
import com.tds.monitor.model.*;
import com.tds.monitor.security.IsUser;
import com.tds.monitor.service.CustomUser;
import com.tds.monitor.service.Impl.CustomUserServiceImpl;
import com.tds.monitor.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@IsUser
@Controller
@EnableScheduling
@Slf4j
public class ThymeleafController {

    @Autowired
    private CustomUserServiceImpl customUserService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private MailDao mailDao;

    JavaShellUtil javaShellUtil;


    @Value("${Image}")
    private String image;

    @RequestMapping("/")
    public String home(ModelMap map) throws Exception {
        CustomUser customUser = (CustomUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //更新用户最后登陆时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (userDao.findByName(customUser.getUsername()).isPresent()){
            User user = userDao.findByName(customUser.getUsername()).get();
            user.setLogin_time(formatter.format(new Date()));
            userDao.save(user);
        }
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        TDSInfo info = new TDSInfo();
        //节点详情
        Nodes nodeinfo = new Nodes();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            String url_node = mapCacheUtil.getCacheItem("bindNode").toString();
            JSONObject get_info = new JSONObject();
            if (HttpRequestUtil.sendGet(String.format("http://%s/rpc/stat", url_node), null)!=""){
                get_info = JSON.parseObject(HttpRequestUtil.sendGet(String.format("http://%s/rpc/stat", url_node), null)).getJSONObject("data");
            }
            if (get_info != null && get_info.size() != 0){
                info = JSON.toJavaObject(get_info, TDSInfo.class);
                info.setCpu(String.format("%.2f", Float.parseFloat(info.getCpu())) + "%");
                BigDecimal memoryUsed = new BigDecimal(info.getMemoryUsed());
                BigDecimal totalMemory = new BigDecimal(info.getTotalMemory());
                BigDecimal memory = memoryUsed.divide(totalMemory,4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                info.setMemory(String.format("%.2f", memory) + "%");
                byte[] message = Files.readAllBytes(Paths.get(System.getProperty("user.home"), ".tdos", "etc", "config.json"));
                String mess = new String(message);
                if (JSON.parseObject(mess).getString("mining").equals("true")) {
                    info.setMinings("是");
                } else {
                    info.setMinings("否");
                }
                info.setP2pAddress(info.getP2pAddress().substring(0,info.getP2pAddress().indexOf(":")));
                map.addAttribute("isrun","运行中");
            }else {
                info = JSON.toJavaObject((JSONObject)JSONObject.toJSON(new TDSInfo()), TDSInfo.class);
            }
        }
        map.addAttribute("info", nodeinfo);
        map.addAttribute("result", info);
        map.addAttribute("role",customUserService.getRole());
        return "InfoSummary";
    }

    @RequestMapping("/home")
    public String home1(ModelMap map) throws Exception {
        CustomUser customUser = (CustomUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        TDSInfo info = new TDSInfo();
        //节点详情
        Nodes nodeinfo = new Nodes();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            String url_node = mapCacheUtil.getCacheItem("bindNode").toString();
            JSONObject get_info = new JSONObject();
            if (HttpRequestUtil.sendGet(String.format("http://%s/rpc/stat", url_node), null)!=""){
                get_info = JSON.parseObject(HttpRequestUtil.sendGet(String.format("http://%s/rpc/stat", url_node), null)).getJSONObject("data");
            }
            if (get_info != null && get_info.size() != 0){
                info = JSON.toJavaObject(get_info, TDSInfo.class);
                if(info.getCpu().compareTo("0")<0){
                    info.setCpu("0.1%");
                }else {
                    info.setCpu(String.format("%.2f", Float.parseFloat(info.getCpu())) + "%");
                }
                BigDecimal memoryUsed = new BigDecimal(info.getMemoryUsed());
                BigDecimal totalMemory = new BigDecimal(info.getTotalMemory());
                BigDecimal memory = memoryUsed.divide(totalMemory,4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                info.setMemory(String.format("%.2f", memory) + "%");

                byte[] message = Files.readAllBytes(Paths.get(System.getProperty("user.home"), ".tdos", "etc", "config.json"));
                String mess = new String(message);
                if (JSON.parseObject(mess).getString("mining").equals("true")) {
                    info.setMinings("是");
                } else {
                    info.setMinings("否");
                }
                map.addAttribute("isrun","运行中");
            }else {
                info = JSON.toJavaObject((JSONObject)JSONObject.toJSON(new TDSInfo()), TDSInfo.class);
            }
        }
        map.addAttribute("info", nodeinfo);
        map.addAttribute("result", info);
        map.addAttribute("role",customUserService.getRole());
        return "InfoSummary";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/header")
    public String header(ModelMap map) {
        try {
            map.addAttribute((CustomUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            map.addAttribute("dateNow", new java.util.Date().getTime());
            List<String> nodeinfoList = new ArrayList<>();
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            JSONObject get_info;
            String p2pAddress = "";
            TDSInfo info;
            if (mapCacheUtil.getCacheItem("bindNode") != null) {
                nodeinfoList.add(mapCacheUtil.getCacheItem("bindNode").toString());
                if (HttpRequestUtil.sendGet(String.format("http://%s/rpc/stat", mapCacheUtil.getCacheItem("bindNode")), null)!=""){
                    get_info = JSON.parseObject(HttpRequestUtil.sendGet(String.format("http://%s/rpc/stat", mapCacheUtil.getCacheItem("bindNode")), null)).getJSONObject("data");
                    if (get_info != null && get_info.size() != 0) {
                        info = JSON.toJavaObject(get_info, TDSInfo.class);
                        p2pAddress =  info.getP2pAddress().substring(0,info.getP2pAddress().indexOf(":"));
                    }
                }
            }
            map.addAllAttributes(nodeinfoList);
            //节点详情
            Nodes nodeinfo = new Nodes();
            String version = "";
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(NodeinfoController.getVersion());
            if ((int) jsonObject.get("code") == 200) {
                JSONObject result = (JSONObject) jsonObject.get("data");
                version = result.get("version").toString();
                nodeinfo.setNodeIP(mapCacheUtil.getCacheItem("bindNode").toString());
                String[] bindNodeiphost = mapCacheUtil.getCacheItem("bindNode").toString().split(":");
                List<Nodes> nodeList = nodeDao.findAll();
                for (int i = 0; i < nodeList.size(); i++) {
                    if (nodeList.get(i).getNodeIP().equals(bindNodeiphost[0]) && nodeList.get(i).getNodePort().equals(bindNodeiphost[1])) {
                        if (nodeList.get(i).getNodeType().equals("1")) {
                            nodeinfo.setNodeType("全节点");
                            break;
                        }
                        nodeinfo.setNodeType("矿工节点");
                        break;
                    }
                }
                map.addAttribute("info", nodeinfo);
                map.addAttribute("isrun", "运行中");
            } else {
                if(mapCacheUtil.getCacheItem("bindNode") != null){
                    String node = javaShellUtil.nodeShell();
                    if (node.equals("true\n")) {
                        map.addAttribute("isrun", "正在启动中");
                    }
                } else {
                    nodeinfo.setNodeIP(jsonObject.getString("message"));
                }
            }
            map.addAttribute("info", nodeinfo);
            map.addAttribute("version", version);
            map.addAttribute("p2pAddress", p2pAddress);
            map.addAttribute("role", customUserService.getRole());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "header";
    }

    @GetMapping("/sider")
    public String sider() {
        return "sider";
    }

    @GetMapping("/dataInitialization")
    public String dataInitialization() {
        return "dataInitialization";
    }

    @RequestMapping("/test1")
    public String test1(){
        String node = javaShellUtil.nodeShell();
        return node;
    }


    @RequestMapping("/console")
    public String console(ModelMap map,
                            @RequestParam(defaultValue = "0",value = "page")Integer pageNum){
        List<Nodes> nodeList = nodeDao.findAll();
        if (nodeList != null) {
            List<String> list = new ArrayList<>();
            List<Nodes> nodesListPage = new ArrayList<>();
            if (nodeList.size() - pageNum * 10 <= 10 && nodeList.size() - pageNum * 10 >= 0) {
                for (int i = pageNum * 10; i < nodeList.size(); i++) {
                    nodesListPage.add(nodeList.get(i));
                }
            } else {
                for (int i = pageNum * 10; i < (pageNum + 1) * 10; i++) {
                    nodesListPage.add(nodeList.get(i));
                }
            }
            for (int i = 0; i < nodeList.size(); i++) {
                String heightUrl = "http://" + nodeList.get(i).getNodeIP() + ":" + nodeList.get(i).getNodePort() + "/rpc/stat";
                if (HttpRequestUtil.sendGet(heightUrl, "") == "") {
                    MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
                    if (mapCacheUtil.getCacheItem("bindNode") != null) {
                        String node = javaShellUtil.nodeShell();
                        if (node.equals("true\n")) {
                            nodeList.get(i).setNodeState("正在启动中");
                        } else {
                            nodeList.get(i).setNodeState("未运行");
                        }
                    }else {
                        nodeList.get(i).setNodeState("未运行");
                    }
                } else {
                    nodeList.get(i).setNodeState("运行中");
                    nodeList.get(i).setNodeVersion(JSON.parseObject(JSON.parseObject(HttpRequestUtil.sendGet(heightUrl, "")).get("data").toString()).get("version").toString());
                }
            }
            map.addAttribute(nodeList);
            map.addAllAttributes(list);
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if (mapCacheUtil.getCacheItem("bindNode") != null) {
                map.addAttribute("nodeip", mapCacheUtil.getCacheItem("bindNode").toString());
            }
            map.addAttribute("pageSize", nodeList.size());
            map.addAttribute("nodesListPage", nodesListPage);
        }else{
            map.addAttribute("pageSize", 0);
        }
        map.addAttribute("role", customUserService.getRole());
        return "console";
    }

    @GetMapping("/infoSummary")
    public String InfoSummary() {
        return "InfoSummary";
    }

    @GetMapping("/layout")
    public String layout() {
        return "login";
    }

    @GetMapping("/log")
    public String log() {
        return "log";
    }

    @GetMapping("/warnInfo")
    public String warnInfo(ModelMap map){
        Mail mail = new Mail();
        if(mailDao.findAll().size() != 0){
            mail = mailDao.findAll().get(0);
        }
        map.addAttribute(mail);
        map.addAttribute("role",customUserService.getRole());
        return "warnInfo";
    }

    @GetMapping("/authenticationSet")
    public String authenticationSet(ModelMap map,
                                    @RequestParam(defaultValue = "0",value = "page")Integer pageNum){
        List<User> userListPage = new ArrayList<User>();
        List<User> userList = userDao.findAll();
        if(userList.size()-pageNum*10<=10&&userList.size()-pageNum*10>=0){
            for(int i = pageNum*10;i<userList.size();i++){
                userListPage.add(userList.get(i));
            }
        }else {
            for (int i = pageNum * 10; i < (pageNum + 1) * 10; i++) {
                userListPage.add(userList.get(i));
            }
        }
        map.addAttribute("userListPage",userListPage);
        map.addAttribute("pageSize",userList.size());
        map.addAttribute("role",customUserService.getRole());
        return "authenticationSet";
    }

    @GetMapping("/dataOutput")
    public String dataOutput() {
        return "dataOutput";
    }

    @GetMapping("/monitorBrowser")
    public String monitorBrowser() {
        return "monitorBrowser";
    }

    @ResponseBody
    @RequestMapping("/adduser")
    public String adduser(@ModelAttribute User user){
        boolean tag = false;
        if (!userDao.findByName(user.getName()).isPresent()){
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
            userDao.save(user);
            tag = true;
        }
        Result rs = new Result();
        if (tag) {
            rs.setCode(ResultCode.SUCCESS);
            rs.setMessage("添加用户成功");
        } else {
            rs.setCode(ResultCode.Warn);
            rs.setMessage("已存在相同登录名");
        }
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/deleteuser")
    public String deleteuser(@ModelAttribute User user){
        Result rs = new Result();
        List<User> userList = userDao.findAll();
        if (userDao.findByName(user.getName()).isPresent()){
            userDao.deleteByName(user.getName());
            rs.setCode(ResultCode.SUCCESS);
            rs.setMessage("删除成功");
        }else {
            rs.setCode(ResultCode.FAIL);
            rs.setMessage("删除失败");
        }
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/addnode")
    public String addnode(@ModelAttribute Nodes node){
        Result rs = new Result();
        if (!nodeDao.findNodesByNodeIPAndNodePort(node.getNodeIP(),node.getNodePort()).isPresent()){
            nodeDao.save(node);
            rs.setCode(ResultCode.SUCCESS);
            rs.setMessage("添加节点成功");
        }else {
            rs.setCode(ResultCode.FAIL);
            rs.setMessage("已存在相同节点");
        }
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/bindNode")
    public String bindNode(@RequestParam("nodeStr") String nodeStr){
        Result rs = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") == null){
            mapCacheUtil.putCacheItem("bindNode",nodeStr);
        }else {
            mapCacheUtil.removeCacheItem("bindNode");
            mapCacheUtil.putCacheItem("bindNode",nodeStr);
        }

        rs.setCode(ResultCode.SUCCESS);
        rs.setMessage("绑定成功");
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/unbindNode")
    public String unbindNode(@RequestParam("nodeStr") String nodeStr) throws IOException {
        Result rs = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        mapCacheUtil.removeCacheItem("bindNode");
        rs.setCode(ResultCode.SUCCESS);
        rs.setMessage("解绑成功");
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/deleteNode")
    public String deleteNode(@RequestParam("nodeStr") String nodeStr){
        Result rs = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            if (mapCacheUtil.getCacheItem("bindNode").toString().equals(nodeStr)){
                mapCacheUtil.removeCacheItem("bindNode");
            }
        }
        String[] nodeinfo = nodeStr.split(":");
        nodeDao.deleteByNodeIPAndNodePort(nodeinfo[0],nodeinfo[1]);
        rs.setCode(ResultCode.SUCCESS);
        rs.setMessage("删除成功");
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/editmail")
    public String editMail(@ModelAttribute Mail mail) throws IOException {
        Result rs = new Result();
        if(mailDao.findAll().size() == 0){
            mailDao.save(mail);
        }else{
            Mail mail1 = mailDao.findAll().get(0);
            mail1.setSender(mail.getSender());
            mail1.setPassword(mail.getPassword());
            mail1.setReceiver(mail.getReceiver());
            mailDao.save(mail1);
        }
        rs.setCode(ResultCode.SUCCESS);
        rs.setMessage("成功");
        return rs.toString();
    }

    @GetMapping(value = {"/printLog"})
    @ResponseBody
    public Object printLog(@RequestParam("startTime")String startTime,
                           @RequestParam("endTime")String endTime,HttpServletResponse response){
        Result result = new Result();
        startTime = startTime.replace(" ","T");
        endTime = endTime.replace(" ","T");
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        try {
            if (mapCacheUtil.getCacheItem("bindNode") != null){
                try {
                    GetNodeinfo getNodeinfo = new GetNodeinfo(mapCacheUtil.getCacheItem("bindNode").toString()).invoke();
                    String username = getNodeinfo.username;
                    String usepassword = getNodeinfo.usepassword;
                    String ip = getNodeinfo.ip;
                    ConnectionUtil connectionUtil = new ConnectionUtil(ip, username, usepassword);
                    if (connectionUtil.login()) {
                        if (connectionUtil.login()) {
                            String shellResult = connectionUtil.executeSuccess("echo " + usepassword + " |sudo -S docker logs -t --since='"+startTime+"' --until='"+endTime+"' "+ image);
                            if (StringUtils.isBlank(shellResult)) {
                                result.setMessage("查询失败");
                                result.setCode(ResultCode.FAIL);
                                return result;
                            }
                            result.setMessage("成功");
                            result.setData(shellResult);
                            result.setCode(ResultCode.SUCCESS);
                            return result;
                            } else {
                                result.setMessage("失败");
                                result.setCode(ResultCode.FAIL);
                                return result;
                            }
                        }


                } catch (Exception e) {
                    result.setMessage("错误");
                    result.setCode(ResultCode.FAIL);
                    return result;
                }
                result.setMessage("失败");
                result.setCode(ResultCode.FAIL);
                return result;
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

    private class GetNodeinfo {
        private String ipPort;
        private String username;
        private String usepassword;
        private String ip;

        public GetNodeinfo(String ipPort) {
            this.ipPort = ipPort;
        }

        public String getUsername() {
            return username;
        }

        public String getUsepassword() {
            return usepassword;
        }

        public String getIp() {
            return ip;
        }

        public String getIpPort() {
            return ipPort;
        }


        public GetNodeinfo invoke() throws IOException {
            List<Nodes> nodeList = nodeDao.findAll();
            ip = null;
            if (nodeList != null) {
                for (int i = 0; i < nodeList.size(); i++) {
                    if (nodeList.get(i).getNodeIP().equals(ipPort.split(":")[0]) && nodeList.get(i).getNodePort().equals(ipPort.split(":")[1])) {
                        ip = nodeList.get(i).getNodeIP();
                        username = nodeList.get(i).getUserName();
                        usepassword = nodeList.get(i).getPassword();
                        break;
                    }
                }
            }
            return this;
        }
    }


}