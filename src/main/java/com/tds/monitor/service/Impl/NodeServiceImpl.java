package com.tds.monitor.service.Impl;

import com.tds.monitor.dao.NodeDao;
import com.tds.monitor.dao.UserDao;
import com.tds.monitor.model.Nodes;
import com.tds.monitor.model.Result;
import com.tds.monitor.model.ResultCode;
import com.tds.monitor.model.User;
import com.tds.monitor.utils.ConnectionDbUtil;
import com.tds.monitor.utils.ConnectionUtil;
import com.tds.monitor.utils.Constants;
import com.tds.monitor.utils.MapCacheUtil;
import com.tds.monitor.service.NodeService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Service
public class NodeServiceImpl implements NodeService {
    @Value("${Image}")
    private String image;
    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private UserDao userDao;

    @Override
    public Object stop(String ipPort) {
        Result result = new Result();
        try {
            Nodes node = nodeDao.findNodesByNodeIPAndNodePort(ipPort.split(":")[0], ipPort.split(":")[1]).get();
            String username = node.getUserName();
            String usepassword = node.getPassword();
            String ip = node.getNodeIP();
            if (username == null || usepassword == null) {
                result.setMessage("失败，请完善远程连接信息。");
                result.setCode(ResultCode.FAIL);
                return result;
            }
            ConnectionUtil connectionUtil = new ConnectionUtil(ip, username, usepassword);
            if (connectionUtil.login()) {
                String userHome = getUserHome(ip, username, usepassword);
                String shellResult = connectionUtil.execute("echo " + usepassword + " |sudo -S "  + userHome + "/.tdos/etc/kill.sh 1");
                if (StringUtils.isBlank(shellResult)) {
                    result.setMessage("停止失败");
                    result.setCode(ResultCode.FAIL);
                    return result;
                }
                result.setMessage("成功");
                result.setCode(ResultCode.SUCCESS);
            } else {
                result.setMessage("连接失败");
                result.setCode(ResultCode.FAIL);
            }
            return result;
        } catch (Exception e) {
            result.setMessage("错误");
            result.setCode(ResultCode.FAIL);
            return result;
        }
    }

    public String getUserHome(String ip,String username,String usepassword) {
        ConnectionUtil connectionUtil = new ConnectionUtil(ip, username, usepassword);
        if (connectionUtil.login()) {
            String result = connectionUtil.execute("echo " + usepassword + " |sudo -S " + "env|grep ^HOME= | cut -c 6-");
            result = result.substring(0, result.indexOf("\n"));
            return result;
        }
        return "";
    }

