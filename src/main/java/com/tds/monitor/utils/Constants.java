package com.tds.monitor.utils;

import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Predicate;

public class Constants {
    // 递归删除，Predicate 为 true 的内容不会被删除
    public static void delDir(File f, Predicate<File> excludes) {
        // 判断是否是一个目录, 不是的话跳过, 直接删除; 如果是一个目录, 先将其内容清空.
        if(excludes.test(f)){
            return;
        }

        if(f.isDirectory()) {
            // 获取子文件/目录
            File[] subFiles = f.listFiles();
            // 遍历该目录
            for (File subFile : subFiles) {
                // 递归调用删除该文件: 如果这是一个空目录或文件, 一次递归就可删除. 如果这是一个非空目录, 多次
                // 递归清空其内容后再删除
                delDir(subFile, excludes);
            }
        }
        // 删除空目录或文件
        try{
            f.delete();
        }catch (Exception ignored){

        }
    }
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
    // tds 日志
    public static final String TDS_LOG = Paths.get(TDS_HOME, ".log").toString();
    // tds 错误输出
    public static final String TDS_ERROR = Paths.get(TDS_HOME, ".err").toString();

    public static final String JAVA_HOME = Paths.get(TDS_HOME, "jdk-11.0.2").toString();

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
