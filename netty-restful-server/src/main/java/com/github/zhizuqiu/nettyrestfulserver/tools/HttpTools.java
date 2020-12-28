package com.github.zhizuqiu.nettyrestfulserver.tools;

import com.github.zhizuqiu.nettyrestfulcore.annotation.HttpMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class HttpTools {

    /**
     * 用于生成FullHttpResponse的方法
     *
     * @param text       返回体
     * @param response   FullHttpResponse临时变量
     * @param returnType 返回类型
     * @return 用于响应请求的FullHttpResponse
     */
    public static FullHttpResponse getFullHttpResponse(String text, FullHttpResponse response, HttpMap.ReturnType returnType) {
        ByteBuf content = Unpooled.copiedBuffer("", CharsetUtil.UTF_8);
        FullHttpResponse res = new DefaultFullHttpResponse(response.protocolVersion(), response.status(), content, response.headers(), response.trailingHeaders());
        if (response.status() == OK) {
            content = Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
            res = new DefaultFullHttpResponse(response.protocolVersion(), response.status(), content, response.headers(), response.trailingHeaders());
            if (returnType == HttpMap.ReturnType.APPLICATION_JSON) {
                res.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
            } else if (returnType == HttpMap.ReturnType.TEXT_HTML) {
                res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            } else {
                res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            }
            HttpUtil.setContentLength(res, content.readableBytes());
        }
        return res;
    }

    /**
     * 用于生成FullHttpResponse的方法
     *
     * @param text        返回体
     * @param response    FullHttpResponse临时变量
     * @param contentType 返回类型
     * @return 用于响应请求的FullHttpResponse
     */
    public static FullHttpResponse getFullHttpResponse(String text, FullHttpResponse response, String contentType) {
        FullHttpResponse res = new DefaultFullHttpResponse(response.protocolVersion(), response.status());
        if (response.status() == OK) {
            ByteBuf content = Unpooled.copiedBuffer(text, CharsetUtil.UTF_8);
            res = new DefaultFullHttpResponse(response.protocolVersion(), response.status(), content);
            if (contentType != null) {
                res.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType + "; charset=UTF-8");
            } else {
                res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            }
            HttpUtil.setContentLength(res, content.readableBytes());
        }
        return res;
    }

    /**
     * 响应请求
     */
    public static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {

        // Generate an error page if response getStatus code is not OK (200).
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpUtil.setContentLength(res, res.content().readableBytes());
        }

        boolean keepAlive = HttpUtil.isKeepAlive(req);
        if (keepAlive) {
            if (!req.protocolVersion().isKeepAliveDefault()) {
                res.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
        } else {
            // Tell the client we're going to close the connection.
            res.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }

        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
