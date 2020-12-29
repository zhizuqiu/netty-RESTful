package com.github.zhizuqiu.nettyrestful.server.handler;

import io.netty.handler.codec.http.FullHttpResponse;

import java.io.RandomAccessFile;

public interface StaticFileHandler {
    FullHttpResponse customResponse(String uri, RandomAccessFile raf, FullHttpResponse response);
}
