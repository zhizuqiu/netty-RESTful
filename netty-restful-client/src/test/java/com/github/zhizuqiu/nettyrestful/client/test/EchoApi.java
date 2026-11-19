package com.github.zhizuqiu.nettyrestful.client.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.core.annotation.Param;

public interface EchoApi {

    @HttpMap(path = "/getData?name={name}")
    String getData(@Param("name") String name) throws Exception;
}
