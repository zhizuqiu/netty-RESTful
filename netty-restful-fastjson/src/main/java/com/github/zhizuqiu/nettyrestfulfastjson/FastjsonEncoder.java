package com.github.zhizuqiu.nettyrestfulfastjson;

import com.alibaba.fastjson.JSON;
import com.github.zhizuqiu.nettyrestfulcommon.codec.EncodeException;
import com.github.zhizuqiu.nettyrestfulcommon.codec.Encoder;

public class FastjsonEncoder implements Encoder {

    public FastjsonEncoder() {
    }

    @Override
    public String encode(Object object) throws EncodeException {
        return JSON.toJSONString(object);
    }
}
