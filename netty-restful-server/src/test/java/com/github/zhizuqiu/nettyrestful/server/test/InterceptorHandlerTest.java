package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.server.interceptor.AbstractInterceptor;
import com.github.zhizuqiu.nettyrestful.server.interceptor.InterceptorHandler;
import com.github.zhizuqiu.nettyrestful.server.interceptor.InterceptorResponse;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * InterceptorHandler 单元测试：覆盖空列表、全部放行、短路阻断、postHandle 全调用、
 * 以及拦截器通过 InterceptorResponse 设置自定义响应。
 */
public class InterceptorHandlerTest {

    @Before
    public void setUp() {
        MethodData.setInterceptorList(new ArrayList<>());
    }

    @After
    public void tearDown() {
        // 必须重置，避免拦截器残留影响其它走 TestServerSupport 的测试
        MethodData.setInterceptorList(new ArrayList<>());
    }

    @Test
    public void preHandle_nullList_returnsTrue() {
        MethodData.setInterceptorList(null);
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");

        assertTrue(InterceptorHandler.preHandle(req, new InterceptorResponse()));

        req.release();
    }

    @Test
    public void preHandle_emptyList_returnsTrue() {
        MethodData.setInterceptorList(new ArrayList<>());
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");

        assertTrue(InterceptorHandler.preHandle(req, new InterceptorResponse()));

        req.release();
    }

    @Test
    public void preHandle_allPass_returnsTrueAndInvokesAll() {
        RecordingInterceptor a = new RecordingInterceptor();
        RecordingInterceptor b = new RecordingInterceptor();
        MethodData.setInterceptorList(new ArrayList<>(Arrays.asList(a, b)));
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");

        boolean result = InterceptorHandler.preHandle(req, new InterceptorResponse());

        assertTrue(result);
        assertTrue("第一个拦截器 preHandle 应被调用", a.preCalled);
        assertTrue("第二个拦截器 preHandle 应被调用", b.preCalled);

        req.release();
    }

    /**
     * 任一拦截器返回 false 即短路，后续拦截器 preHandle 不应被调用。
     */
    @Test
    public void preHandle_oneReturnsFalse_shortCircuitsRemaining() {
        RecordingInterceptor first = new RecordingInterceptor();
        first.preResult = false;
        RecordingInterceptor second = new RecordingInterceptor();
        MethodData.setInterceptorList(new ArrayList<>(Arrays.asList(first, second)));
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");

        boolean result = InterceptorHandler.preHandle(req, new InterceptorResponse());

        assertFalse(result);
        assertTrue("首个拦截器应被调用", first.preCalled);
        assertFalse("短路后后续拦截器不应被调用", second.preCalled);

        req.release();
    }

    @Test
    public void postHandle_invokesAllPostHandles() {
        RecordingInterceptor a = new RecordingInterceptor();
        RecordingInterceptor b = new RecordingInterceptor();
        MethodData.setInterceptorList(new ArrayList<>(Arrays.asList(a, b)));
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");

        InterceptorHandler.postHandle(req);

        assertTrue(a.postCalled);
        assertTrue(b.postCalled);

        req.release();
    }

    @Test
    public void preHandle_interceptorCanSetCustomResponse() {
        RecordingInterceptor interceptor = new RecordingInterceptor();
        FullHttpResponse custom = new DefaultFullHttpResponse(HTTP_1_1,
                io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN);
        interceptor.responseToSet = custom;
        MethodData.setInterceptorList(new ArrayList<>(Collections.singletonList(interceptor)));
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");

        InterceptorResponse ir = new InterceptorResponse();
        // 此拦截器放行（true），仅验证它能向 InterceptorResponse 写入响应
        interceptor.preResult = true;
        InterceptorHandler.preHandle(req, ir);

        assertNotNull(ir.getResponse());
        assertSame(custom, ir.getResponse());

        req.release();
        custom.release();
    }

    /** 记录调用状态的拦截器桩。 */
    static class RecordingInterceptor extends AbstractInterceptor {
        boolean preCalled = false;
        boolean postCalled = false;
        boolean preResult = true;
        FullHttpResponse responseToSet;

        @Override
        public boolean preHandle(FullHttpRequest req, InterceptorResponse interceptorResponse) {
            preCalled = true;
            if (responseToSet != null) {
                interceptorResponse.setResponse(responseToSet);
            }
            return preResult;
        }

        @Override
        public void postHandle(FullHttpRequest req) {
            postCalled = true;
        }
    }
}
