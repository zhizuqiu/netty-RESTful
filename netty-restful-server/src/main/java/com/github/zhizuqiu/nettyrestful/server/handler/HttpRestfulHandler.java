package com.github.zhizuqiu.nettyrestful.server.handler;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import com.github.zhizuqiu.nettyrestful.server.tools.HttpTools;
import com.github.zhizuqiu.nettyrestful.server.tools.MethodTool;
import com.github.zhizuqiu.nettyrestful.server.tools.RequestParser;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * RESTful handler
 */
public class HttpRestfulHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(HttpRestfulHandler.class);

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
     * restful的处理方法，只有在not found时返回null，否则返回FullHttpResponse
     */
    public FullHttpResponse handle(FullHttpRequest req) {

        String url = RequestParser.getUrl(req.uri());

        RestMethodKey restMethodKey = new RestMethodKey(url,
                MethodTool.getMethod(req.method()),
                MethodTool.getParamTypeFromHeader(req.headers().get("Content-Type"))
        );

        RestMethodValue restMethodValue = MethodData.getRestAndPreProxyMethod(restMethodKey);

        if (restMethodValue != null) {
            HttpMap httpMap = restMethodValue.getHttpMap();

            Method method = restMethodValue.getMethod();
            Object restHandler = restMethodValue.getInstance();
            // getJsonParam在getParam之前
            String jsonParam = RequestParser.getJsonParam(req);
            Object param = RequestParser.getParam(req);

            // 根据缓存的元数据匹配 url，反射相应的方法，来处理请求
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
            Object re = invoke(method, restHandler, param, jsonParam, req, response);
            String result = MethodTool.serializeString(httpMap, re);

            //成功
            FullHttpResponse res = HttpTools.getFullHttpResponse(result, response, httpMap.returnType());
            return HttpTools.getHttpResponse(req, res);
        } else {
            return null;
        }
    }

    /**
     * method() OK
     * <p>
     * method(param) OK
     * method(jsonParam) OK
     * method(response) OK
     * method(req) OK
     * <p>
     * method(param,response) OK
     * method(jsonParam,response) OK
     * method(param,req) OK
     * method(jsonParam,req) OK
     * method(param,jsonParam) OK
     * method(jsonParam,param) OK
     * <p>
     * method(param,jsonParam,response) OK
     * method(jsonParam,param,response) OK
     * method(param,jsonParam,req) OK
     * method(jsonParam,param,req) OK
     * method(param,req,response) OK
     * method(jsonParam,req,response) OK
     * <p>
     * method(param,jsonParam,req,response) OK
     * method(jsonParam,param,req,response) OK
     */
    private Object invoke(Method method, Object restHandler, Object param, String jsonParam, FullHttpRequest req, FullHttpResponse response) {
        Object re = null;
        try {
            Class[] cs = method.getParameterTypes();
            int paramCount = method.getParameterTypes().length;
            if (paramCount == 0) {
                // method()
                re = method.invoke(restHandler);
            } else if (paramCount == 1) {
                if (cs[0] == HttpResponse.class || cs[0] == FullHttpResponse.class || cs[0] == DefaultFullHttpResponse.class) {
                    // method(response)
                    re = method.invoke(restHandler, response);
                } else if (cs[0] == HttpRequest.class || cs[0] == FullHttpRequest.class || cs[0] == DefaultFullHttpRequest.class) {
                    // method(req)
                    re = method.invoke(restHandler, req);
                } else if (cs[0] == String.class) {
                    // method(jsonParam)
                    re = method.invoke(restHandler, jsonParam);
                } else {
                    // method(param)
                    re = method.invoke(restHandler, param);
                }
            } else if (paramCount == 2) {
                if ((cs[0] == Map.class || cs[0] == Object.class) && cs[1] == String.class) {
                    // method(param,jsonParam)
                    re = method.invoke(restHandler, param, jsonParam);
                } else if (cs[0] == String.class && (cs[1] == Map.class || cs[1] == Object.class)) {
                    // method(jsonParam,param)
                    re = method.invoke(restHandler, jsonParam, param);
                } else if ((cs[0] == Map.class || cs[0] == Object.class) && (cs[1] == HttpResponse.class || cs[1] == FullHttpResponse.class || cs[1] == DefaultFullHttpResponse.class)) {
                    // method(param,response)
                    re = method.invoke(restHandler, param, response);
                } else if (cs[0] == String.class && (cs[1] == HttpResponse.class || cs[1] == FullHttpResponse.class || cs[1] == DefaultFullHttpResponse.class)) {
                    // method(jsonParam,response)
                    re = method.invoke(restHandler, jsonParam, response);
                } else if ((cs[0] == Map.class || cs[0] == Object.class) && (cs[1] == HttpRequest.class || cs[1] == FullHttpRequest.class || cs[1] == DefaultFullHttpRequest.class)) {
                    // method(param,req)
                    re = method.invoke(restHandler, param, req);
                } else if (cs[0] == String.class && (cs[1] == HttpRequest.class || cs[1] == FullHttpRequest.class || cs[1] == DefaultFullHttpRequest.class)) {
                    // method(jsonParam,req)
                    re = method.invoke(restHandler, jsonParam, req);
                }
            } else if (paramCount == 3) {
                if ((cs[0] == Map.class || cs[0] == Object.class) && cs[1] == String.class && (cs[2] == HttpResponse.class || cs[2] == FullHttpResponse.class || cs[2] == DefaultFullHttpResponse.class)) {
                    // method(param,jsonParam,response)
                    re = method.invoke(restHandler, param, jsonParam, response);
                } else if (cs[0] == String.class && (cs[1] == Map.class || cs[1] == Object.class) && (cs[2] == HttpResponse.class || cs[2] == FullHttpResponse.class || cs[2] == DefaultFullHttpResponse.class)) {
                    // method(jsonParam,param,response)
                    re = method.invoke(restHandler, jsonParam, param, response);
                } else if ((cs[0] == Map.class || cs[0] == Object.class) && cs[1] == String.class && (cs[2] == HttpRequest.class || cs[2] == FullHttpRequest.class || cs[2] == DefaultFullHttpRequest.class)) {
                    // method(param,jsonParam,req)
                    re = method.invoke(restHandler, param, jsonParam, req);
                } else if (cs[0] == String.class && (cs[1] == Map.class || cs[1] == Object.class) && (cs[2] == HttpRequest.class || cs[2] == FullHttpRequest.class || cs[2] == DefaultFullHttpRequest.class)) {
                    // method(jsonParam,param,req)
                    re = method.invoke(restHandler, jsonParam, param, req);
                } else if ((cs[0] == Map.class || cs[0] == Object.class) && (cs[1] == HttpRequest.class || cs[1] == FullHttpRequest.class || cs[1] == DefaultFullHttpRequest.class) && (cs[2] == HttpResponse.class || cs[2] == FullHttpResponse.class || cs[2] == DefaultFullHttpResponse.class)) {
                    // method(param,req,response)
                    re = method.invoke(restHandler, param, req, response);
                } else if (cs[0] == String.class && (cs[1] == HttpRequest.class || cs[1] == FullHttpRequest.class || cs[1] == DefaultFullHttpRequest.class) && (cs[2] == HttpResponse.class || cs[2] == FullHttpResponse.class || cs[2] == DefaultFullHttpResponse.class)) {
                    // method(jsonParam,req,response)
                    re = method.invoke(restHandler, jsonParam, req, response);
                }
            } else if (paramCount == 4) {
                if ((cs[0] == Map.class || cs[0] == Object.class) && cs[1] == String.class && (cs[2] == HttpRequest.class || cs[2] == FullHttpRequest.class || cs[2] == DefaultFullHttpRequest.class) && (cs[3] == HttpResponse.class || cs[3] == FullHttpResponse.class || cs[3] == DefaultFullHttpResponse.class)) {
                    // method(param,jsonParam,req,response)
                    re = method.invoke(restHandler, param, jsonParam, req, response);
                } else if (cs[0] == String.class && (cs[1] == Map.class || cs[1] == Object.class) && (cs[2] == HttpRequest.class || cs[2] == FullHttpRequest.class || cs[2] == DefaultFullHttpRequest.class) && (cs[3] == HttpResponse.class || cs[3] == FullHttpResponse.class || cs[3] == DefaultFullHttpResponse.class)) {
                    // method(param,jsonParam,req,response)
                    re = method.invoke(restHandler, param, jsonParam, req, response);
                }
            }
        } catch (IllegalAccessException e) {
            LOGGER.error("IllegalAccessException:" + e.getMessage());
            response.setStatus(INTERNAL_SERVER_ERROR);
        } catch (InvocationTargetException e) {
            LOGGER.error("InvocationTargetException:" + e.getMessage());
            response.setStatus(INTERNAL_SERVER_ERROR);
        }
        return re;
    }


}
