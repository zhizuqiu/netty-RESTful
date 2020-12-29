package com.github.zhizuqiu.nettyrestful.server.bean;

import java.lang.reflect.Method;

public class TemplateMethodValue {

    private Method method;
    private Object instance;

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
