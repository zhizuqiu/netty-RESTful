package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpHandler;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.Map;

/**
 * 覆盖 HttpRestfulHandler.invoke() 注释中列出的全部 19 种参数组合
 */
@HttpHandler
public class InvokeCombinationHandler {

    // ===== 0 参数 =====
    @HttpMap(path = "/invoke/m0", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m0() { return "ok-m0"; }

    // ===== 1 参数 =====
    @HttpMap(path = "/invoke/m1_p", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m1_p(Map<String, String> param) { return "ok-m1_p:" + param.get("key"); }

    @HttpMap(path = "/invoke/m1_j", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m1_j(String jsonParam) { return "ok-m1_j"; }

    @HttpMap(path = "/invoke/m1_r", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m1_r(FullHttpResponse response) { return "ok-m1_r"; }

    @HttpMap(path = "/invoke/m1_q", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m1_q(FullHttpRequest req) { return "ok-m1_q"; }

    // ===== 2 参数 =====
    @HttpMap(path = "/invoke/m2_pj", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m2_pj(Map<String, String> param, String jsonParam) { return "ok-m2_pj"; }

    @HttpMap(path = "/invoke/m2_jp", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m2_jp(String jsonParam, Map<String, String> param) { return "ok-m2_jp"; }

    @HttpMap(path = "/invoke/m2_pr", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m2_pr(Map<String, String> param, FullHttpResponse response) { return "ok-m2_pr"; }

    @HttpMap(path = "/invoke/m2_jr", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m2_jr(String jsonParam, FullHttpResponse response) { return "ok-m2_jr"; }

    @HttpMap(path = "/invoke/m2_pq", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m2_pq(Map<String, String> param, FullHttpRequest req) { return "ok-m2_pq"; }

    @HttpMap(path = "/invoke/m2_jq", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m2_jq(String jsonParam, FullHttpRequest req) { return "ok-m2_jq"; }

    // ===== 3 参数 =====
    @HttpMap(path = "/invoke/m3_pjr", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m3_pjr(Map<String, String> param, String jsonParam, FullHttpResponse response) { return "ok-m3_pjr"; }

    @HttpMap(path = "/invoke/m3_jpr", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m3_jpr(String jsonParam, Map<String, String> param, FullHttpResponse response) { return "ok-m3_jpr"; }

    @HttpMap(path = "/invoke/m3_pjq", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m3_pjq(Map<String, String> param, String jsonParam, FullHttpRequest req) { return "ok-m3_pjq"; }

    @HttpMap(path = "/invoke/m3_jpq", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m3_jpq(String jsonParam, Map<String, String> param, FullHttpRequest req) { return "ok-m3_jpq"; }

    @HttpMap(path = "/invoke/m3_pqr", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m3_pqr(Map<String, String> param, FullHttpRequest req, FullHttpResponse response) { return "ok-m3_pqr"; }

    @HttpMap(path = "/invoke/m3_jqr", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m3_jqr(String jsonParam, FullHttpRequest req, FullHttpResponse response) { return "ok-m3_jqr"; }

    // ===== 4 参数 =====
    @HttpMap(path = "/invoke/m4_pjqr", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m4_pjqr(Map<String, String> param, String jsonParam, FullHttpRequest req, FullHttpResponse response) { return "ok-m4_pjqr"; }

    @HttpMap(path = "/invoke/m4_jpqr", method = HttpMap.Method.POST, paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String m4_jpqr(String jsonParam, Map<String, String> param, FullHttpRequest req, FullHttpResponse response) { return "ok-m4_jpqr"; }
}
