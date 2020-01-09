package com.github.zhizuqiu.nettyrestfulserver.interceptor;

import java.util.List;

/**
 * 用户可以通过该接口自行定义需要生效哪些拦截器
 *
 * @author 借鉴 all4you/redant
 **/
public interface InterceptorBuilder {

    /**
     * 构造拦截器列表
     *
     * @return 列表
     */
    List<AbstractInterceptor> build();

}
