package com.github.zhizuqiu.nettyrestful.server.interceptor;

import io.netty.handler.codec.http.FullHttpResponse;

public class InterceptorResponse {
    private FullHttpResponse response = null;

    public FullHttpResponse getResponse() {
        return response;
    }

    public void setResponse(FullHttpResponse response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "InterceptorResponse{" +
                "response=" + response +
                '}';
    }
}
