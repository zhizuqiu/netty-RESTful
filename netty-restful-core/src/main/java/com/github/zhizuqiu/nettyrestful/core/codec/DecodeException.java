package com.github.zhizuqiu.nettyrestful.core.codec;


import com.github.zhizuqiu.nettyrestful.core.ClientException;
import com.github.zhizuqiu.nettyrestful.core.Util;

public class DecodeException extends ClientException {

    private static final long serialVersionUID = 1L;

    public DecodeException(String message) {
        super(Util.checkNotNull(message, "message"));
    }


    public DecodeException(String message, Throwable cause) {
        super(message, Util.checkNotNull(cause, "cause"));
    }
}
