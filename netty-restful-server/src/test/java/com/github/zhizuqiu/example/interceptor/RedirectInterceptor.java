package com.github.zhizuqiu.example.interceptor;

import com.github.zhizuqiu.nettyrestfulserver.interceptor.AbstractInterceptor;
import com.github.zhizuqiu.nettyrestfulserver.interceptor.InterceptorResponse;
import com.github.zhizuqiu.nettyrestfulserver.tools.RequestParser;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.SEE_OTHER;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


/**
 * 拦截器
 */
public class RedirectInterceptor extends AbstractInterceptor {

    @Override
    public boolean preHandle(FullHttpRequest req, InterceptorResponse interceptorResponse) {
        System.out.println("RedirectInterceptor pre ------------");
        String url = RequestParser.getUrl(req.uri());
        if ("/redirect".equals(url)) {

            System.out.println("/redirect");

            // 配置重定向
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);
            res.setStatus(SEE_OTHER);
            res.headers().add(HttpHeaderNames.LOCATION, "/netty-restful-test.html");
            interceptorResponse.setResponse(res);

            return false;
        }

        return true;
    }

    @Override
    public void postHandle(FullHttpRequest req) {
        System.out.println("RedirectInterceptor post ------------");
    }
}
