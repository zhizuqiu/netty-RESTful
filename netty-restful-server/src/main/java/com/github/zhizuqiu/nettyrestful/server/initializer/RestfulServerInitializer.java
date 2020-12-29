package com.github.zhizuqiu.nettyrestful.server.initializer;

import com.github.zhizuqiu.nettyrestful.server.handler.HttpHandler;
import com.github.zhizuqiu.nettyrestful.server.handler.StaticFileHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.List;

public class RestfulServerInitializer extends ChannelInitializer<SocketChannel> {


    private final SslContext sslCtx;
    private final String websocketPath;
    private final ChannelHandler websocketHandler;
    private final List<String> restfulPreProxy;
    private final StaticFileHandler staticFileHandler;

    public RestfulServerInitializer(SslContext sslCtx, String websocketPath, ChannelHandler websocketHandler, List<String> restfulPreProxy, StaticFileHandler staticFileHandler) {
        this.sslCtx = sslCtx;
        this.websocketPath = websocketPath;
        this.websocketHandler = websocketHandler;
        this.restfulPreProxy = restfulPreProxy;
        this.staticFileHandler = staticFileHandler;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // ssl
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        // 对通信数据进行编解码
        pipeline.addLast(new HttpServerCodec());
        // 把多个HTTP请求中的数据组装成一个
        pipeline.addLast(new HttpObjectAggregator(65536));
        // 用于处理大数据流
        pipeline.addLast(new ChunkedWriteHandler());
        if (websocketPath != null) {
            // WebSocket数据压缩
            pipeline.addLast(new WebSocketServerCompressionHandler());
            // Netty支持websocket
            pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true));
        }
        // http的handler
        pipeline.addLast(new HttpHandler(this.restfulPreProxy, this.staticFileHandler));
        if (websocketPath != null) {
            // websocket的handler
            pipeline.addLast(websocketHandler);
        }
    }
}