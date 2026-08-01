package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.example.TestMessage;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpHandler;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.google.gson.Gson;

/**
 * 测试用 REST 处理器：接收 JSON 串并解析后回写。
 */
@HttpHandler
public class UploadTestHandler {

    @HttpMap(path = "/upload/json",
            method = HttpMap.Method.POST,
            paramType = HttpMap.ParamType.JSON,
            returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public TestMessage echoJson(String jsonParam) {
        return new Gson().fromJson(jsonParam, TestMessage.class);
    }
}
