package com.github.zhizuqiu.nettyrestfulserver.interceptor;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author 借鉴 all4you/redant
 **/
public abstract class AbstractInterceptor {

    /**
     * 拦截器的前置处理方法
     */
    public boolean preHandle(FullHttpRequest req, InterceptorResponse interceptorResponse) {
        return true;
    }

    /**
     * 拦截器的后置处理方法
     */
    public abstract void postHandle(FullHttpRequest req);

}
