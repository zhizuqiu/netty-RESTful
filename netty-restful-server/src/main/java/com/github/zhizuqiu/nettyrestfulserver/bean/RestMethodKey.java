package com.github.zhizuqiu.nettyrestfulserver.bean;


import com.github.zhizuqiu.nettyrestfulcommon.annotation.HttpMap;

/**
 * 应用启动时，扫描注解后存入Map的key
 */
public class RestMethodKey {
    private String url;
    private HttpMap.Method method;
    private HttpMap.ParamType paramType;

    public RestMethodKey(String url, HttpMap.Method method, HttpMap.ParamType paramType) {
        this.url = url;
        this.method = method;
        this.paramType = paramType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMap.Method getMethod() {
        return method;
    }

    public void setMethod(HttpMap.Method method) {
        this.method = method;
    }

    public HttpMap.ParamType getParamType() {
        return paramType;
    }

    public void setParamType(HttpMap.ParamType paramType) {
        this.paramType = paramType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RestMethodKey)) {
            return false;
        }

        RestMethodKey restMethodKey = (RestMethodKey) o;

        if (url != null ? !url.equals(restMethodKey.url) : restMethodKey.url != null) {
            return false;
        }
        return method == restMethodKey.method && paramType == restMethodKey.paramType;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (paramType != null ? paramType.hashCode() : 0);
        return result;
    }
}
