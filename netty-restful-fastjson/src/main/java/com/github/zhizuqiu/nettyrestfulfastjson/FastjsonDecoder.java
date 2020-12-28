package com.github.zhizuqiu.nettyrestfulfastjson;

import com.alibaba.fastjson.JSON;
import com.github.zhizuqiu.nettyrestfulcommon.codec.DecodeException;
import com.github.zhizuqiu.nettyrestfulcommon.codec.Decoder;

import java.io.IOException;
import java.lang.reflect.Type;

public class FastjsonDecoder implements Decoder {

    public FastjsonDecoder() {
    }

    @Override
    public Object decode(String response, Type type) throws IOException, DecodeException {
        return JSON.parseObject(response, type);
    }
}
