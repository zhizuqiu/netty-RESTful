package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.server.handler.CustomStaticFileHandler;
import com.github.zhizuqiu.nettyrestful.server.initializer.RestfulServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * 测试用：在随机端口启动一个使用 RestfulServerInitializer 的真实 Netty HTTP 服务。
 */
public final class TestServerSupport {

    public interface Lifecycle {
        int port();

        void stop();
    }

    private TestServerSupport() {
    }

    public static Lifecycle start(boolean enableUpload) throws Exception {
        return start(enableUpload, null);
    }

    public static Lifecycle start(boolean enableUpload, CustomStaticFileHandler custom) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(1);
        ServerBootstrap b = new ServerBootstrap();
        b.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new RestfulServerInitializer(null, null, null, custom, enableUpload));
        Channel ch = b.bind(0).sync().channel();
        int port = ((InetSocketAddress) ch.localAddress()).getPort();
        return new Lifecycle() {
            @Override
            public int port() {
                return port;
            }

            @Override
            public void stop() {
                ch.close().syncUninterruptibly();
                boss.shutdownGracefully();
                worker.shutdownGracefully();
            }
        };
    }
}
