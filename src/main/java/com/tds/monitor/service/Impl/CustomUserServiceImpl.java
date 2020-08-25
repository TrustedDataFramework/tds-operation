package com.tds.monitor.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.tds.monitor.model.User;
import com.tds.monitor.service.CustomUserService;
import com.tds.monitor.leveldb.Leveldb;
import com.tds.monitor.service.CustomUser;
import com.tds.monitor.service.Database;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CustomUserServiceImpl implements CustomUserService {


    @Override
    public String getRole() {
        Object[] list= ((CustomUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getAuthorities().toArray();
        return ((SimpleGrantedAuthority) list[0]).getAuthority();
    }

    @Override
    public CustomUser getCustomUserByName(String name) {

        try {
            Database database = new Database();
            Leveldb leveldb = new Leveldb();
            List<User> userList = JSONObject.parseArray(leveldb.readAccountFromSnapshot("user"), User.class);
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getName().equals(name)) {
                    User user = userList.get(i);
                    CustomUser customUser = new CustomUser(0, user.getName(), user.getPassword(), database.getGrants(user.getRole()));
                    return customUser;
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}
