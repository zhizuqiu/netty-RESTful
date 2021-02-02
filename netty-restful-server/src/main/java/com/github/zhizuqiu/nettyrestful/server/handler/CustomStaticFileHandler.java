package com.github.zhizuqiu.nettyrestful.server.handler;

import io.netty.handler.codec.http.FullHttpResponse;

import java.io.RandomAccessFile;

public interface CustomStaticFileHandler {
    FullHttpResponse customResponse(String uri, RandomAccessFile raf, FullHttpResponse response);
}
