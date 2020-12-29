package com.github.zhizuqiu.example;

import com.github.zhizuqiu.example.interceptor.CustomInterceptorBuilder;
import com.github.zhizuqiu.nettyrestful.server.NettyRestServer;
import com.github.zhizuqiu.nettyrestful.template.mustache.MustacheTemplate;
import com.github.zhizuqiu.nettyrestful.template.thymeleaf.ThymeleafTemplate;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;


public class ServerTest {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NettyRestServer.class);

    private static final boolean SSL = false;
    private static final int PORT = 80;
    private static final int BOSSTHREADCOUNT = 2;
    private static final int WORKTHREADCOUNT = 4;

    public static void main(String[] args) {
        NettyRestServer nettyRestServer = new NettyRestServer.NettyRestServerBuilder()
                .setSsl(SSL)
                .setPort(PORT)
                .setBossThreadCount(BOSSTHREADCOUNT)
                .setWorkThreadCount(WORKTHREADCOUNT)
                .setPackages("com.github.zhizuqiu.example")
                .setStaticFilePath("netty-restful-server/resources")
                .setWebsocketHandler("/echo", new WebSocketFrameHandler())
                .setRestCallback((bossGroup, workerGroup) -> logger.info("callback"))
                .setInterceptorBuilder(new CustomInterceptorBuilder())
                .setRestfulPreProxy("/api", "/api2")
                .setTemplateList(new MustacheTemplate(), new ThymeleafTemplate())
                .build();
        try {
            nettyRestServer.run();
        } catch (Exception e) {
            nettyRestServer.stop();
            logger.error("NettyRestServer Exception:" + e.getMessage());
        }
    }
}
