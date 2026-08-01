package com.github.zhizuqiu.nettyrestful.client.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.core.annotation.Param;

/**
 * 客户端端到端测试 API：
 * <ul>
 *   <li>{@link #echo(Item)} —— POST JSON body，服务端原样回写，验证编解码往返。</li>
 *   <li>{@link #getItem(String)} —— GET 路径参数 {id}，验证 URL 模板替换与解码。</li>
 *   <li>{@link #error()} —— GET 触发 500，验证错误传播。</li>
 * </ul>
 */
public interface StoreApi {

    @HttpMap(path = "/echo", method = HttpMap.Method.POST,
            paramType = HttpMap.ParamType.JSON, returnType = HttpMap.ReturnType.APPLICATION_JSON)
    Item echo(Item body) throws Exception;

    @HttpMap(path = "/items/{id}", method = HttpMap.Method.GET,
            returnType = HttpMap.ReturnType.APPLICATION_JSON)
    Item getItem(@Param("id") String id) throws Exception;

    @HttpMap(path = "/error", method = HttpMap.Method.GET)
    Item error() throws Exception;
}
