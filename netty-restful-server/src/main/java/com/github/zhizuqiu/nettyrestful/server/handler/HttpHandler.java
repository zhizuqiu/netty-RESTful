package com.github.zhizuqiu.nettyrestful.server.handler;

import com.github.zhizuqiu.nettyrestful.server.interceptor.InterceptorHandler;
import com.github.zhizuqiu.nettyrestful.server.interceptor.InterceptorResponse;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import com.github.zhizuqiu.nettyrestful.server.tools.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Outputs Http message.
 */
public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final List<String> restfulPreProxy;
    private final StaticFileHandler staticFileHandler;

    public HttpHandler(List<String> restfulPreProxy, StaticFileHandler staticFileHandler) {
        this.restfulPreProxy = restfulPreProxy;
        this.staticFileHandler = staticFileHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {

        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            HttpTools.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        String url = RequestParser.getUrl(req.uri());

        // 前置拦截器
        InterceptorResponse interceptorResponse = new InterceptorResponse();
        if (!InterceptorHandler.preHandle(req, interceptorResponse)) {
            if (interceptorResponse.getResponse() != null) {
                HttpTools.sendHttpResponse(ctx, req, interceptorResponse.getResponse());
            } else {
                HttpTools.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR));
            }
            return;
        }

        boolean hasDone = false;
        try {
            hasDone = HttpRestfulTools.handle(ctx, req, this.restfulPreProxy);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!hasDone) {
            if (MethodData.getConfig().getStaticFilePath() != null) {
                if (url.endsWith(".html") || url.endsWith(".htm")) {
                    try {
                        hasDone = HttpHtmlTools.handle(ctx, req);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!hasDone) {
                        HttpTools.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
                    }
                } else {
                    try {
                        hasDone = HttpStaticFileTools.handle(ctx, req, staticFileHandler);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!hasDone) {
                        HttpTools.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
                    }
                }
            } else {
                HttpTools.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            }
        }

        //后置拦截器
        InterceptorHandler.postHandle(req);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
