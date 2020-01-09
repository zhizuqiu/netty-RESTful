package com.github.zhizuqiu.nettyrestfulserver.handler;

import io.netty.handler.codec.http.FullHttpResponse;

import java.io.RandomAccessFile;

public interface StaticFileHandler {
    FullHttpResponse customResponse(String uri, RandomAccessFile raf, FullHttpResponse response);
}
