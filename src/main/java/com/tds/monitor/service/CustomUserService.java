package com.tds.monitor.service;

public interface CustomUserService {
    String getRole();
    CustomUser getCustomUserByName(String name);
}
