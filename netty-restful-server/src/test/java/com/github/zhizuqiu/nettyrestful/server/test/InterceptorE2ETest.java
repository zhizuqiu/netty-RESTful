package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.interceptor.AbstractInterceptor;
import com.github.zhizuqiu.nettyrestful.server.interceptor.InterceptorResponse;
import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 拦截器端到端测试：通过真实服务验证 HttpPreHandler 与 InterceptorHandler 的接线。
 * - 阻断型拦截器应返回自定义响应且 REST 处理器不被调用。
 * - 放行型拦截器应让请求到达 REST 处理器。
 */
public class InterceptorE2ETest {

    @Before
    public void setUp() throws Exception {
        MethodData.setConfig(new Config());
        Method m = InterceptorTestHandler.class.getMethod("echo", String.class);
        HttpMap map = m.getAnnotation(HttpMap.class);
        Object inst = InterceptorTestHandler.class.newInstance();
        MethodData.putRestMethod(
                new RestMethodKey("/interceptor/echo", HttpMap.Method.POST, HttpMap.ParamType.JSON),
                new RestMethodValue(map, m, inst));
        InterceptorTestHandler.restInvoked = false;
        MethodData.setInterceptorList(new ArrayList<>());
    }

    @After
    public void tearDown() {
        // 关键：清理拦截器，避免残留阻断其它走 TestServerSupport 的测试
        MethodData.setInterceptorList(new ArrayList<>());
    }

    @Test
    public void blockingInterceptor_returnsCustomResponse_andSkipsRest() throws Exception {
        MethodData.setInterceptorList(
                new ArrayList<>(Collections.singletonList(new BlockingInterceptor())));
        InterceptorTestHandler.restInvoked = false;

        TestServerSupport.Lifecycle server = TestServerSupport.start(false);
        try {
            HttpURLConnection con = postJson(server.port(), "/interceptor/echo",
                    "{\"type\":\"t\",\"group\":\"g\",\"message\":\"m\"}");
            int code = con.getResponseCode();
            String body = readBody(con, code);

            assertEquals(403, code);
            assertTrue("应返回拦截器设置的自定义响应体, 实际: " + body,
                    body.contains("blocked-by-interceptor"));
            assertFalse("REST 处理器不应被调用", InterceptorTestHandler.restInvoked);
        } finally {
            server.stop();
        }
    }

    @Test
    public void passingInterceptor_reachesRestHandler() throws Exception {
        MethodData.setInterceptorList(
                new ArrayList<>(Collections.singletonList(new PassingInterceptor())));
        InterceptorTestHandler.restInvoked = false;

        TestServerSupport.Lifecycle server = TestServerSupport.start(false);
        try {
            HttpURLConnection con = postJson(server.port(), "/interceptor/echo",
                    "{\"type\":\"t\",\"group\":\"g\",\"message\":\"m\"}");
            int code = con.getResponseCode();
            String body = readBody(con, code);

            assertEquals(200, code);
            assertTrue("应返回 REST 处理器回写的 type 字段, 实际: " + body,
                    body.contains("\"type\":\"t\""));
            assertTrue("REST 处理器应被调用", InterceptorTestHandler.restInvoked);
        } finally {
            server.stop();
        }
    }

    // ---------------- 辅助方法 ----------------

    private static HttpURLConnection postJson(int port, String path, String payload) throws Exception {
        URL url = new URL("http://127.0.0.1:" + port + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        return con;
    }

    private static String readBody(HttpURLConnection con, int code) throws Exception {
        InputStream is = (code >= 400) ? con.getErrorStream() : con.getInputStream();
        if (is == null) {
            return "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    /** 阻断型拦截器：写入 403 自定义响应并返回 false。 */
    static class BlockingInterceptor extends AbstractInterceptor {
        @Override
        public boolean preHandle(FullHttpRequest req, InterceptorResponse ir) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN,
                    Unpooled.copiedBuffer("blocked-by-interceptor", StandardCharsets.UTF_8));
            ir.setResponse(res);
            return false;
        }

        @Override
        public void postHandle(FullHttpRequest req) {
        }
    }

    /** 放行型拦截器：preHandle 返回 true，不写响应。 */
    static class PassingInterceptor extends AbstractInterceptor {
        @Override
        public boolean preHandle(FullHttpRequest req, InterceptorResponse ir) {
            return true;
        }

        @Override
        public void postHandle(FullHttpRequest req) {
        }
    }
}
