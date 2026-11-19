package com.github.zhizuqiu.nettyrestful.client.test;

import com.github.zhizuqiu.nettyrestful.client.netty.ResponseHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResponseHandlerTest {

    /**
     * Problem 5: 发生异常时，promise 应被失败，而不是永久挂起。
     */
    @Test
    public void promiseFailedOnExceptionCaught() throws Exception {
        Promise<String> promise = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
        EmbeddedChannel channel = new EmbeddedChannel(new ResponseHandler(promise));

        channel.pipeline().fireExceptionCaught(new java.io.IOException("boom"));

        assertTrue("promise 应在超时内完成", promise.await(2, TimeUnit.SECONDS));
        assertTrue(promise.isDone());
        assertFalse(promise.isSuccess());
        channel.finishAndReleaseAll();
    }

    /**
     * Problem 5: channel 在没有收到响应时关闭，promise 应被失败。
     */
    @Test
    public void promiseFailedOnChannelInactive() throws Exception {
        Promise<String> promise = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
        EmbeddedChannel channel = new EmbeddedChannel(new ResponseHandler(promise));

        channel.close().sync();

        assertTrue("promise 应在超时内完成", promise.await(2, TimeUnit.SECONDS));
        assertTrue(promise.isDone());
        assertFalse(promise.isSuccess());
    }
}
