package com.tds.monitor.quartz;

import com.alibaba.fastjson.JSONObject;
import com.tds.monitor.service.Impl.ApplicationRunnerImpl;
import com.tds.monitor.utils.Constants;
import com.tds.monitor.utils.JavaShellUtil;
import com.tds.monitor.utils.LocalHostUtil;
import com.tds.monitor.utils.RestTemplateUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Log4j
public class searchOSSFileJob {

    @Autowired
    RestTemplateUtil restTemplateUtil;

    private boolean isLock = false;

    @Autowired
    ApplicationRunnerImpl applicationRunnerImpl;
    @Scheduled(cron = "0 0 */3 * * ?")//每三小时执行一次
    public void searchWhiteArray() throws IOException {
        try {
            if (!isLock) {
                // 加锁，防止定时器重复执行，
                isLock = true;
                RestTemplateUtil restTemplateUtil = new RestTemplateUtil();
                log.info("=============开始读取oss文件");
                String ip = LocalHostUtil.getLocalIP();
                log.info("=============查看节点是否启动");
                String result = restTemplateUtil.getBrowserInfo(ip, 7010);
                log.info("=============节点已经启动，查看注册码");
                if (result != null || !result.equals("")) {
                    //获取路径注册码
                    //获取注册码
                    byte[] all = Files.readAllBytes(Paths.get(Constants.ETC_DIR, ".serial"));
                    String mes = new String(all).trim();
                    //获取白名单
                    log.info("==============================获取白名单");
                    JSONObject jsonObject = ApplicationRunnerImpl.getWhiteArrays();
                    List list = new ArrayList();
                    for (Object o : jsonObject.getJSONArray("whiteArrays")) {
                        list.add(o);
                    }
                    if (list.contains(mes) && list.size() > 0) {
                        log.info("==============================在白名单列表");
                        //获取系统时间
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                        Date localTime = df.parse(df.format(new Date()));
                        Date endTime = df.parse(jsonObject.getString("endTime"));
                        if (localTime.after(endTime)) {
                            String kill = JavaShellUtil.ProcessKillShell("sunflower");
                            if (kill != null || !kill.equals("")) {
                                log.info("==============停止节点成功");
                            }
                        }
                    }
                }
            }
        }catch (Exception e) {
            log.error( "停止失败");
        }
        isLock = false;
    }
}
