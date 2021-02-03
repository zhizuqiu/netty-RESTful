package com.github.zhizuqiu.nettyrestful.server.store;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private String staticFilePath;
    private List<String> restfulPreProxy;

    public Config() {
        staticFilePath = null;
        restfulPreProxy = new ArrayList<>();
    }

    public List<String> getRestfulPreProxy() {
        return restfulPreProxy;
    }

    public void setRestfulPreProxy(List<String> restfulPreProxy) {
        this.restfulPreProxy = restfulPreProxy;
    }

    public String getStaticFilePath() {
        return staticFilePath;
    }

    public void setStaticFilePath(String staticFilePath) {
        this.staticFilePath = staticFilePath;
    }

    @Override
    public String toString() {
        return "Config{" +
                "staticFilePath='" + staticFilePath + '\'' +
                ", restfulPreProxy=" + restfulPreProxy +
                '}';
    }
}
