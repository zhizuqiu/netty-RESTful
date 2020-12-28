package com.github.zhizuqiu.nettyrestfulserver.bean;

import com.github.zhizuqiu.nettyrestfulcore.annotation.HttpMap;

import java.lang.reflect.Method;

/**
 * 应用启动时，扫描注解后存入Map的value
 */
public class RestMethodValue {
    private HttpMap httpMap;
    private Method method;
    private Object instance;

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
