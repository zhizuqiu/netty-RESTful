package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpHandler;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;

/**
 * 异常路径测试处理器：方法直接抛出异常，用于验证 HttpRestfulHandler 将其转为 500。
 */
@HttpHandler
public class ExceptionTestHandler {

    @HttpMap(path = "/error/boom",
            method = HttpMap.Method.POST,
            paramType = HttpMap.ParamType.JSON,
            returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String boom(String jsonParam) {
        throw new RuntimeException("kaboom");
    }
}
