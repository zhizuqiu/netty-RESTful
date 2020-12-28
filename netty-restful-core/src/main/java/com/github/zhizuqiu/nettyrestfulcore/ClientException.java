package com.github.zhizuqiu.nettyrestfulcore;

public class ClientException extends RuntimeException {
    private static final long serialVersionUID = 0;
    private int status;

    protected ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ClientException(String message) {
        super(message);
    }

    protected ClientException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return this.status;
    }
}
