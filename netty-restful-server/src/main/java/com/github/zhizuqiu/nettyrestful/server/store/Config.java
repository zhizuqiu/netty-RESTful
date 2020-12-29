package com.github.zhizuqiu.nettyrestful.server.store;

public class Config {
    private String staticFilePath;

    public Config() {
        staticFilePath = null;
    }

    public Config(String staticFilePath) {
        this.staticFilePath = staticFilePath;
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
                '}';
    }
}
