package com.github.zhizuqiu.nettyrestfulcore.codec;


import com.github.zhizuqiu.nettyrestfulcore.ClientException;

import static com.github.zhizuqiu.nettyrestfulcore.Util.checkNotNull;

public class DecodeException extends ClientException {

    private static final long serialVersionUID = 1L;

    public DecodeException(String message) {
        super(checkNotNull(message, "message"));
    }


    public DecodeException(String message, Throwable cause) {
        super(message, checkNotNull(cause, "cause"));
    }
}
