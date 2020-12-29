package com.github.zhizuqiu.nettyrestful.server.handler;

import io.netty.channel.EventLoopGroup;

public interface RestCallback {
    void call(EventLoopGroup bossGroup, EventLoopGroup workerGroup);
}
