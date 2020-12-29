package com.github.zhizuqiu.nettyrestful.server.tools;

import com.github.zhizuqiu.nettyrestful.server.bean.ResourceValue;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.TemplateMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpHtmlTools {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(HttpHtmlTools.class);

    /**
     * html的处理方法，只有在not found时返回false，否则返回true
     */
    public static boolean handle(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        String url = RequestParser.getUrl(req.uri());

        RestMethodKey restMethodKey = new RestMethodKey(url,
                MethodTool.getMethod(req.method()),
                MethodTool.getParamTypeFromHeader(req.headers().get("Content-Type"))
        );

        ResourceValue resourceValue = MethodData.getResource(restMethodKey);
        if (resourceValue == null) {
            return false;
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
                    HttpTools.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR));
                    return true;
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
        HttpTools.sendHttpResponse(ctx, req, res);
        return true;
    }

}
