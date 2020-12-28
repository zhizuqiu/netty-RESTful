package com.github.zhizuqiu.nettyrestfulgson;

import com.github.zhizuqiu.nettyrestfulcommon.codec.DecodeException;
import com.github.zhizuqiu.nettyrestfulcommon.codec.Decoder;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.Type;

public class GsonDecoder implements Decoder {

    private final Gson gson;

    public GsonDecoder(Gson gson) {
        this.gson = gson;
    }

    @Override
    public Object decode(String response, Type type) throws IOException, DecodeException {
        return gson.fromJson(response, type);
    }
}
