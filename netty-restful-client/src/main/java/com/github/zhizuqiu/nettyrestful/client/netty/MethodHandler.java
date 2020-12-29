package com.github.zhizuqiu.nettyrestful.client.netty;


public interface MethodHandler {
    Object invoke(Object[] argv) throws Throwable;
}
