package com.github.zhizuqiu.nettyrestful.core.template;

public interface Template {
    String parse(Object scopes, String text) throws Exception;
}
