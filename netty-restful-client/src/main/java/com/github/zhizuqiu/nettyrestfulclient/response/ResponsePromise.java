package com.github.zhizuqiu.nettyrestfulclient.response;

import com.github.zhizuqiu.nettyrestfulcommon.codec.Decoder;
import io.netty.util.concurrent.Promise;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.concurrent.TimeoutException;

public class ResponsePromise {
    private Type type;
    private Promise<String> promise;
    private Decoder decoder;
    protected Throwable exception;

    public ResponsePromise(Type type, Decoder decoder) {
        this.type = type;
        this.decoder = decoder;
    }

    public void waitForPromiseSuccess() {
        while (!promise.isDone() && !promise.isCancelled()) {
            promise.awaitUninterruptibly();
            if (!promise.isSuccess()) {
                this.setException(promise.cause());
            }
            break;
        }
    }

    public Object get() throws IOException, TimeoutException {
        waitForPromiseSuccess();

        if (promise.getNow() != null) {
            return decoder.decode(promise.getNow(), type);
        } else {
            if (this.exception instanceof IOException) {
                throw (IOException) this.exception;
            } else if (this.exception instanceof io.netty.handler.timeout.TimeoutException) {
                throw new TimeoutException();
            } else {
                throw new IOException(this.exception);
            }
        }

    }

    public void cancel(Throwable throwable) {
        this.promise.cancel(true);
        this.exception = throwable;
    }

    public void handleRetry(Throwable cause) {
        System.out.println("need handleRetry");
        /*
        try {
            this.retryPolicy.retry(connectionState, retryHandler, connectionFailHandler);
        } catch (RetryPolicy.RetryCancelled retryCancelled) {
            this.getNettyPromise().setFailure(cause);
        }
        */
    }

    // ------------

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Promise<String> getPromise() {
        return promise;
    }

    public void setPromise(Promise<String> promise) {
        this.promise = promise;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
