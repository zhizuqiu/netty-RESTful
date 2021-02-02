package com.github.zhizuqiu.nettyrestful.server.handler;

import com.github.zhizuqiu.nettyrestful.server.interceptor.InterceptorHandler;
import com.github.zhizuqiu.nettyrestful.server.interceptor.InterceptorResponse;
import com.github.zhizuqiu.nettyrestful.server.tools.HttpTools;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpPreHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
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
        ctx.fireChannelRead(req.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
