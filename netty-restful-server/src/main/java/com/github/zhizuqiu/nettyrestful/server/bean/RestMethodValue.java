package com.github.zhizuqiu.nettyrestful.server.bean;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;

import java.lang.reflect.Method;

/**
 * 应用启动时，扫描注解后存入Map的value
 */
public class RestMethodValue {
    private HttpMap httpMap;
    private Method method;
    private Object instance;

    public RestMethodValue() {
    }

    public RestMethodValue(HttpMap httpMap, Method method, Object instance) {
        this.httpMap = httpMap;
        this.method = method;
        this.instance = instance;
    }

    public HttpMap getHttpMap() {
        return httpMap;
    }

    public void setHttpMap(HttpMap httpMap) {
        this.httpMap = httpMap;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }
}
