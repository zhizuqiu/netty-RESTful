package com.github.zhizuqiu.nettyrestfulcommon.codec;

public interface Encoder {
    String encode(Object object) throws EncodeException;
}
