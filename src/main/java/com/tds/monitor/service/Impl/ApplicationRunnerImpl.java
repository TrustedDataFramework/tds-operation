package com.tds.monitor.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tds.monitor.dao.NodeDao;
import com.tds.monitor.model.Nodes;
import com.tds.monitor.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class ApplicationRunnerImpl implements ApplicationRunner {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    RestTemplateUtil restTemplateUtil;

    JavaShellUtil javaShellUtil;

    private static String pushUrl = "https://tdos-store.oss-cn-beijing.aliyuncs.com/whiteList.json";

    public static String getJavaBin(){
        return Paths.get(Constants.JAVA_HOME, "bin", "java").toString();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("通过实现ApplicationRunner接口，在spring boot项目启动后打印参数");
        String ip = LocalHostUtil.getLocalIP();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        String bind = ip + ":7010";
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            mapCacheUtil.removeCacheItem("bindNode");
        }
        mapCacheUtil.putCacheItem("bindNode",bind);
        //查看节点是否启动
        JSONObject jsonObject = restTemplateUtil.getNodeInfo(ip, 7010);
        if (jsonObject == null) {
            log.info("==============================获取注册码");
            //获取注册码
            byte[] all = Files.readAllBytes(Paths.get(Constants.ETC_DIR, ".serial"));
            String mes = new String(all).trim();
            //获取白名单
            log.info("==============================获取白名单");
            JSONObject json = getWhiteArrays();
            List list = new ArrayList();
            for (Object o : json.getJSONArray("whiteArrays")) {
                list.add(o);
            }
            if (list.contains(mes) && list.size() > 0) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                Date localTime = df.parse(df.format(new Date()));
                Date endTime = df.parse(json.getString("endTime"));
                if (localTime.before(endTime)) {
                    log.info("==============================启动节点");
                    String[] cmds = new String[]{
                            "nohup", getJavaBin(), "-jar", Constants.TDS_JAR_PATH,
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
                    //获取节点数据
                    log.info("==============================获取节点数据");
                    byte[] message = Files.readAllBytes(Paths.get(System.getProperty("user.home"), ".tdos", "etc", "config.json"));
                    String mess = new String(message);
                    String mining;
                    if (JSON.parseObject(mess).getString("mining").equals("true")) {
                        mining = "2";
                    } else {
                        mining = "1";
                    }
                    Nodes node = new Nodes();
                    log.info("==============================获取节点到数据");
                    node.setNodeIP(ip);
                    node.setNodePort("7010");
                    node.setNodeType(mining);
                    if (!nodeDao.findNodesByNodeIPAndNodePort(ip, "7010").isPresent()) {
                        log.info("=============================保存节点信息");
                        nodeDao.save(node);
                    }
                }
            }
        }
//        //获取密码,启动浏览器
//        log.info("=============================启动浏览器");
//        String password = Constants.getSudoPassword();
//        javaShellUtil.ProcessBrowserShell(1,password);
    }


    public static JSONObject getWhiteArrays() {
        JSONObject jsonObject = new JSONObject();
        try {
            URL url = new URL(pushUrl);
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(1000);  //读取超时，时限1秒
            conn.setConnectTimeout(1000);  //链接超时，时限1秒

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuffer receive = new StringBuffer();
            receive.append(reader.readLine());
            jsonObject = JSONObject.parseObject(receive.toString());
            reader.close();//读取关闭
            conn.connect();  //链接关闭
        } catch (Exception e) {
            e.printStackTrace();//这里抓取的异常范围比较大，是异常就抛出
        }
        return jsonObject;
    }
}