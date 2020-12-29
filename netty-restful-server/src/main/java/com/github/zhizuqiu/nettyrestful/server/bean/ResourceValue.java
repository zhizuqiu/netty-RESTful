package com.github.zhizuqiu.nettyrestful.server.bean;

public class ResourceValue {
    private String contentType;
    private String value;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ResourceValue(String contentType, String value) {

        this.contentType = contentType;
        this.value = value;
    }
}
