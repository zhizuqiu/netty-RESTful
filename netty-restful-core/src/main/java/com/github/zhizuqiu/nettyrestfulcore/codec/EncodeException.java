package com.github.zhizuqiu.nettyrestfulcore.codec;


import com.github.zhizuqiu.nettyrestfulcore.ClientException;

import static com.github.zhizuqiu.nettyrestfulcore.Util.checkNotNull;

public class EncodeException extends ClientException {
    private static final long serialVersionUID = 1L;


    public EncodeException(String message) {
        super(checkNotNull(message, "message"));
    }


    public EncodeException(String message, Throwable cause) {
        super(message, checkNotNull(cause, "cause"));
    }
}
