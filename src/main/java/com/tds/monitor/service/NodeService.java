package com.tds.monitor.service;

import com.tds.monitor.model.Nodes;
import com.tds.monitor.model.User;

public interface NodeService {
    Object stop(String ipPort);
    Object restart(String ipPort);
    Object deleteData(long height);
    Nodes searchNode(String ipPort);
    User searchUser(String username);
    boolean updateUser(User user);
    boolean updateNode(Nodes nodes);


}
