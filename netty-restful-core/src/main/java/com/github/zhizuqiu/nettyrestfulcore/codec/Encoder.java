package com.github.zhizuqiu.nettyrestfulcore.codec;

public interface Encoder {
    String encode(Object object) throws EncodeException;
}
