package com.tds.monitor.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.monitor.leveldb.Leveldb;
import com.tds.monitor.model.Mail;
import com.tds.monitor.service.Impl.NodeServiceImpl;
import com.tds.monitor.service.TransactionService;
import com.tds.monitor.utils.HttpRequestUtil;
import com.tds.monitor.utils.MapCacheUtil;
import com.tds.monitor.utils.SendMailUtil;
import com.tds.monitor.utils.*;
import com.tds.monitor.utils.ApiResult.APIResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;


@Component
public class   Monitor {
    private static final Logger logger = LoggerFactory.getLogger(Monitor.class);
    @Autowired
    private Leveldb leveldb;
    @Autowired
    TransactionService transactionService;
    @Value("${Image}")
    private String image;
    @Autowired
    private JdbcTemplate tmpl;
    @Autowired
    public NodeServiceImpl nodeService;

    //卡块预警
    public static int recoveryBifurcate(boolean ismail){
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            //获取当前高度
            String ip = mapCacheUtil.getCacheItem("bindNode").toString();
            String heightUrl = "http://" + ip + "/rpc/stat";
            JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl, ""));
            if (result == null)
                return -1;
            if (result.getInteger("code") == 200) {
                return 1;
            }else {
                if (ismail){
                    StringBuffer messageText=new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
                    messageText.append("<span>警告:</span></br>");
                    messageText.append("<span>您绑定的节点存在卡块风险！请检查！！！</span></br>");
                    try {
                        SendMailUtil.sendMailOutLook("通知",messageText.toString());
                    } catch (IOException e) {
                        logger.error("IOException when sendEmail",e);
                    }
                }
                return -1;
            }
        }
        return -1;
    }

    //整体监测

    @Scheduled(cron="0/5 * *  * * ? ")
    public void monitorStatus() throws IOException {
        boolean ismail = false;
        Leveldb leveldb = new Leveldb();
        Mail mail = new Mail();
        Object read = JSONObject.parseObject(leveldb.readAccountFromSnapshot("mail"), Mail.class);
        if (read != null) {
            mail= (Mail) read;
            ismail = true;
        }
        recoveryBifurcate(ismail);
    }



    public  List<String> getPeers(){
        List<String> list = new ArrayList<>();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            //获取当前高度
            String ip = mapCacheUtil.getCacheItem("bindNode").toString();
            String url = "http://" + ip + "/peers/status";
            JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(url, ""));

            if (result != null) {
                JSONObject data = result.getJSONObject("data");
                JSONArray peersArray = data.getJSONArray("peers");
                if (peersArray.size() > 0) {
                    for (Object s : peersArray) {
                        list.add(s.toString().substring(s.toString().indexOf("@") + 1, s.toString().length() - 5) + ":19585");
                    }
                }
            }
        }

        return list;
    }

    public String getBlockHash(String ip,Long height){
        String blockHash = " ";
        try {
            String blockUrl = "http://"+ip+"/block/"+height;
            JSONObject jsonObject= JSON.parseObject(HttpRequestUtil.sendGet(blockUrl,""));
            if(jsonObject != null){
                blockHash = (String) jsonObject.get("blockHash");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return blockHash;
    }

    public static int divisionRoundingUp(int divisor, int dividend){
        Float number = Float.valueOf(divisor)/Float.valueOf(dividend);
        return (int)Math.ceil(number);

    }
}
