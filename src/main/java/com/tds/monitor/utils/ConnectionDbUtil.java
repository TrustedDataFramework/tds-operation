package com.tds.monitor.utils;



import com.tds.monitor.model.Result;
import com.tds.monitor.model.ResultCode;
import lombok.extern.log4j.Log4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Log4j
public class ConnectionDbUtil {
    private static String url ="jdbc:postgresql://";
    private String endpoint;
    private String databaseName;
    private String dbUsername;
    private String dbPassword;
    private static Connection conn;
    private static Statement statement;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        ConnectionDbUtil.url = url;
    }

    public static Connection getConn() {
        return conn;
    }

    public static void setConn(Connection conn) {
        ConnectionDbUtil.conn = conn;
    }

    public static Statement getStatement() {
        return statement;
    }

    public static void setStatement(Statement statement) {
        ConnectionDbUtil.statement = statement;
    }

    public ConnectionDbUtil() {
        super();
    }
    public ConnectionDbUtil(String endpoint,String databaseName,String dbUsername,String dbPassword) {
        this.endpoint = endpoint;
        this.databaseName = databaseName;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
    }

    public Object login(){
        boolean flg=false;
        Result result = new Result();
        try {
            conn= DriverManager.getConnection(url+endpoint+"/"+databaseName, dbUsername, dbPassword);
            flg = true;
            if (flg){
                log.info("认证成功！");
            }
            statement = conn.createStatement();
        } catch (SQLException e) {
            result.setCode(ResultCode.FAIL);
            result.setMessage(e.getMessage());
            return result;
        }
        result.setCode(ResultCode.SUCCESS);
        result.setMessage("SUCCESS");
        return result;
    }
}
