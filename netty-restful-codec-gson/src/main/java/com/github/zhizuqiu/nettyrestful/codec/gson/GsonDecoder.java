package com.github.zhizuqiu.nettyrestful.codec.gson;

import com.github.zhizuqiu.nettyrestful.core.codec.DecodeException;
import com.github.zhizuqiu.nettyrestful.core.codec.Decoder;
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
