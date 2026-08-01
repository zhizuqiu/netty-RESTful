package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpHandler;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.Map;

/**
 * 用于测试 invoke 参数顺序的测试处理器
 */
@HttpHandler
public class InvokeParamTestHandler {

    /**
     * 4参数版本：method(String jsonParam, Map param, FullHttpRequest req, FullHttpResponse response)
     * 用于验证 HttpRestfulHandler.invoke() 参数顺序
     */
    @HttpMap(path = "/rest/4params",
            method = HttpMap.Method.POST,
            paramType = HttpMap.ParamType.JSON,
            returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String handle4Params(String jsonParam, Map<String, String> param,
                                FullHttpRequest req, FullHttpResponse response) {
        // 如果参数顺序正确，此方法正常执行
        // 如果顺序反了（bug），Method.invoke 会抛 IllegalArgumentException
        return "ok-4params";
    }

    /**
     * 2参数版本：method(Map param, FullHttpResponse response)
     * 用于验证 HttpUploadServerHandler.invoke() 传参
     */
    @HttpMap(path = "/upload/param-response",
            method = HttpMap.Method.POST,
            paramType = HttpMap.ParamType.MULTIPART_FORM_DATA,
            returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String handleParamResponse(Map<String, String> param, FullHttpResponse response) {
        // 如果参数正确，此方法正常执行
        // 如果误传 FileUpload[] 作为 response（bug），Method.invoke 会抛 IllegalArgumentException
        return "ok-param-response";
    }
}
