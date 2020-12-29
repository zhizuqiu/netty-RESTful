package com.github.zhizuqiu.nettyrestful.core.codec;

import java.io.IOException;
import java.lang.reflect.Type;

public interface Decoder {
    Object decode(String response, Type type) throws IOException, DecodeException;
}
