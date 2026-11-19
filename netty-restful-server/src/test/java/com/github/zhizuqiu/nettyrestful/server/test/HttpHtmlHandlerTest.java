package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.ResourceValue;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.TemplateMethodValue;
import com.github.zhizuqiu.nettyrestful.server.handler.HttpHtmlHandler;
import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import com.github.zhizuqiu.nettyrestful.template.mustache.MustacheTemplate;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.CharsetUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * HttpHtmlHandler.handle() 单元测试：覆盖静态资源查找与 Mustache 模板渲染。
 * handle() 返回 FullHttpResponse 或 null（未命中），无需真实 channel。
 */
public class HttpHtmlHandlerTest {

    private final HttpHtmlHandler handler = new HttpHtmlHandler();

    @Before
    public void setUp() {
        MethodData.setConfig(new Config());
        MethodData.setTemplateList(new java.util.ArrayList<>());
    }

    @After
    public void tearDown() {
        // 重置静态状态，避免影响其它测试
        MethodData.setConfig(new Config());
        MethodData.setTemplateList(new java.util.ArrayList<>());
    }

    @Test
    public void handle_staticFilePathNull_returnsNull() {
        // Config 默认 staticFilePath=null
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/page.html");

        assertNull(handler.handle(req));

        req.release();
    }

    @Test
    public void handle_nonHtmlUrl_returnsNull() {
        setStaticFilePath("target/html-test");
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/page.txt");

        assertNull(handler.handle(req));

        req.release();
    }

    @Test
    public void handle_htmlResourceNotRegistered_returnsNull() {
        setStaticFilePath("target/html-test");
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/missing.html");

        assertNull(handler.handle(req));

        req.release();
    }

    @Test
    public void handle_registeredHtmlResource_returnsContent() {
        setStaticFilePath("target/html-test");
        MethodData.putResource(
                new RestMethodKey("/page.html", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA),
                new ResourceValue("text/html", "<h1>hi</h1>"));
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/page.html");

        FullHttpResponse res = handler.handle(req);

        assertTrue("已注册的 html 资源应返回响应", res != null);
        assertEquals("text/html; charset=UTF-8", res.headers().get(HttpHeaderNames.CONTENT_TYPE));
        assertTrue("响应体应包含资源内容", res.content().toString(CharsetUtil.UTF_8).contains("<h1>hi</h1>"));

        res.release();
        req.release();
    }

    /**
     * 注册带 {{name}} 占位符的模板资源与返回作用域的方法，
     * handle() 应通过 MustacheTemplate 渲染出 "Hello world"。
     */
    @Test
    public void handle_withMustacheTemplate_rendersScope() throws Exception {
        setStaticFilePath("target/html-test");
        MethodData.putResource(
                new RestMethodKey("/tpl.html", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA),
                new ResourceValue("text/html", "Hello {{name}}"));

        TemplateMethodValue tmv = new TemplateMethodValue();
        Method scopeMethod = ScopeProvider.class.getMethod("scope");
        tmv.setMethod(scopeMethod);
        tmv.setInstance(new ScopeProvider());
        MethodData.putTemplateMethod("/tpl.html", tmv);
        MethodData.setTemplateList(Arrays.asList(new MustacheTemplate()));

        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/tpl.html");

        FullHttpResponse res = handler.handle(req);

        assertTrue("模板资源应返回响应", res != null);
        String body = res.content().toString(CharsetUtil.UTF_8);
        assertTrue("应渲染出作用域 name 值, 实际: " + body, body.contains("Hello world"));

        res.release();
        req.release();
    }

    private static void setStaticFilePath(String path) {
        Config cfg = new Config();
        cfg.setStaticFilePath(path);
        MethodData.setConfig(cfg);
    }

    /** 模板作用域提供者：返回 {name: "world"}。 */
    public static class ScopeProvider {
        public Map<String, String> scope() {
            Map<String, String> m = new HashMap<>();
            m.put("name", "world");
            return m;
        }
    }
}
