package com.github.zhizuqiu.nettyrestful.client.test;

import com.github.zhizuqiu.nettyrestful.client.NettyRestClient;
import com.github.zhizuqiu.nettyrestful.codec.gson.GsonDecoder;
import com.github.zhizuqiu.nettyrestful.codec.gson.GsonEncoder;
import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class ClientReusabilityTest {

    /**
     * Problem 2: 客户端单次请求失败不应搞垮共享的 EventLoopGroup，
     * 同一 group 应能继续承载后续成功请求。（回归保护）
     */
    @Test
    public void sharedGroupSurvivesRequestFailure() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        int badPort = findFreePortThenClose();
        EchoServer echo = startEchoServer();
        try {
            Gson gson = new Gson();

            EchoApi failApi = NettyRestClient.builder()
                    .host("127.0.0.1").port(badPort)
                    .group(group)
                    .encoder(new GsonEncoder(gson))
                    .decoder(new GsonDecoder(gson))
                    .target(EchoApi.class);

            try {
                failApi.getData("x");
                fail("expected connection failure");
            } catch (Exception expected) {
                // 期望连接失败
            }
            assertFalse("共享 EventLoopGroup 不应进入关闭流程", group.isShuttingDown());

            EchoApi okApi = NettyRestClient.builder()
                    .host("127.0.0.1").port(echo.port)
                    .group(group)
                    .encoder(new GsonEncoder(gson))
                    .decoder(new GsonDecoder(gson))
                    .target(EchoApi.class);
            assertEquals("ok", okApi.getData("x"));
            assertFalse(group.isShuttingDown());
        } finally {
            echo.close();
            group.shutdownGracefully();
        }
    }

    private static int findFreePortThenClose() throws Exception {
        ServerSocket s = new ServerSocket(0);
        int p = s.getLocalPort();
        s.close();
        return p;
    }

    private static EchoServer startEchoServer() throws Exception {
        final NioEventLoopGroup boss = new NioEventLoopGroup(1);
        final NioEventLoopGroup worker = new NioEventLoopGroup(1);
        ServerBootstrap b = new ServerBootstrap();
        b.group(boss, worker).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new HttpServerCodec(), new HttpObjectAggregator(65536),
                                new SimpleChannelInboundHandler<FullHttpRequest>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
                                        FullHttpResponse res = new DefaultFullHttpResponse(
                                                HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                                                Unpooled.copiedBuffer("\"ok\"", StandardCharsets.UTF_8));
                                        res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
                                        HttpUtil.setContentLength(res, res.content().readableBytes());
                                        boolean keepAlive = HttpUtil.isKeepAlive(req);
                                        if (keepAlive) {
                                            res.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                                        }
                                        ChannelFuture f = ctx.writeAndFlush(res);
                                        if (!keepAlive) {
                                            f.addListener(ChannelFutureListener.CLOSE);
                                        }
                                    }
                                });
                    }
                });
        Channel ch = b.bind(0).sync().channel();
        int port = ((InetSocketAddress) ch.localAddress()).getPort();
        return new EchoServer(port, ch, boss, worker);
    }

    private static final class EchoServer {
        final int port;
        final Channel ch;
        final EventLoopGroup boss;
        final EventLoopGroup worker;

        EchoServer(int port, Channel ch, EventLoopGroup boss, EventLoopGroup worker) {
            this.port = port;
            this.ch = ch;
            this.boss = boss;
            this.worker = worker;
        }

        void close() {
            ch.close().syncUninterruptibly();
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
