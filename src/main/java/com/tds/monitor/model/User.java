package com.tds.monitor.model;

import lombok.Data;


@Data
public class User {
    private String name;
    private String password;
    private String role;
    private String loginTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public User() {
    }

    public User(String name, String password, String role,String loginTime) {
        this.name = name;
        this.role = role;
        this.password = password;
        this.loginTime = loginTime;
    }
}
