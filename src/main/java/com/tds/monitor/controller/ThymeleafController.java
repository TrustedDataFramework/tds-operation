package com.tds.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tds.monitor.leveldb.Leveldb;
import com.tds.monitor.model.*;
import com.tds.monitor.model.*;
import com.tds.monitor.security.IsUser;
import com.tds.monitor.service.CustomUser;
import com.tds.monitor.service.Impl.CustomUserServiceImpl;
import com.tds.monitor.utils.ConnectionUtil;
import com.tds.monitor.utils.HttpRequestUtil;
import com.tds.monitor.utils.MapCacheUtil;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@IsUser
@Controller
@EnableScheduling
@Slf4j
public class ThymeleafController {

    @Autowired
    private CustomUserServiceImpl customUserService;

    @Value("${Image}")
    private String image;

    @RequestMapping("/")
    public String home(ModelMap map) throws Exception {
        CustomUser customUser = (CustomUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //更新用户最后登陆时间
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Leveldb leveldb = new Leveldb();
        List<User> userList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("user"), User.class);
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getName().equals(customUser.getUsername())) {
                userList.get(i).setLoginTime(formatter.format(new Date()));
                break;
            }
        }
        leveldb.addAccount("user", JSON.toJSONString(userList));
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        TDSInfo info = new TDSInfo();
        //节点详情
        Nodes nodeinfo = new Nodes();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            String url_node = mapCacheUtil.getCacheItem("bindNode").toString();
            //String url_node = "192.168.1.167:7010";
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
                if(info.getMining().equals(true)){
                    info.setMining("是");
                }else{
                    info.setMining("否");
                }
                String[] bindNodeiphost = mapCacheUtil.getCacheItem("bindNode").toString().split(":");
                List<Nodes> nodeList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);
                for (int i = 0; i < nodeList.size(); i++) {
                    if (nodeList.get(i).getNodeIP().equals(bindNodeiphost[0]) && nodeList.get(i).getNodePort().equals(bindNodeiphost[1])) {
                        if (nodeList.get(i).getNodeType().equals("1")){
                            nodeinfo.setNodeType("全节点");
                            break;
                        }
                        nodeinfo.setNodeType("矿工节点");
                        break;
                    }
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

    @RequestMapping("/home")
    public String home1(ModelMap map) throws Exception {
        CustomUser customUser = (CustomUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        Leveldb leveldb = new Leveldb();
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
                    info.setCpu("异常");
                }else {
                    info.setCpu(String.format("%.2f", Float.parseFloat(info.getCpu())) + "%");
                }
                BigDecimal memoryUsed = new BigDecimal(info.getMemoryUsed());
                BigDecimal totalMemory = new BigDecimal(info.getTotalMemory());
                BigDecimal memory = memoryUsed.divide(totalMemory,4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                info.setMemory(String.format("%.2f", memory) + "%");
                if(info.getMining().equals(true)){
                    info.setMining("是");
                }else{
                    info.setMining("否");
                }
                String[] bindNodeiphost = mapCacheUtil.getCacheItem("bindNode").toString().split(":");
                List<Nodes> nodeList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);
                for (int i = 0; i < nodeList.size(); i++) {
                    if (nodeList.get(i).getNodeIP().equals(bindNodeiphost[0]) && nodeList.get(i).getNodePort().equals(bindNodeiphost[1])) {
                        if (nodeList.get(i).getNodeType().equals("1")){
                            nodeinfo.setNodeType("全节点");
                            break;
                        }
                        nodeinfo.setNodeType("矿工节点");
                        break;
                    }
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

    @GetMapping("/header")
    public String header(ModelMap map) {
        try {
            Leveldb leveldb = new Leveldb();
            map.addAttribute((CustomUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            map.addAttribute("dateNow", new java.util.Date().getTime());
            List<String> nodeinfoList = new ArrayList<>();
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if (mapCacheUtil.getCacheItem("bindNode") != null) {
                nodeinfoList.add(mapCacheUtil.getCacheItem("bindNode").toString());
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
                List<Nodes> nodeList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);
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
                nodeinfo.setNodeIP(jsonObject.getString("message"));
            }
            map.addAttribute("info", nodeinfo);
            map.addAttribute("version", version);
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

    @GetMapping("/console")
    public String console(ModelMap map,
                            @RequestParam(defaultValue = "0",value = "page")Integer pageNum) throws IOException {
        Leveldb leveldb = new Leveldb();
        List<Nodes> nodeList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);

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
                    nodeList.get(i).setNodeState("未运行");
                } else {
                    nodeList.get(i).setNodeState("运行中");
                    nodeList.get(i).setNodeVersion(JSON.parseObject(JSON.parseObject(HttpRequestUtil.sendGet(heightUrl, "")).get("data").toString()).get("version").toString());
                }
            }

            map.addAttribute(nodeList);
            map.addAllAttributes(list);
            List<String> nodeinfoList = new ArrayList<>();
            String nodeinfo = "";
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if (mapCacheUtil.getCacheItem("bindNode") != null) {
                nodeinfoList = Arrays.asList(mapCacheUtil.getCacheItem("bindNode").toString().split(":"));
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

    @GetMapping("/InfoSummary")
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
    public String warnInfo(ModelMap map) throws IOException {
        Leveldb leveldb = new Leveldb();
        Mail mail = new Mail();
        Object read = JSONObject.parseObject(leveldb.readAccountFromSnapshot("mail"), Mail.class);
        if (read != null) {
            mail= (Mail) read;
        }
        map.addAttribute(mail);
        return "warnInfo";
    }

    @GetMapping("/authenticationSet")
    public String authenticationSet(ModelMap map,
                                    @RequestParam(defaultValue = "0",value = "page")Integer pageNum) throws  Exception{
        Leveldb leveldb = new Leveldb();
        List<User> userListPage = new ArrayList<User>();
        List<User> userList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("user"), User.class);
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

    @ResponseBody
    @GetMapping("/adduser")
    public String adduser(@ModelAttribute User user) throws IOException {
        boolean tag = false;
        Leveldb leveldb = new Leveldb();
        List<User> userList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("user"), User.class);
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getName().equals(user.getName())) {
                tag = true;
                break;
            }
        }
        Result rs = new Result();
        if (!tag) {
            user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
            userList.add(user);
            leveldb.addAccount("user", JSON.toJSONString(userList));
            rs.setCode(ResultCode.SUCCESS);
            rs.setMessage("添加用户成功");
        } else {
            rs.setCode(ResultCode.Warn);
            rs.setMessage("已存在相同登录名");
        }
        return rs.toString();
    }

    @ResponseBody
    @GetMapping("/deleteuser")
    public String deleteuser(@ModelAttribute User user) throws IOException {
        Result rs = new Result();
        Leveldb leveldb = new Leveldb();
        List<User> userList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("user"), User.class);
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getName().equals(user.getName())) {
                userList.remove(i);
                leveldb.addAccount("user", JSON.toJSONString(userList));
                rs.setCode(ResultCode.SUCCESS);
                rs.setMessage("删除成功");
                break;
            }
        }
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/addnode")
    public String addnode(@ModelAttribute Nodes node) throws IOException {
        boolean tag = false;
        Leveldb leveldb = new Leveldb();
        List<Nodes> nodeList = new ArrayList<Nodes>();
        Object read = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);
        if (read != null) {
            nodeList = (List<Nodes>) read;
            for (int i = 0; i < nodeList.size(); i++) {
                if (nodeList.get(i).getNodeIP().equals(node.getNodeIP()) && nodeList.get(i).getNodePort().equals(node.getNodePort())) {
                    tag = true;
                    break;
                }
            }
        }
        Result rs = new Result();
        if (!tag) {
            nodeList.add(node);
            leveldb.addAccount("node", JSON.toJSONString(nodeList));
            rs.setCode(ResultCode.SUCCESS);
            rs.setMessage("添加节点成功");
        } else {
            rs.setCode(ResultCode.Warn);
            rs.setMessage("已存在相同节点");
        }
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/bindNode")
    public String bindNode(@RequestParam("nodeStr") String nodeStr) throws IOException {
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
    public String deleteNode(@RequestParam("nodeStr") String nodeStr) throws IOException {
        Result rs = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            if (mapCacheUtil.getCacheItem("bindNode").toString().equals(nodeStr)){
                mapCacheUtil.removeCacheItem("bindNode");
            }
        }
        Leveldb leveldb = new Leveldb();
        String[] nodeinfo = nodeStr.split(":");
        List<Nodes> nodesList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);
        for (int i = 0; i < nodesList.size(); i++) {
            if (nodesList.get(i).getNodeIP().equals(nodeinfo[0]) && nodesList.get(i).getNodePort().equals(nodeinfo[1])) {
                nodesList.remove(i);
                leveldb.addAccount("node", JSON.toJSONString(nodesList));
                rs.setCode(ResultCode.SUCCESS);
                rs.setMessage("删除成功");
                break;
            }
        }
        return rs.toString();
    }

    @ResponseBody
    @RequestMapping("/editmail")
    public String editMail(@ModelAttribute Mail mail) throws IOException {
        Result rs = new Result();
        Leveldb leveldb = new Leveldb();
        leveldb.addAccount("mail", JSON.toJSONString(mail));
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
                List<String> strList = new ArrayList<String>();
                try {
                    //GetNodeinfo getNodeinfo = new GetNodeinfo(mapCacheUtil.getCacheItem("bindNode").toString()).invoke();
                    String username = "sal";
                    String usepassword = "123456";
                    String ip = "192.168.1.36";
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
            Leveldb leveldb = new Leveldb();
            Object read = JSONObject.parseArray(leveldb.readAccountFromSnapshot("node"), Nodes.class);
            List<Nodes> nodeList = new ArrayList<Nodes>();
            ip = null;
            if (read != null) {
                nodeList = (List<Nodes>) read;
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