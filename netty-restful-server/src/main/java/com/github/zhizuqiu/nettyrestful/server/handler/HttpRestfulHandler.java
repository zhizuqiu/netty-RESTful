package com.github.zhizuqiu.nettyrestful.server.handler;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import com.github.zhizuqiu.nettyrestful.server.tools.HttpTools;
import com.github.zhizuqiu.nettyrestful.server.tools.MethodTool;
import com.github.zhizuqiu.nettyrestful.server.tools.RequestParser;
import com.google.gson.Gson;
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
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpRestfulHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final List<String> restfulPreProxy;

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(HttpRestfulHandler.class);


    public HttpRestfulHandler(List<String> restfulPreProxy) {
        this.restfulPreProxy = restfulPreProxy;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        FullHttpResponse res = handle(req, this.restfulPreProxy);
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
    public FullHttpResponse handle(FullHttpRequest req, List<String> restfulPreProxy) {

        String url = RequestParser.getUrl(req.uri());

        RestMethodKey restMethodKey = new RestMethodKey(url,
                MethodTool.getMethod(req.method()),
                MethodTool.getParamTypeFromHeader(req.headers().get("Content-Type"))
        );

        RestMethodValue restMethodValue = MethodData.getRestMethod(restMethodKey);

        if (restMethodValue == null) {
            for (String proxy : restfulPreProxy) {
                if (url.startsWith(proxy)) {
                    RestMethodValue restMethodValueTemp = MethodData.getRestMethod(
                            new RestMethodKey(url.substring(proxy.length()),
                                    MethodTool.getMethod(req.method()),
                                    MethodTool.getParamTypeFromHeader(req.headers().get("Content-Type"))
                            ));
                    if (restMethodValueTemp != null) {
                        restMethodValue = restMethodValueTemp;
                        break;
                    }
                }
            }
        }

        if (restMethodValue != null) {

            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);

            HttpMap httpMap = restMethodValue.getHttpMap();

            String result;
            Object re = null;
            Object param;

            if (httpMap.paramType() == HttpMap.ParamType.FORM_DATA || httpMap.paramType() == HttpMap.ParamType.URL_DATA) {
                param = RequestParser.getParam(req);
            } else if (httpMap.paramType() == HttpMap.ParamType.JSON) {
                param = RequestParser.getJsonParam(req);
            } else {
                return HttpTools.getHttpResponse(req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR));
            }

            Method method = restMethodValue.getMethod();

            try {
                Object restHandler = restMethodValue.getInstance();
                int paramCount = method.getParameterTypes().length;
                if (paramCount == 0) {
                    re = method.invoke(restHandler);
                } else if (paramCount == 1) {
                    re = method.invoke(restHandler, response);
                } else if (paramCount == 2) {
                    re = method.invoke(restHandler, param, response);
                } else if (paramCount == 3) {
                    re = method.invoke(restHandler, param, req, response);
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("IllegalAccessException:" + e.getMessage());
                response.setStatus(INTERNAL_SERVER_ERROR);
            } catch (InvocationTargetException e) {
                LOGGER.error("InvocationTargetException:" + e.getMessage());
                response.setStatus(INTERNAL_SERVER_ERROR);
            }

            // 转json
            if (httpMap.returnType() == HttpMap.ReturnType.APPLICATION_JSON) {
                switch (httpMap.gsonExcludeType()) {
                    case Expose:
                        result = MethodTool.newGsonExcludeExpose().toJson(re);
                        break;
                    case Modifier:
                        result = MethodTool.newGsonExcludeModifier(httpMap.modifierType()).toJson(re);
                        break;
                    case SkipFieldStartWith:
                        result = MethodTool.newGsonExcludeStartsWithStr(httpMap.skipFieldStartWith()).toJson(re);
                        break;
                    case Default:
                        result = new Gson().toJson(re);
                        break;
                    default:
                        result = new Gson().toJson(re);
                        break;
                }
            } else {
                result = re.toString();
            }

            //成功
            FullHttpResponse res = HttpTools.getFullHttpResponse(result, response, httpMap.returnType());
            return HttpTools.getHttpResponse(req, res);
        } else {
            return null;
        }
    }


}
