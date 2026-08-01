package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.server.NettyRestServer;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * NettyRestServer 启动行为测试。
 *
 * 重点验证：当端口已被占用时，run() 必须将绑定异常向上传播（抛出 Exception），
 * 而不是吞掉异常导致调用方误以为服务已正常启动。
 *
 * 为防止 bind 意外成功后 run() 阻塞在 closeFuture().sync() 导致测试挂死，
 * run() 在守护线程中执行并设置超时。
 */
public class NettyRestServerTest {

    /**
     * 绑定已被占用的端口时，run() 应抛出异常。
     * 这是对此前 "NettyRestServer 必须传播绑定异常" 修复的回归保护。
     */
    @Test
    public void run_bindToInUsePort_throwsException() throws Exception {
        // 先占用一个随机端口
        TestServerSupport.Lifecycle holder = TestServerSupport.start(false);
        int occupiedPort = holder.port();
        try {
            // 使用默认 host 0.0.0.0（与 TestServerSupport 占用端口相同的通配地址），
            // 这样两次 listen 同一 (0.0.0.0, port) 才会真正冲突。
            // 注：在 BSD/macOS 上，SO_REUSEADDR 允许在通配地址占用后再绑定更具体的地址
            // (如 127.0.0.1)，故必须用相同的通配地址才能复现 EADDRINUSE。
            final NettyRestServer server = new NettyRestServer.NettyRestServerBuilder()
                    .setPort(occupiedPort)
                    .build();

            final CountDownLatch done = new CountDownLatch(1);
            final AtomicReference<Throwable> thrown = new AtomicReference<>(null);

            Thread t = new Thread(() -> {
                try {
                    server.run();
                } catch (Throwable e) {
                    thrown.set(e);
                } finally {
                    done.countDown();
                }
            });
            t.setDaemon(true);
            t.start();

            // bind 失败应快速返回；若 10s 内未结束则视为 bind 意外成功而阻塞
            if (!done.await(10, TimeUnit.SECONDS)) {
                t.interrupt();
                fail("run() 在已占用端口上应在超时前抛出异常，但实际阻塞超过 10s（bind 可能未冲突）");
            }

            assertNotNull(
                    "绑定已占用端口 " + occupiedPort + " 应抛出异常，但 run() 正常返回",
                    thrown.get());
        } finally {
            holder.stop();
        }
    }
}
