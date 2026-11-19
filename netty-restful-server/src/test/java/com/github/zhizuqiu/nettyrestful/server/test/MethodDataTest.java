package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * MethodData 测试：基础 put/get 往返与并发安全回归（HashMap→ConcurrentHashMap 修复保护）。
 */
public class MethodDataTest {

    @Test
    public void putRestMethod_thenGetRestMethod_returnsSame() {
        RestMethodKey key = uniqueKey("roundtrip");
        RestMethodValue value = newValue();

        MethodData.putRestMethod(key, value);

        assertSame(value, MethodData.getRestMethod(key));
    }

    @Test
    public void getRestMethod_unregisteredKey_returnsNull() {
        RestMethodKey key = uniqueKey("missing");
        assertNull(MethodData.getRestMethod(key));
    }

    /**
     * 并发 put/get 回归保护：历史 bug 为 HashMap 在多线程 put 下可能结构损坏
     * （死循环/丢数据）。ConcurrentHashMap 应能安全支持并发写入且全部可读回。
     */
    @Test
    public void concurrentPutAndGet_allRetrievableNoCrash() throws Exception {
        int threads = 8;
        int perThread = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicReference<Throwable> failure = new AtomicReference<>(null);

        List<RestMethodKey> allKeys = Collections.synchronizedList(new ArrayList<>());

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    for (int i = 0; i < perThread; i++) {
                        RestMethodKey key = uniqueKey("conc-" + UUID.randomUUID());
                        RestMethodValue value = newValue();
                        MethodData.putRestMethod(key, value);
                        allKeys.add(key);
                        // 立即读回，验证并发读不会拿到 null/错误值
                        assertSame(value, MethodData.getRestMethod(key));
                    }
                } catch (Throwable e) {
                    failure.compareAndSet(null, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue("并发 put/get 应在超时前完成",
                latch.await(30, TimeUnit.SECONDS));
        pool.shutdown();

        if (failure.get() != null) {
            throw new AssertionError("并发操作出现异常: " + failure.get(), failure.get());
        }

        assertEquals(threads * perThread, allKeys.size());
        // 全部 key 都应可读回非 null
        for (RestMethodKey key : allKeys) {
            assertNotNull(MethodData.getRestMethod(key));
        }
    }

    private static RestMethodKey uniqueKey(String tag) {
        return new RestMethodKey("/md/" + tag + "/" + UUID.randomUUID(),
                HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
    }

    private static RestMethodValue newValue() {
        return new RestMethodValue();
    }

    private static void assertSame(Object expected, Object actual) {
        assertTrue("期望同一实例", expected == actual);
    }
}
