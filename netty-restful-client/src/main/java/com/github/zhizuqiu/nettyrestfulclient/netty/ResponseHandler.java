package com.github.zhizuqiu.nettyrestfulclient.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.concurrent.Promise;

import java.io.IOException;

public class ResponseHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private Promise<String> promise;

    public Promise<String> getPromise() {
        return promise;
    }

    public ResponseHandler(Promise<String> promise) {
        this.promise = promise;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse response) throws Exception {
        final HttpResponseStatus status = response.status();
        final HttpHeaders headers = response.headers();
        final ByteBuf content = response.content();

        if (status.equals(HttpResponseStatus.MOVED_PERMANENTLY)
                || status.equals(HttpResponseStatus.TEMPORARY_REDIRECT)) {
            if (headers.contains(HttpHeaderNames.LOCATION)) {
                this.promise.setFailure(new IOException("redirect for " + HttpHeaderNames.LOCATION));
                channelHandlerContext.close();
            } else {
                this.promise.setFailure(new Exception("Missing Location header on redirect"));
            }
        } else {
            // If connection was accepted maybe response has to be waited for
            if (status.code() != HttpResponseStatus.OK.code()
                    && status.code() != HttpResponseStatus.ACCEPTED.code()
                    && status.code() != HttpResponseStatus.CREATED.code()) {
                this.promise.setFailure(new IOException("Content was not readable. HTTP Status: " + status));
                return;
            }
            try {
                String str = content.toString(io.netty.util.CharsetUtil.UTF_8);
                promise.setSuccess(str);
            } catch (Exception e) {
                this.promise.setFailure(e);
            }
        }
    }
}
