package com.tds.monitor.utils;

import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Constants {
    // .tdos 目录
    public static final String TDS_HOME = Paths.get(System.getProperty("user.home"), ".tdos").toString();
    // etc 目录
    public static final String ETC_DIR  = Paths.get(TDS_HOME, "etc").toString();
    // tds 配置文件
    public static final String YML_PATH = Paths.get(ETC_DIR, "config.yml").toString();
    // tds 链 jar
    public static final String TDS_JAR_PATH = Paths.get(TDS_HOME, "sunflower-core.jar").toString();
    // 序列号文件
    public static final String SERIAL_PATH = Paths.get(ETC_DIR, ".serial").toString();
    // sudo 密码文件
    public static final String SUDO_PASSWORD_PATH = Paths.get(ETC_DIR, ".sudo").toString();
    // tds 链 pid 文件
    public static final String TDS_PID = Paths.get(ETC_DIR, ".pid").toString();

    @SneakyThrows
    public static String getSudoPassword(){
        return new String(Files.readAllBytes(Paths.get(SUDO_PASSWORD_PATH)));
    }

    // 获取 tds 链的 pid，如果不能存在返回 0
    @SneakyThrows
    public static int getTDSPid(){
        File pidFile = new File(TDS_PID);
        if(!pidFile.exists())
            return 0;
        String p = new String(Files.readAllBytes(Paths.get(TDS_PID)));
        return Integer.parseInt(p);
    }

    // 判断 tds 链是否在运行
//    public static boolean tdsIsRunning(){
//
//    }
}
