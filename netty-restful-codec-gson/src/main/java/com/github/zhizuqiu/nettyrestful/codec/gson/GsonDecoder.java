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
        try {
            return gson.fromJson(response, type);
        } catch (RuntimeException e) {
            // Gson 以 JsonSyntaxException/JsonIOException 等运行时异常表达解析失败，
            // 但 Decoder 契约约定抛出 DecodeException，此处包装以避免异常类型泄漏。
            throw new DecodeException("Gson decode failed: " + e.getMessage(), e);
        }
    }
}
