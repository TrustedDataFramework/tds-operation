package com.tds.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tds.monitor.model.*;
import com.tds.monitor.service.Impl.ApplicationRunnerImpl;
import com.tds.monitor.service.Impl.NodeServiceImpl;
import com.tds.monitor.utils.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.*;

@RestController
public class NodeController {

    @Autowired
    public NodeServiceImpl nodeService;

    @Autowired
    RestTemplateUtil restTemplateUtil;

    JavaShellUtil javaShellUtil;

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
                if(ip.equals(LocalHostUtil.getLocalIP())) {
                    String pwd = Constants.getSudoPassword();
                    JavaShellUtil.ProcessKillShell(1, pwd);
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

    @GetMapping(value = {"/getInformation"})
    public Result getInformation() {
        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        JSONObject get_info = new JSONObject();
        if (mapCacheUtil.getCacheItem("bindNode") != null){
            String url_node = mapCacheUtil.getCacheItem("bindNode").toString();
            if (HttpRequestUtil.sendGet(String.format("http://%s/rpc/stat", url_node), null)!=""){
                get_info = JSON.parseObject(HttpRequestUtil.sendGet(String.format("http://%s/rpc/stat", url_node), null)).getJSONObject("data");
                result.setCode(ResultCode.SUCCESS);
                result.setData(get_info);
            }else{
                result.setCode(ResultCode.FAIL);
                result.setData(get_info);
            }
        }else{
            result.setCode(ResultCode.FAIL);
            result.setData(get_info);
        }
        return result;
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
    public Object detectExplorer(){
        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        String ip = mapCacheUtil.getCacheItem("bindNode").toString().split(":")[0];
        String version = restTemplateUtil.getBrowserInfo(ip,8080);
        if(version != null){
            JSONObject jsonObject = JSONObject.parseObject(version);
            String ver = jsonObject.getString("data");
            result.setData(ver);
            result.setMessage("1");
        }else{
            result.setMessage("2");
        }
        result.setCode(ResultCode.SUCCESS);
        return result;
    }

    //启动浏览器
    @GetMapping(value = {"/startWeb"})
    public String startWeb(){
        String password = Constants.getSudoPassword();
//        String ip = LocalHostUtil.getLocalIP();
//        if (!StringUtils.isEmpty(ip)){
//            JavaShellUtil.replaceComposeShell(ip, password);
//        }
        javaShellUtil.ProcessBrowserShell(1,password);
        return "";
    }

    //验证sudo密码
    @GetMapping(value = {"/checkShell1"})
    public String checkShell(){
        String result = javaShellUtil.checkShell();
        return result;
    }

    //初始化节点
    @GetMapping(value = {"/initNode"})
    public String initNode(){
        try {
            String pwd = Constants.getSudoPassword();
            javaShellUtil.ProcessKillShell(2,pwd);
        }catch (Exception e) {
            e.printStackTrace();
//            throw e;
        }finally {
            System.out.close();
            System.err.close();
            Constants.delDir(new File(Constants.TDS_HOME),
                    x -> x.getName().startsWith("jdk")
                            || x.getName().equals("monitor.jar")
                            || x.getName().equals("sunflower-core.jar")
                            || x.getName().equals(".private-key")
            );
            // 5 秒后退出运维工具
            EXECUTOR.schedule(() -> System.exit(0), 7, TimeUnit.SECONDS);
        }
        return "";
    }

    //修改密码
    @GetMapping(value = {"/updatePassword"})
    public Object updatePassword(@RequestParam("password")String password) throws IOException {
        Result result = new Result();
        String filePath = Constants.SUDO_PASSWORD_PATH; // 文件路径
        FileWriter fileWriter =new FileWriter(filePath);
        fileWriter.write("");
        fileWriter.flush();
        write(filePath, read(filePath)); // 读取修改文件
        try {
            fileAppender(filePath, password);
            result.setCode(ResultCode.SUCCESS);
            return result;
        }catch (Exception e) {
            throw e;
        }
    }

    public String read(String filePath) {
        BufferedReader br = null;
        String line = null;
        StringBuffer buf = new StringBuffer();

        try {
            // 根据文件路径创建缓冲输入流
            br = new BufferedReader(new FileReader(filePath));

            // 循环读取文件的每一行, 对需要修改的行进行修改, 放入缓冲对象中
            while ((line = br.readLine()) != null) {
                // 此处根据实际需要修改某些行的内容
                if (line.startsWith("a")) {
                    buf.append(line).append(" start with a");
                }                 else if (line.startsWith("b")) {
                    buf.append(line).append(" start with b");
                }
                // 如果不用修改, 则按原来的内容回写
                else {
                    buf.append(line);
                }
                buf.append(System.getProperty("line.separator"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }

        return buf.toString();
    }

    public void write(String filePath, String content) {
        BufferedWriter bw = null;

        try {
            // 根据文件路径创建缓冲输出流
            bw = new BufferedWriter(new FileWriter(filePath));
            // 将内容写入文件中
            bw.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    bw = null;
                }
            }
        }
    }

    public void fileAppender(String fileName,String content) throws IOException{

        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        String line = null;
        // 一行一行的读
        StringBuilder sb = new StringBuilder();
        sb.append(content);
        while ((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append("\r\n");
        }
        reader.close();
        //写回去
        RandomAccessFile mm = new RandomAccessFile(fileName, "rw");
        mm.writeBytes(sb.toString());
        mm.close();
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

    //查看是否绑定节点
    @GetMapping(value = {"/nodeType"})
    public Object nodeType(){
        Result result = new Result();
        try{
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if(mapCacheUtil.getCacheItem("bindNode").toString() == null || mapCacheUtil.getCacheItem("bindNode").toString().isEmpty()){
                result.setMessage("");
                result.setCode(ResultCode.FAIL);
            }else{
                result.setCode(ResultCode.SUCCESS);
            }
        }catch (Exception e){
            result.setCode(ResultCode.FAIL);
        }
        return result;
    }

    //查看浏览器是否启动
    @GetMapping(value = {"/webStartOrNot"})
    public Object webStartOrNot(){
        Result result = new Result();
        try {
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if(mapCacheUtil.getCacheItem("bindNode").toString() == null || mapCacheUtil.getCacheItem("bindNode").toString().isEmpty()){
                result.setMessage("");
                result.setCode(ResultCode.Warn);
                return result;
            }
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
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    //查看浏览器容器是否清除
    @GetMapping(value = {"/webKillOrNot"})
    public Object webKillOrNot(){
        String password = Constants.getSudoPassword();
        String resu = javaShellUtil.exlporerShell(password);
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
