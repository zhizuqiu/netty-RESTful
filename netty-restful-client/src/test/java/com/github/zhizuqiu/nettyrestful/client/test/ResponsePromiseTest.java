package com.github.zhizuqiu.nettyrestful.client.test;

import com.github.zhizuqiu.nettyrestful.client.response.ResponsePromise;
import com.github.zhizuqiu.nettyrestful.codec.gson.GsonDecoder;
import com.google.gson.Gson;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * ResponsePromise 单元测试：覆盖成功解码、cancel 异常类型保留（先设异常再取消）、
 * 超时异常映射等关键路径。
 */
public class ResponsePromiseTest {

    private static Promise<String> nettyPromise() {
        return new DefaultPromise<>(ImmediateEventExecutor.INSTANCE);
    }

    @Test
    public void get_promiseSuccess_decodesValue() throws Exception {
        ResponsePromise rp = new ResponsePromise(Item.class, new GsonDecoder(new Gson()));
        Promise<String> promise = nettyPromise();
        rp.setPromise(promise);
        promise.setSuccess("{\"name\":\"widget\",\"value\":9}");

        Object result = rp.get();

        assertTrue(result instanceof Item);
        Item item = (Item) result;
        assertEquals("widget", item.getName());
        assertEquals(9, item.getValue());
    }

    /**
     * cancel 须先记录异常再取消 promise（历史 bug：先取消导致异常丢失），
     * 之后 get() 应抛出原始 IOException 而非通用异常。
     */
    @Test
    public void cancel_withIOException_getThrowsIOException() {
        ResponsePromise rp = new ResponsePromise(Item.class, new GsonDecoder(new Gson()));
        rp.setPromise(nettyPromise());
        rp.cancel(new IOException("boom"));

        try {
            rp.get();
            fail("cancel 后 get() 应抛出 IOException");
        } catch (IOException e) {
            assertEquals("boom", e.getMessage());
        } catch (TimeoutException e) {
            fail("应抛出 IOException 而非 TimeoutException");
        }
    }

    /**
     * Netty 读超时（ReadTimeoutException）应映射为 java.util.concurrent.TimeoutException。
     */
    @Test(expected = TimeoutException.class)
    public void cancel_withReadTimeout_getThrowsTimeoutException() throws Exception {
        ResponsePromise rp = new ResponsePromise(Item.class, new GsonDecoder(new Gson()));
        rp.setPromise(nettyPromise());
        rp.cancel(ReadTimeoutException.INSTANCE);

        rp.get();
    }

    /**
     * 回归保护：cancel 后异常字段应立即等于传入的 cause（先设异常再取消）。
     */
    @Test
    public void cancel_setsExceptionFieldBeforeCancel() {
        ResponsePromise rp = new ResponsePromise(Item.class, new GsonDecoder(new Gson()));
        Promise<String> promise = nettyPromise();
        rp.setPromise(promise);
        IOException cause = new IOException("preserved");

        rp.cancel(cause);

        assertSame(cause, rp.getException());
        assertTrue("promise 应已被取消", promise.isCancelled());
    }
}
