package com.github.zhizuqiu.example.interceptor;

import com.github.zhizuqiu.nettyrestfulserver.interceptor.AbstractInterceptor;
import com.github.zhizuqiu.nettyrestfulserver.interceptor.InterceptorResponse;
import com.github.zhizuqiu.nettyrestfulserver.tools.HttpTools;
import com.github.zhizuqiu.nettyrestfulserver.tools.RequestParser;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.util.Iterator;
import java.util.Set;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 拦截器
 */
public class CookieInterceptor extends AbstractInterceptor {

    @Override
    public boolean preHandle(FullHttpRequest req, InterceptorResponse interceptorResponse) {
        System.out.println("CookieInterceptor pre ------------");
        String url = RequestParser.getUrl(req.uri());
        if ("/getCookie".equals(url)) {

            System.out.println("/getCookie");

            // 读取cookie
            Set<Cookie> cookies = RequestParser.getCookies(req);
            System.out.println(cookies);

            // 遍历
            Iterator<Cookie> i = cookies.iterator();
            boolean has = false;
            while (i.hasNext()) {
                if ("1".equals(i.next().name())) {
                    has = true;
                }
            }

            FullHttpResponse res = HttpTools.getFullHttpResponse("/getCookie:" + has, new DefaultFullHttpResponse(HTTP_1_1, OK), "text/html");
            interceptorResponse.setResponse(res);
            return false;

        } else if ("/setCookie".equals(url)) {

            System.out.println("/setCookie");

            FullHttpResponse res = HttpTools.getFullHttpResponse("/setCookie", new DefaultFullHttpResponse(HTTP_1_1, OK), "text/html");

            Cookie cookie = new DefaultCookie("1", "1");
            cookie.setPath("/");
            cookie.setMaxAge(1000);
            res.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            interceptorResponse.setResponse(res);

            return false;

        } else if ("/deleteCookie".equals(url)) {

            System.out.println("/deleteCookie");

            FullHttpResponse res = HttpTools.getFullHttpResponse("/deleteCookie", new DefaultFullHttpResponse(HTTP_1_1, OK), "text/html");

            Cookie cookie = new DefaultCookie("1", "1");
            cookie.setPath("/");
            cookie.setMaxAge(1);
            res.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            interceptorResponse.setResponse(res);

            return false;

        }

        return true;
    }

    @Override
    public void postHandle(FullHttpRequest req) {
        System.out.println("CookieInterceptor post ------------");
    }
}
