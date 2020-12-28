package com.github.zhizuqiu.nettyrestfulcommon.codec;


import com.github.zhizuqiu.nettyrestfulcommon.ClientException;

import static com.github.zhizuqiu.nettyrestfulcommon.Util.checkNotNull;

public class DecodeException extends ClientException {

    private static final long serialVersionUID = 1L;

    public DecodeException(String message) {
        super(checkNotNull(message, "message"));
    }


    public DecodeException(String message, Throwable cause) {
        super(message, checkNotNull(cause, "cause"));
    }
}
