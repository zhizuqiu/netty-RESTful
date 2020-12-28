package com.github.zhizuqiu.nettyrestfulclient;

import com.github.zhizuqiu.nettyrestfulcommon.codec.Decoder;
import com.github.zhizuqiu.nettyrestfulcommon.codec.Encoder;
import com.github.zhizuqiu.nettyrestfulclient.netty.MethodHandler;
import com.github.zhizuqiu.nettyrestfulclient.netty.NettyRestInvacationHandler;
import com.github.zhizuqiu.nettyrestfulclient.netty.SynchronousMethodHandler;
import com.github.zhizuqiu.nettyrestfulclient.request.Request;
import com.github.zhizuqiu.nettyrestfulclient.response.ResponsePromise;
import com.github.zhizuqiu.nettyrestfulcommon.annotation.HttpMap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.zhizuqiu.nettyrestfulcommon.Util.checkNotNull;

public class NettyRestClient {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private EventLoopGroup group;
        private String host = "localhost";
        private int port = 8080;
        private long timeout = -1;
        private int maxFrameSize = 1024 * 100;
        private String preProxy = "";
        private Decoder decoder;
        private Encoder encoder;

        public Builder encoder(Encoder encoder) {
            this.encoder = encoder;
            return this;
        }

        public Builder decoder(Decoder decoder) {
            this.decoder = decoder;
            return this;
        }

        public Builder preProxy(String preProxy) {
            this.preProxy = preProxy;
            return this;
        }

        public Builder maxFrameSize(int maxFrameSize) {
            this.maxFrameSize = maxFrameSize;
            return this;
        }


        public Builder group(EventLoopGroup group) {
            this.group = group;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public <T> T target(Class<T> apiType) {

            String frameSize = System.getProperty("com.github.zhizuqiu.nettyrestfulclient.maxFrameSize");
            if (frameSize != null) {
                maxFrameSize = Integer.parseInt(frameSize);
            }
            if (group == null) {
                group = new NioEventLoopGroup(2);
            }
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast("codec", new HttpClientCodec());
                    p.addLast("chunkedWriter", new ChunkedWriteHandler());
                    p.addLast("aggregate", new HttpObjectAggregator(maxFrameSize));
                }
            });

            Map<Method, MethodHandler> methodToHandler = new LinkedHashMap<>();
            for (Method method : apiType.getMethods()) {
                if (method.getDeclaringClass() == Object.class) {
                    continue;
                } else {
                    HttpMap todoAnnotation = method.getAnnotation(HttpMap.class);
                    if (todoAnnotation != null) {
                        methodToHandler.put(method, new SynchronousMethodHandler(
                                bootstrap,
                                this.host,
                                this.port,
                                this.timeout,
                                new Request(method, this.preProxy, checkNotNull(this.encoder, "encoder")),
                                new ResponsePromise(method.getAnnotatedReturnType().getType(), checkNotNull(this.decoder, "decoder"))
                        ));
                    }
                }
            }

            return (T) Proxy.newProxyInstance(
                    apiType.getClassLoader(),
                    new Class<?>[]{apiType},
                    new NettyRestInvacationHandler(methodToHandler)
            );
        }

    }


}
