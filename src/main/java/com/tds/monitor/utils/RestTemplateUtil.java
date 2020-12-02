package com.tds.monitor.utils;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateUtil {

    public RestTemplate restTemplate;

    private SimpleClientHttpRequestFactory simpleClientHttpRequestFactory;

    public RestTemplateUtil() {
        this.simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setReadTimeout(8 * 1000);
        simpleClientHttpRequestFactory.setConnectTimeout(10 * 1000);
        this.restTemplate = new RestTemplate(this.simpleClientHttpRequestFactory);
    }


    //查看节点是否启动 本地节点默认端口7010
    public JSONObject getNodeInfo(String url, long port) {
        url = "http://" + url + ":" + port + "/rpc/stat";
        JSONObject result;
        try {
            result = restTemplate.getForObject(url, JSONObject.class);
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    //查看浏览器后端是否启动，默认端口8181
    public String getBrowserInfo(String url, long port) {
        url = "http://" + url + ":" + port + "/version";
        String result;
        try {
            result = restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            return null;
        }
        return result;
    }
}
