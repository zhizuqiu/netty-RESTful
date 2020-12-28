package com.github.zhizuqiu.nettyrestfulclient.netty;

import com.github.zhizuqiu.nettyrestfulclient.request.Request;
import com.github.zhizuqiu.nettyrestfulclient.response.ResponsePromise;
import com.github.zhizuqiu.nettyrestfulcore.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestfulcore.annotation.Param;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

public class SynchronousMethodHandler implements MethodHandler {

    private final Bootstrap bootstrap;
    private final String host;
    private final int port;
    private final long timeout;

    private final Request visionRequest;
    private final ResponsePromise responsePromise;

    public SynchronousMethodHandler(Bootstrap bootstrap, String host, int port, long timeout, Request visionRequest, ResponsePromise responsePromise) {
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.visionRequest = visionRequest;
        this.responsePromise = responsePromise;
    }

    public ResponsePromise send(Object[] argv) {
        final ChannelFuture channelFuture = bootstrap.connect(host, port);
        responsePromise.setPromise(channelFuture.channel().eventLoop().newPromise());

        channelFuture.addListener((GenericFutureListener<ChannelFuture>) future -> {
            if (!channelFuture.isSuccess()) {
                final Throwable cause = future.cause();
                if (cause instanceof ClosedChannelException || cause instanceof IllegalStateException) {
                    responsePromise.cancel(new CancellationException("Channel closed"));
                } else {
                    responsePromise.cancel(new ConnectException("Connection failed"));
                    // todo
                    // responsePromise.handleRetry(future.cause());
                }
                return;
            }

            // Handle already cancelled promises
            if (responsePromise.getPromise().isCancelled()) {
                future.channel().close();
                responsePromise.getPromise().setFailure(new CancellationException());
                return;
            }

            final Promise<String> listenedToPromise = responsePromise.getPromise();

            // Close channel when promise is satisfied or cancelled later
            listenedToPromise.addListener(f -> {
                // Only close if it was not redirected to new promise
                if (responsePromise.getPromise() == listenedToPromise) {
                    future.channel().close();
                }
            });

            createAndSendHttpRequest(responsePromise, channelFuture, future.channel().pipeline(), visionRequest, argv).addListener((ChannelFutureListener) f -> {
                if (!f.isSuccess()) {
                    responsePromise.setException(future.cause());
                    if (!future.channel().eventLoop().inEventLoop()) {
                        future.channel().eventLoop().shutdownGracefully();
                    }

                    future.channel().close();
                }
            });

        });

        return responsePromise;
    }


    private ChannelFuture createAndSendHttpRequest(
            ResponsePromise visionResponsePromise,
            final ChannelFuture channelFuture,
            final ChannelPipeline pipeline,
            Request visionRequest, Object[] argv) {

        List<AnnotationItem> annotationItemList = getMethodParameterNamesByAnnotation(visionRequest.getMethod());

        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1,
                visionRequest.getHttpMethod(),
                tmplUrl(annotationItemList, visionRequest.getUrl(), argv)
        );

        Object param = getParam(visionRequest.getMethod(), argv);
        if (param != null) {
            request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1,
                    visionRequest.getHttpMethod(),
                    tmplUrl(annotationItemList, visionRequest.getUrl(), argv),
                    Unpooled.wrappedBuffer(visionRequest.getEncoder().encode(param).getBytes(StandardCharsets.UTF_8))
            );
        }

        // 构建http请求
        request.headers().set(HttpHeaderNames.HOST, host);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        if (visionRequest.getParamType() == HttpMap.ParamType.JSON) {
            request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON);
        }
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());

        // 添加pipeline
        if (hasTimeout()) {
            pipeline.addFirst(new ReadTimeoutHandler(getTimeout(), TimeUnit.SECONDS));
        }
        ResponseHandler clientHandler = new ResponseHandler(visionResponsePromise.getPromise());
        pipeline.addLast("clientHandler", clientHandler);

        return channelFuture.channel().writeAndFlush(request);
    }

    private boolean hasTimeout() {
        return timeout != -1;
    }

    private long getTimeout() {
        return timeout;
    }

    @Override
    public Object invoke(Object[] argv) throws Throwable {
        return this.send(argv).get();
    }

    private static List<AnnotationItem> getMethodParameterNamesByAnnotation(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (parameterAnnotations == null || parameterAnnotations.length == 0) {
            return null;
        }
        List<AnnotationItem> parameterNames = new ArrayList<>();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] parameterAnnotation = parameterAnnotations[i];
            for (Annotation annotation : parameterAnnotation) {
                if (annotation instanceof Param) {
                    Param param = (Param) annotation;
                    parameterNames.add(new AnnotationItem(i, param.value()));
                }
            }
        }
        return parameterNames;
    }

    private String tmplUrl(List<AnnotationItem> annotationItemList, String urlTmpl, Object[] argv) {
        if (argv == null) {
            return urlTmpl;
        }
        for (AnnotationItem annotationItem : annotationItemList) {
            int index = annotationItem.getIndex();
            if (index >= argv.length) {
                continue;
            }
            String name = annotationItem.getName();
            String value = argv[index].toString();
            urlTmpl = urlTmpl.replaceAll("\\{" + name + "}", value);
        }
        return urlTmpl;
    }

    private Object getParam(Method method, Object[] argv) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        if (parameterAnnotations == null || parameterAnnotations.length == 0) {
            return null;
        }
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] parameterAnnotation = parameterAnnotations[i];
            if (parameterAnnotation.length < 1) {
                return argv[i];
            }
        }
        return null;
    }
}
