package com.github.zhizuqiu.nettyrestful.server.handler;

import com.github.zhizuqiu.nettyrestful.server.bean.ResourceValue;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.TemplateMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import com.github.zhizuqiu.nettyrestful.server.tools.HttpTools;
import com.github.zhizuqiu.nettyrestful.server.tools.MethodTool;
import com.github.zhizuqiu.nettyrestful.server.tools.RequestParser;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpHtmlHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(HttpHtmlHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        FullHttpResponse res = handle(req);
        if (res != null) {
            boolean keepAlive = HttpUtil.isKeepAlive(req);
            ChannelFuture f = ctx.channel().writeAndFlush(res);
            if (!keepAlive) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            ctx.fireChannelRead(req.retain());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * html的处理方法，只有在not found时返回null，否则返回FullHttpResponse
     */
    public FullHttpResponse handle(FullHttpRequest req) {
        if (MethodData.getConfig().getStaticFilePath() == null) {
            return null;
        }
        String url = RequestParser.getUrl(req.uri());

        if (url.endsWith(".html") || url.endsWith(".htm")) {
            RestMethodKey restMethodKey = new RestMethodKey(url,
                    MethodTool.getMethod(req.method()),
                    MethodTool.getParamTypeFromHeader(req.headers().get("Content-Type"))
            );

            ResourceValue resourceValue = MethodData.getResource(restMethodKey);
            if (resourceValue == null) {
                return null;
            }
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
            String contentType = resourceValue.getContentType();
            String value = resourceValue.getValue();

            TemplateMethodValue templateMethodValue = MethodData.getTemplateMethod(url);
            if (templateMethodValue != null) {
                Object re = null;
                Method method = templateMethodValue.getMethod();
                try {
                    Object restHandler = templateMethodValue.getInstance();
                    re = method.invoke(restHandler);
                } catch (IllegalAccessException e) {
                    LOGGER.error("IllegalAccessException:" + e.getMessage());
                    response.setStatus(INTERNAL_SERVER_ERROR);
                } catch (InvocationTargetException e) {
                    LOGGER.error("InvocationTargetException:" + e.getMessage());
                    response.setStatus(INTERNAL_SERVER_ERROR);
                }
                Class returnType = method.getReturnType();
                if (!"void".equals(returnType.getName())) {
                    if (re == null) {
                        return HttpTools.getHttpResponse(req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR));
                    }
                } else {
                    re = null;
                }

                if (re != null) {
                    value = MethodData.templateMustache(re, value);
                }
            }

            //成功
            FullHttpResponse res = HttpTools.getFullHttpResponse(value, response, contentType);
            return HttpTools.getHttpResponse(req, res);
        }

        return null;
    }

}
