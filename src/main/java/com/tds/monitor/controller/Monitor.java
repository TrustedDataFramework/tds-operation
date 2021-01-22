package com.tds.monitor.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tds.monitor.dao.MailDao;
import com.tds.monitor.model.Mail;
import com.tds.monitor.service.Impl.NodeServiceImpl;
import com.tds.monitor.utils.*;
import com.tds.monitor.utils.ApiResult.APIResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;


@Component
public class  Monitor {
    private static final Logger logger = LoggerFactory.getLogger(Monitor.class);
    @Autowired
    public NodeServiceImpl nodeService;

    @Autowired
    public RestTemplateUtil restTemplateUtil;

    @Autowired
    private MailDao mailDao;



    //分叉监测
    public Object checkBifurcate()           {
        try {
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            String ip = "";
            if (mapCacheUtil.getCacheItem("bindNode") != null){
                ip = mapCacheUtil.getCacheItem("bindNode").toString();
                //获取当前高度
                String heightUrl = "http://"+ip+"/rpc/stat";
                JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl,""));
                if(result != null){
                    JSONObject result1 = result.getJSONObject("data");
                    Long height = result1.getLong("height");
                    String nBlockhash = getBlockHash(ip,height);
                    if (StringUtils.isEmpty(nBlockhash)){
                        return "";
                    }
                    int confirmNum =0;
                    List<String> proposersList = getPeers();
                    if(proposersList.size()<2){
                        return APIResult.newSuccess(1);
                    }
                    for (String str : proposersList) {
                        String proposersBlockHash = getBlockHash(str, height);
                        if (proposersBlockHash == null || StringUtils.isEmpty(proposersBlockHash)){
                            return "";
                        }
                        if (proposersBlockHash.equals(nBlockhash)) {
                            confirmNum++;
                        }
                    }
                    //不满足2/3一致则删除对于高度的区块
                    if (divisionRoundingUp(proposersList.size() * 2, 3) > confirmNum)
                        return APIResult.newFailResult(-1,height.toString());
                }
                return APIResult.newSuccess(1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return APIResult.newFailResult(0,"Please bind node");
    }

    //分叉修复
    public  void recoveryBifurcate(boolean ismail){
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            String ip = mapCacheUtil.getCacheItem("bindNode").toString();
            //获取当前高度
            String heightUrl = "http://"+ip+"/rpc/stat";
            JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl,""));
            JSONObject result1 = result.getJSONObject("data");
            Long nHeight = 1L;
            if(result1 != null) {
                nHeight  = result1.getLong("height");
            }
            if(new ObjectMapper().convertValue(checkBifurcate(),APIResult.class).getCode() == -1){
                logger.info("Wrong Block Height: "+ nHeight);
                ismail = true;
                if (ismail){
                    List<Mail> mail = mailDao.findAll();
                    Mail mail1 = mail.get(0);
                    StringBuffer messageText=new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
                    messageText.append("<span>通知:</span></br>");
                    messageText.append("<span>您绑定的节点出现分叉！</span></br>");
                    new SendMailUtil().sendMailOutLook("通知",messageText.toString(),mail1);
                }
            }
        }
    }
//
//    public static void main(String[] args) throws ParseException {
//        String heightUrl = "http://192.168.1.62:7010/rpc/block/-1";
//        JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl, ""));
//        JSONObject result1 = result.getJSONObject("data");
//        System.out.println(result1.getString("createdAt"));
//
//            Instant ins = Instant.parse("2021-01-21T09:29:38Z");
//            // unix 秒
//        System.out.println(ins.getEpochSecond());
//        long now = System.currentTimeMillis() / 1000;
//        System.out.println(now - ins.getEpochSecond());
//    }

    //卡块监测
    public static int checkBlockIsStuck(boolean ismail,Mail mail1){
        try {
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if (mapCacheUtil.getCacheItem("bindNode") != null) {
                //获取当前高度
                String ip = mapCacheUtil.getCacheItem("bindNode").toString();
                String heightUrl = "http://" + ip + "/rpc/stat";
                JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl, ""));
                if (result == null) {
                    return 0;
                }
                JSONObject resultAee = result.getJSONObject("data");
                String allowUnauthorized = resultAee.getString("allowUnauthorized");
                int averageBlockInterval = resultAee.getInteger("averageBlockInterval");
                String timeUrl = "http://" + ip + "/rpc/block/-1";
                JSONObject resultTime = JSON.parseObject(HttpRequestUtil.sendGet(timeUrl, ""));
                JSONObject result1 = resultTime.getJSONObject("data");
                if (result1 == null) {
                    return 0;
                }
                if (!allowUnauthorized.equals("true")) {
                    return 1;
                } else {
                    String time = result1.getString("createdAt");
                    Instant ins = Instant.parse(time);
                    // unix 秒
                    long now = System.currentTimeMillis() / 1000;
                    if(now - ins.getEpochSecond()>averageBlockInterval*10){
                        ismail = true;
                    }
                    if (ismail) {
                        StringBuffer messageText = new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
                        messageText.append("<span>警告:</span></br>");
                        messageText.append("<span>您绑定的节点存在卡块风险！请检查！！！</span></br>");
                        new SendMailUtil().sendMailOutLook("通知", messageText.toString(),mail1);
                        return -1;
                    }
                }
            }
        }catch (Exception e){
            throw e;
        }
        return 0;
    }

    //整体监测

    @Scheduled(cron="*/5 * *  * * ? ")
    public void monitorStatus() throws IOException {
        boolean ismail = false;
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            String[] bindNodeiphost = mapCacheUtil.getCacheItem("bindNode").toString().split(":");
            JSONObject result = restTemplateUtil.getNodeInfo(bindNodeiphost[0],7010);
            if (result != null && !result.equals("")) {
                List<Mail> mail = mailDao.findAll();
                Mail mail1 = mail.get(0);
                recoveryBifurcate(ismail);
                checkBlockIsStuck(ismail,mail1);
            }
        }
    }



    public  List<String> getPeers(){
        List<String> list = new ArrayList<>();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            //获取当前高度
            String ip = mapCacheUtil.getCacheItem("bindNode").toString();
            String url = "http://" + ip + "/rpc/peers";
            JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(url, ""));
            if (result != null) {
                JSONObject data = result.getJSONObject("data");
                JSONArray peersArray = data.getJSONArray("peers");
                if (peersArray.size() > 0) {
                    for (int i = 0;i<peersArray.size();i++) {
                        JSONObject jsonObject = (JSONObject) peersArray.get(i);
                        list.add(jsonObject.getString("host")+":7010");
                    }
                }
            }
        }

        return list;
    }

    public String getBlockHash(String ip,Long height){
        String blockHash = "";
        try {
            String blockUrl = "http://"+ip+"/rpc/block/"+height;
            JSONObject jsonObject= JSON.parseObject(HttpRequestUtil.sendGet(blockUrl,""));
            if(jsonObject != null){
                JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                blockHash = (String) jsonObject1.get("hash");
            }
        }catch (Exception e){
//            e.printStackTrace();
            return blockHash;
        }

        return blockHash;
    }

    public static int divisionRoundingUp(int divisor, int dividend){
        Float number = Float.valueOf(divisor)/Float.valueOf(dividend);
        return (int)Math.ceil(number);

    }


}
