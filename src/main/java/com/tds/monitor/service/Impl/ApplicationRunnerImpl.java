package com.tds.monitor.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.monitor.dao.NodeDao;
import com.tds.monitor.model.Meaasge;
import com.tds.monitor.model.Nodes;
import com.tds.monitor.model.Result;
import com.tds.monitor.model.ResultCode;
import com.tds.monitor.utils.ConnectionUtil;
import com.tds.monitor.utils.MapCacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ApplicationRunnerImpl implements ApplicationRunner {
    @Value(value = "classpath:a.json")
    private Resource resource;

    @Autowired
    private NodeDao nodeDao;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("通过实现ApplicationRunner接口，在spring boot项目启动后打印参数");
        BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        StringBuffer message=new StringBuffer();
        String line = null;
        while((line = br.readLine()) != null) {
            message.append(line);
        }
        String defaultString=message.toString();
        String result=defaultString.replace("\r\n", "").replaceAll(" +", "");
        System.out.println(result);
        //获取指定路径
        System.getProperty("home.dir");
        String path;
        System.out.println(System.getProperty("home.dir"));
        path = System.getProperty("home.dir") + ".tdos" + "/etc/docker-compose.yml";
        System.out.println(path);
//        JSONObject jsonObject = JSONObject.parseObject(result);
//        String ip = jsonObject.getString("ip");
//        String port = jsonObject.getString("port");
//        String type = jsonObject.getString("type");
//        String nodeStr = ip+":"+port;
//        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
//        if (mapCacheUtil.getCacheItem("bindNode") == null){
//            mapCacheUtil.putCacheItem("bindNode",nodeStr);
//        }else {
//            mapCacheUtil.removeCacheItem("bindNode");
//            mapCacheUtil.putCacheItem("bindNode",nodeStr);
//        }
//        System.out.println("12111");
//        Nodes node = new Nodes();
//        node.setNodeIP(ip);
//        node.setNodePort(port);
//        node.setNodeType(type);
//        node.setUserName("");
//        node.setPassword("");
//        node.setUserName("");
//        node.setNodeState("");
//        node.setNodeVersion("");
//        node.setPassword("");
//        node.setLeveldbPath("");
//        if (!nodeDao.findNodesByNodeIPAndNodePort(ip,port).isPresent()){
//            System.out.println("121");
//            nodeDao.save(node);
//        }else{
//            nodeDao.deleteAll();
//            System.out.println("12");
//        }
    }

    private static String pushUrl= "https://tdos-store.oss-cn-beijing.aliyuncs.com/whiteList.json";

    private List getWhiteArrays(){
        List list = new ArrayList();
        try {
            URL url = new URL(pushUrl);
            //System.out.println("访问路径"+pushUrl);
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(1000);  //读取超时，时限1秒
            conn.setConnectTimeout(1000);  //链接超时，时限1秒

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
            StringBuffer receive = new StringBuffer();
            receive.append(reader.readLine());
            JSONObject jsonObject =JSONObject.parseObject(receive.toString());
            for(Object o :jsonObject.getJSONArray("whiteArrays")){
                list.add(o);
            }
            reader.close();//读取关闭
            conn.connect();  //链接关闭
        } catch (Exception e) {
            e.printStackTrace();//这里抓取的异常范围比较大，是异常就抛出
        }
        return list;
    }

}