package com.github.zhizuqiu.nettyrestful.server.interceptor;


import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;

public class InterceptorHandler {

    public static boolean preHandle(FullHttpRequest req, InterceptorResponse interceptorResponse) {
        List<AbstractInterceptor> interceptors = MethodData.getInterceptorList();
        if (interceptors == null || interceptors.isEmpty()) {
            return true;
        }
        for (AbstractInterceptor interceptor : interceptors) {
            if (!interceptor.preHandle(req, interceptorResponse)) {
                return false;
            }
        }
        return true;
    }

    public static void postHandle(FullHttpRequest req) {
        List<AbstractInterceptor> interceptors = MethodData.getInterceptorList();
        if (interceptors == null || interceptors.isEmpty()) {
            return;
        }
        for (AbstractInterceptor interceptor : interceptors) {
            interceptor.postHandle(req);
        }
    }

}
