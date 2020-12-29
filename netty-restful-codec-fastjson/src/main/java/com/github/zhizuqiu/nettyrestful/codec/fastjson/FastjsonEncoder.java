package com.github.zhizuqiu.nettyrestful.codec.fastjson;

import com.alibaba.fastjson.JSON;
import com.github.zhizuqiu.nettyrestful.core.codec.EncodeException;
import com.github.zhizuqiu.nettyrestful.core.codec.Encoder;

public class FastjsonEncoder implements Encoder {

    public FastjsonEncoder() {
    }

    @Override
    public String encode(Object object) throws EncodeException {
        return JSON.toJSONString(object);
    }
}
