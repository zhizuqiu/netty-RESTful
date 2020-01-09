package com.github.zhizuqiu.example.interceptor;

import com.github.zhizuqiu.nettyrestfulserver.interceptor.AbstractInterceptor;
import com.github.zhizuqiu.nettyrestfulserver.interceptor.InterceptorBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 拦截器生成器
 */
public class CustomInterceptorBuilder implements InterceptorBuilder {
    @Override
    public List<AbstractInterceptor> build() {

        List<AbstractInterceptor> list = new ArrayList<>();

        list.add(new CookieInterceptor());

        list.add(new RedirectInterceptor());

        return list;
    }
}
