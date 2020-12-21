package com.tds.monitor.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;


@Component
public class   Monitor {
    private static final Logger logger = LoggerFactory.getLogger(Monitor.class);
    @Autowired
    public NodeServiceImpl nodeService;

    @Autowired
    public RestTemplateUtil restTemplateUtil;


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
                    if(proposersList.size()<3){
                        return APIResult.newSuccess(1);
                    }
                    for (String str : proposersList) {
                        String proposersBlockHash = getBlockHash(str, height);
                        if (StringUtils.isEmpty(proposersBlockHash)){
                            return "";
                        }
                        if(proposersBlockHash != null){
                            if (proposersBlockHash.equals(nBlockhash)) {
                                confirmNum++;
                            }
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
                if (ismail){
                    StringBuffer messageText=new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
                    messageText.append("<span>通知:</span></br>");
                    messageText.append("<span>您绑定的节点出现分叉！</span></br>");
                    new SendMailUtil().sendMailOutLook("通知",messageText.toString());
                }
            }
        }
    }

    //卡块监测
    public static int checkBlockIsStuck(boolean ismail){
        try {
            MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
            if (mapCacheUtil.getCacheItem("bindNode") != null) {
                //获取当前高度
                String ip = mapCacheUtil.getCacheItem("bindNode").toString();
                String heightUrl = "http://" + ip + "/rpc/stat";
                JSONObject result = JSON.parseObject(HttpRequestUtil.sendGet(heightUrl, ""));
                if(result == null){
                    return -1;
                }
                JSONObject result1 = result.getJSONObject("data");
                if (result1 == null)
                    return -1;
                if (mapCacheUtil.getCacheItem("BlockHeight") == null) {
                    if ((int) result.get("code") == 200) {
                        mapCacheUtil.putCacheItem("BlockHeight", result1.getLong("height"));
                    }
                    return 0;
                }
                String height = mapCacheUtil.getCacheItem("BlockHeight").toString();
                if (!height.equals(result1.getLong("height"))) {
                    return 1;
                }else {
                    if (ismail){
                        StringBuffer messageText=new StringBuffer();//内容以html格式发送,防止被当成垃圾邮件
                        messageText.append("<span>警告:</span></br>");
                        messageText.append("<span>您绑定的节点存在卡块风险！请检查！！！</span></br>");
                        new SendMailUtil().sendMailOutLook("通知",messageText.toString());
                    }
                    return -1;
                }
            }
            mapCacheUtil.removeCacheItem("BlockHeight");
            return -1;
        }catch (Exception e){
            throw e;
        }
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
                recoveryBifurcate(ismail);
                checkBlockIsStuck(ismail);
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
