package com.tds.monitor;

import com.tds.monitor.utils.JavaShellUtil;
import lombok.extern.log4j.Log4j;
import org.hibernate.annotations.common.util.impl.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;

@Log4j
class MonitorApplicationTests {

    @Value("${password}")
    private String password;
    @Autowired
    private JdbcTemplate tmpl;
    @Test
    void contextLoads() throws IOException {
        String stopShell = "echo "+password+" |sudo -S docker stop 6c20180d9ee4";
        log.info(String.valueOf(JavaShellUtil.executeShell(stopShell)));
    }

}
