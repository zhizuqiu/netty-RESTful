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
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 客户端端到端测试：用真实 Netty HTTP 服务验证 NettyRestClient 代理的
 * 编码（GsonEncoder）、URL 模板替换、解码（GsonDecoder）与错误传播。
 */
public class ClientEndToEndTest {

    @Test
    public void postEcho_roundTripsPojo() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        RoutingServer server = startRoutingServer();
        try {
            Gson gson = new Gson();
            StoreApi api = NettyRestClient.builder()
                    .host("127.0.0.1").port(server.port)
                    .group(group)
                    .encoder(new GsonEncoder(gson))
                    .decoder(new GsonDecoder(gson))
                    .target(StoreApi.class);

            Item sent = new Item("widget", 7);
            Item received = api.echo(sent);

            assertEquals("widget", received.getName());
            assertEquals(7, received.getValue());
        } finally {
            server.close();
            group.shutdownGracefully();
        }
    }

    @Test
    public void getItemWithPathParam_decodesPojo() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        RoutingServer server = startRoutingServer();
        try {
            Gson gson = new Gson();
            StoreApi api = NettyRestClient.builder()
                    .host("127.0.0.1").port(server.port)
                    .group(group)
                    .encoder(new GsonEncoder(gson))
                    .decoder(new GsonDecoder(gson))
                    .target(StoreApi.class);

            Item received = api.getItem("abc");

            // 路径参数 {id} 应被替换为 abc，服务端据此回填 name
            assertEquals("abc", received.getName());
            assertEquals(42, received.getValue());
        } finally {
            server.close();
            group.shutdownGracefully();
        }
    }

    /**
     * 非 2xx 响应应使同步调用抛出异常，而非返回 null 或挂起。
     */
    @Test
    public void non200Response_throwsToCaller() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        RoutingServer server = startRoutingServer();
        try {
            Gson gson = new Gson();
            StoreApi api = NettyRestClient.builder()
                    .host("127.0.0.1").port(server.port)
                    .group(group)
                    .encoder(new GsonEncoder(gson))
                    .decoder(new GsonDecoder(gson))
                    .target(StoreApi.class);

            try {
                api.error();
                fail("服务端 500 应导致客户端抛出异常");
            } catch (Exception expected) {
                // 期望异常（ResponseHandler 将非 200 转为 IOException）
            }
        } finally {
            server.close();
            group.shutdownGracefully();
        }
    }

    /**
     * 同一客户端实例连续发起多次请求均应成功，验证基本可重入性。
     */
    @Test
    public void multipleSequentialCalls_succeed() throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        RoutingServer server = startRoutingServer();
        try {
            Gson gson = new Gson();
            StoreApi api = NettyRestClient.builder()
                    .host("127.0.0.1").port(server.port)
                    .group(group)
                    .encoder(new GsonEncoder(gson))
                    .decoder(new GsonDecoder(gson))
                    .target(StoreApi.class);

            for (int i = 0; i < 3; i++) {
                Item received = api.getItem("n" + i);
                assertEquals("n" + i, received.getName());
            }
        } finally {
            server.close();
            group.shutdownGracefully();
        }
    }

    // ---------------- 内嵌路由服务 ----------------

    private static RoutingServer startRoutingServer() throws Exception {
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
                                        String uri = req.uri();
                                        FullHttpResponse res;
                                        if (uri.startsWith("/echo")) {
                                            // 原样回写请求体（已是 Item 的 JSON）
                                            res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                    HttpResponseStatus.OK,
                                                    req.content().retainedDuplicate());
                                            res.headers().set(HttpHeaderNames.CONTENT_TYPE,
                                                    "application/json; charset=UTF-8");
                                        } else if (uri.startsWith("/items/")) {
                                            String id = uri.substring("/items/".length());
                                            String json = "{\"name\":\"" + id + "\",\"value\":42}";
                                            res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                    HttpResponseStatus.OK,
                                                    Unpooled.copiedBuffer(json, StandardCharsets.UTF_8));
                                            res.headers().set(HttpHeaderNames.CONTENT_TYPE,
                                                    "application/json; charset=UTF-8");
                                        } else if (uri.startsWith("/error")) {
                                            res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                    Unpooled.copiedBuffer("boom", StandardCharsets.UTF_8));
                                        } else {
                                            res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                                                    HttpResponseStatus.NOT_FOUND,
                                                    Unpooled.copiedBuffer("not found", StandardCharsets.UTF_8));
                                        }
                                        writeResponse(ctx, req, res);
                                    }
                                });
                    }
                });
        Channel ch = b.bind(0).sync().channel();
        int port = ((InetSocketAddress) ch.localAddress()).getPort();
        return new RoutingServer(port, ch, boss, worker);
    }

    private static void writeResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
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

    private static final class RoutingServer {
        final int port;
        final Channel ch;
        final EventLoopGroup boss;
        final EventLoopGroup worker;

        RoutingServer(int port, Channel ch, EventLoopGroup boss, EventLoopGroup worker) {
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
