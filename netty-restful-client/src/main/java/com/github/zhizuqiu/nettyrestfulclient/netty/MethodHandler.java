package com.github.zhizuqiu.nettyrestfulclient.netty;


public interface MethodHandler {
    Object invoke(Object[] argv) throws Throwable;
}
