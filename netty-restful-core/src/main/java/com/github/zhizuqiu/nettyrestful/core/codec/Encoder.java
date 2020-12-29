package com.github.zhizuqiu.nettyrestful.core.codec;

public interface Encoder {
    String encode(Object object) throws EncodeException;
}
