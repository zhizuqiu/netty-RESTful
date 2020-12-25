package com.github.zhizuqiu.nettyrestfulclient.request;

import com.github.zhizuqiu.nettyrestfulcommon.annotation.HttpMap;
import io.netty.handler.codec.http.HttpMethod;

import java.lang.reflect.Method;

public class Request {
    private final Method method;
    private final String preProxy;

    public Request(Method method, String preProxy) {
        this.method = method;
        this.preProxy = preProxy;
    }

    public HttpMethod getHttpMethod() {
        HttpMap httpMap = method.getAnnotation(HttpMap.class);
        if (httpMap.method() == HttpMap.Method.GET) {
            return HttpMethod.GET;
        } else if (httpMap.method() == HttpMap.Method.POST) {
            return HttpMethod.POST;
        } else if (httpMap.method() == HttpMap.Method.DELETE) {
            return HttpMethod.DELETE;
        } else if (httpMap.method() == HttpMap.Method.PUT) {
            return HttpMethod.PUT;
        }
        return HttpMethod.GET;
    }

    public String getUrl() {
        HttpMap httpMap = method.getAnnotation(HttpMap.class);
        return this.preProxy + httpMap.path();
    }

    public HttpMap.ParamType getParamType() {
        HttpMap httpMap = method.getAnnotation(HttpMap.class);
        return httpMap.paramType();
    }

    public Method getMethod() {
        return method;
    }

}
