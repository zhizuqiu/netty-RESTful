package com.github.zhizuqiu.nettyrestful.core.codec;


import com.github.zhizuqiu.nettyrestful.core.ClientException;

import static com.github.zhizuqiu.nettyrestful.core.Util.checkNotNull;

public class EncodeException extends ClientException {
    private static final long serialVersionUID = 1L;


    public EncodeException(String message) {
        super(checkNotNull(message, "message"));
    }


    public EncodeException(String message, Throwable cause) {
        super(message, checkNotNull(cause, "cause"));
    }
}