    @Override
    public Object restart(String ipPort) {
        Result result = new Result();
        try {
            Nodes node = nodeDao.findNodesByNodeIPAndNodePort(ipPort.split(":")[0], ipPort.split(":")[1]).get();
            String username = node.getUserName();
            String usepassword = node.getPassword();
            if(usepassword.isEmpty()){
                usepassword = Constants.getSudoPassword();
            }
            String ip = node.getNodeIP();
            if (username == null || usepassword == null || username.isEmpty() || usepassword.isEmpty()) {
                result.setMessage("失败，请完善远程连接信息。");
                result.setCode(ResultCode.FAIL);
                return result;
            }
            ConnectionUtil connectionUtil = new ConnectionUtil(ip, username, usepassword);
            if (connectionUtil.login()) {
                String userHome = getUserHome(ip, username, usepassword);
                String shellResult = connectionUtil.execute("echo " + usepassword + " | sudo -S "  + userHome + "/.tdos/etc/kill.sh 1");
                if (StringUtils.isNotBlank(shellResult)) {
                    String[] cmds = new String[]{
                            "nohup", ApplicationRunnerImpl.getJavaBin1(userHome), "-jar",userHome+"/.tdos/sunflower-core.jar",
                            "--spring.config.location=" + userHome+"/.tdos/etc/config.yml",
                    };
                    Thread t = new Thread(() -> {
                        try {
                            connectionUtil.executeSuccess(StringUtils.join(cmds," "));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    t.start();
                    result.setMessage("成功");
                    result.setCode(ResultCode.SUCCESS);
                    return result;
                }
            } else {
                result.setMessage("连接失败");
                result.setCode(ResultCode.FAIL);
                return result;
            }


        } catch (Exception e) {
            result.setMessage("错误");
            result.setCode(ResultCode.FAIL);
            return result;
        }
        result.setMessage("失败");
        result.setCode(ResultCode.FAIL);
        return result;
    }

    @Override
    public Object deleteData(long height) {
        Result result = new Result();
        MapCacheUtil mapCacheUtil = MapCacheUtil.getInstance();
        if (mapCacheUtil.getCacheItem("bindNode") != null) {
            try {
                GetNodeinfo getNodeinfo = new GetNodeinfo(mapCacheUtil.getCacheItem("bindNode").toString()).invoke();
                ConnectionDbUtil connectionDbUtil = new ConnectionDbUtil(getNodeinfo.getDbip() + ":" + getNodeinfo.getDbPort(), getNodeinfo.getDbname(), getNodeinfo.getDbusername(), getNodeinfo.getDbpassword());
                Result connectResult = (Result) connectionDbUtil.login();
                if (connectResult.getCode() == 2000) {
                    String sql = "delete from transaction t where t.tx_hash in(select h.tx_hash from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height>=" + height + "))";
                    connectionDbUtil.getStatement().executeUpdate(sql);
                    String sql2 = "delete from transaction_index h where h.block_hash in(select h.block_hash from header h where h.height>=" + height + ")";
                    connectionDbUtil.getStatement().executeUpdate(sql2);
                    String sql3 = "delete from header h where h.height>=" + height;
                    connectionDbUtil.getStatement().executeUpdate(sql3);
                    result.setMessage("成功");
                    result.setCode(ResultCode.SUCCESS);
                    return result;
                } else {
                    logger.error("deleteData warn:connection failed");
                    result.setMessage("数据库连接失败");
                    result.setCode(ResultCode.FAIL);
                    return result;
                }

            } catch (IOException e) {
                logger.error("deleteData error:" + e.getMessage());
                result.setMessage("失败");
                result.setCode(ResultCode.FAIL);
                return result;
            } catch (SQLException e) {
                logger.error("deleteData SQL error:" + e.getMessage());
                result.setMessage("失败");
                result.setCode(ResultCode.FAIL);
                return result;
            }
        }

        result.setMessage("请绑定节点");
        result.setCode(ResultCode.FAIL);
        logger.warn("deleteData warn:please Bind node");
        return result;
    }

    @Override
    public Nodes searchNode(String ipPort) {
        if (nodeDao.findNodesByNodeIPAndNodePort(ipPort.split(":")[0], ipPort.split(":")[1]).isPresent()) {
            return nodeDao.findNodesByNodeIPAndNodePort(ipPort.split(":")[0], ipPort.split(":")[1]).get();
        }
        return new Nodes();
    }

    @Override
    public User searchUser(String username) {
        if (userDao.findByName(username).isPresent()) {
            return userDao.findByName(username).get();
        }
        return new User();
    }

    @Override
    public boolean updateUser(User user) {
        boolean t = false;
        if (userDao.findByName(user.getName()).isPresent()) {
            user.setId(userDao.findByName(user.getName()).get().getId());
            userDao.save(user);
            t = true;
        }
        return t;
    }


    @Override
    public boolean updateNode(Nodes nodes) {
        boolean t = false;
        if (nodeDao.findNodesByNodeIPAndNodePort(nodes.getNodeIP(), nodes.getNodePort()).isPresent()) {
            nodes.setId(nodeDao.findNodesByNodeIPAndNodePort(nodes.getNodeIP(), nodes.getNodePort()).get().getId());
            nodeDao.save(nodes);
            t = true;
        }
        return t;
    }


    private class GetNodeinfo {
        private String ipPort;
        private String username;
        private String usepassword;
        private String ip;
        private String dbip;
        private String dbPort;
        private String dbname;
        private String dbusername;
        private String dbpassword;

        public GetNodeinfo(String ipPort) {
            this.ipPort = ipPort;
        }

        public String getUsername() {
            return username;
        }

        public String getUsepassword() {
            return usepassword;
        }

        public String getIp() {
            return ip;
        }

        public String getIpPort() {
            return ipPort;
        }

        public String getDbip() {
            return dbip;
        }

        public String getDbPort() {
            return dbPort;
        }

        public String getDbname() {
            return dbname;
        }

        public String getDbusername() {
            return dbusername;
        }

        public String getDbpassword() {
            return dbpassword;
        }

        public GetNodeinfo invoke() throws IOException {
            List<Nodes> nodeList = nodeDao.findAll();
            ip = null;
            if (nodeList != null) {
                for (int i = 0; i < nodeList.size(); i++) {
//                    if (nodeList.get(i).getNodeIP().equals(ipPort.split(":")[0]) && nodeList.get(i).getNodePort().equals(ipPort.split(":")[1])) {
//                        ip = nodeList.get(i).getNodeIP();
//                        username = nodeList.get(i).getUserName();
//                        usepassword = nodeList.get(i).getPassword();
//                        break;
//                    }
                }
            }
            return this;
        }
    }
}
