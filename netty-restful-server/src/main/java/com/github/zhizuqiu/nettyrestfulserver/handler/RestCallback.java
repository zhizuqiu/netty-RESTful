package com.github.zhizuqiu.nettyrestfulserver.handler;

import io.netty.channel.EventLoopGroup;

public interface RestCallback {
    void call(EventLoopGroup bossGroup, EventLoopGroup workerGroup);
}
