package com.github.zhizuqiu.nettyrestfulserver.tools;

import com.github.zhizuqiu.nettyrestfulserver.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestfulserver.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestfulserver.store.MethodData;
import com.github.zhizuqiu.nettyrestfulcommon.annotation.HttpMap;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpRestfulTools {

    private static InternalLogger logger = InternalLoggerFactory.getInstance(HttpRestfulTools.class);

    /**
     * restful的处理方法，只有在not found时返回false，否则返回true
     */
    public static boolean handle(ChannelHandlerContext ctx, FullHttpRequest req, List<String> restfulPreProxy) {

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
                HttpTools.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR));
                return true;
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
                logger.error("IllegalAccessException:" + e.getMessage());
                response.setStatus(INTERNAL_SERVER_ERROR);
            } catch (InvocationTargetException e) {
                logger.error("InvocationTargetException:" + e.getMessage());
                response.setStatus(INTERNAL_SERVER_ERROR);
            }

            Class returnType = method.getReturnType();
            if (!"void".equals(returnType.getName())) {
                if (re == null) {
                    HttpTools.sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR));
                    return true;
                }
            } else {
                re = "";
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
            HttpTools.sendHttpResponse(ctx, req, res);
            return true;
        } else {
            return false;
        }
    }


}
