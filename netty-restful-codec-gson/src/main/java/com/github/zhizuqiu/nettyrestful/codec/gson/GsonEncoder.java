package com.github.zhizuqiu.nettyrestful.codec.gson;

import com.github.zhizuqiu.nettyrestful.core.codec.EncodeException;
import com.github.zhizuqiu.nettyrestful.core.codec.Encoder;
import com.google.gson.Gson;

public class GsonEncoder implements Encoder {

    private final Gson gson;

    public GsonEncoder(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String encode(Object object) throws EncodeException {
        return gson.toJson(object);
    }
}
