package com.github.zhizuqiu.nettyrestful.server.initializer;

import com.github.zhizuqiu.nettyrestful.server.handler.*;
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

/**
 * RESTful Server 的初始化类
 */
public class RestfulServerInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private final String websocketPath;
    private final ChannelHandler websocketHandler;
    private final CustomStaticFileHandler customStaticFileHandler;
    private final Boolean enableUpload;

    public RestfulServerInitializer(SslContext sslCtx, String websocketPath, ChannelHandler websocketHandler, CustomStaticFileHandler customStaticFileHandler, Boolean enableUpload) {
        this.sslCtx = sslCtx;
        this.websocketPath = websocketPath;
        this.websocketHandler = websocketHandler;
        this.customStaticFileHandler = customStaticFileHandler;
        this.enableUpload = enableUpload;
    }

    /**
     * 向 pipeline 中添加自定义 handler
     * 异步 IO 线程将顺序处理这些 handler，以实现相应功能
     */
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 处理 SSL
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        // 将网络报文转为 HTTP 文本报文
        pipeline.addLast(new HttpServerCodec());
        // 如果开启了文件上传
        if (enableUpload) {
            // 使用实时写入临时文件的方式保存数据，以减低内存占用
            pipeline.addLast(new HttpUploadServerHandler());
        }
        // 把多个HTTP请求中的数据组装成一个
        pipeline.addLast(new HttpObjectAggregator(65536));
        // 用于处理大数据流
        pipeline.addLast(new ChunkedWriteHandler());
        // 如果开启了 websocket 功能
        if (websocketPath != null) {
            // WebSocket 数据压缩
            pipeline.addLast(new WebSocketServerCompressionHandler());
            // 将相应路径的 http 请求升级为 websocket 协议
            pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true));
        }
        // 匹配拦截器
        pipeline.addLast(new HttpPreHandler());
        // 匹配 RESTful
        pipeline.addLast(new HttpRestfulHandler());
        // 匹配 Html 缓存器
        pipeline.addLast(new HttpHtmlHandler());
        // 匹配静态文件
        pipeline.addLast(new HttpStaticFileHandler(this.customStaticFileHandler));
        // 未匹配成功，返回 404 Not Found
        pipeline.addLast(new HttpNotFoundHandler());
        if (websocketPath != null) {
            // 处理 websocket 请求
            pipeline.addLast(websocketHandler);
        }
    }
}
