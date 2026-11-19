package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.example.TestMessage;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpHandler;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.google.gson.Gson;

/**
 * 拦截器 e2e 测试用 REST 处理器：通过 {@link #restInvoked} 标记是否被调用，
 * 以验证拦截器放行/阻断时 REST 处理器是否真正执行。
 */
@HttpHandler
public class InterceptorTestHandler {

    public static volatile boolean restInvoked = false;

    @HttpMap(path = "/interceptor/echo",
            method = HttpMap.Method.POST,
            paramType = HttpMap.ParamType.JSON,
            returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public TestMessage echo(String jsonParam) {
        restInvoked = true;
        return new Gson().fromJson(jsonParam, TestMessage.class);
    }
}
