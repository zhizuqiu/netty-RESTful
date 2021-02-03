package com.github.zhizuqiu.nettyrestful.server.handler;

import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import com.github.zhizuqiu.nettyrestful.server.tools.HttpTools;
import com.github.zhizuqiu.nettyrestful.server.tools.MethodTool;
import com.github.zhizuqiu.nettyrestful.server.tools.RequestParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(HttpUploadServerHandler.class);

    private HttpRequest request;
    private final List<FileUpload> fileUploads = new ArrayList<>();
    private final String FILEUPLOADARRAYNAME = "[Lio.netty.handler.codec.http.multipart.FileUpload;";
    private RestMethodValue restMethodValue;
    private HttpData partialContent;

    private static final HttpDataFactory FACTORY =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); // Disk if size exceed

    private HttpPostRequestDecoder decoder;

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // should delete file
        // on exit (in normal
        // exit)
        DiskFileUpload.baseDirectory = null; // system temp directory
        DiskAttribute.deleteOnExitTemporaryFile = true; // should delete file on
        // exit (in normal exit)
        DiskAttribute.baseDirectory = null; // system temp directory
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        HandleResult result = handle(msg);
        switch (result.result) {
            case NEXT:
                ctx.fireChannelRead(msg);
                break;
            case CONTINUE:
                break;
            case END:
                Object re = invoke(result.response);
                String str = MethodTool.serializeString(restMethodValue.getHttpMap(), re);
                FullHttpResponse res = HttpTools.getFullHttpResponse(str, result.response, restMethodValue.getHttpMap().returnType());
                HttpTools.sendHttpResponse(ctx, request, res, false);
                reset();
                break;
            case EXCEPTION:
                HttpTools.sendHttpResponse(ctx, request, result.response, true);
                break;
            default:
        }
    }

    /**
     * method() OK
     * <p>
     * method(param) OK
     * method(fileUploads)  OK
     * method(response) OK
     * <p>
     * method(param,fileUploads) OK
     * method(param,response) OK
     * method(fileUploads,response) OK
     * method(fileUploads,param) OK
     * <p>
     * method(param,fileUploads,response) OK
     * method(fileUploads,param,response) OK
     */
    private Object invoke(FullHttpResponse response) {
        Method method = restMethodValue.getMethod();
        Object restHandler = restMethodValue.getInstance();

        Object re = null;
        try {
            Class[] cs = method.getParameterTypes();
            int paramCount = method.getParameterTypes().length;
            Object param = getParam();
            FileUpload[] fs = new FileUpload[fileUploads.size()];
            fileUploads.toArray(fs);
            if (paramCount == 0) {
                // method()
                re = method.invoke(restHandler);
            } else if (paramCount == 1) {
                if (cs[0] == HttpResponse.class || cs[0] == FullHttpResponse.class || cs[0] == DefaultFullHttpResponse.class) {
                    // method(response)
                    re = method.invoke(restHandler, response);
                } else if (FILEUPLOADARRAYNAME.equals(cs[0].getName())) {
                    // method(fileUploads)
                    re = method.invoke(restHandler, (Object) fs);
                } else {
                    // method(param)
                    re = method.invoke(restHandler, param);
                }
            } else if (paramCount == 2) {
                if ((cs[0] == Map.class || cs[0] == Object.class) && FILEUPLOADARRAYNAME.equals(cs[1].getName())) {
                    // method(param,fileUploads)
                    re = method.invoke(restHandler, param, fs);
                } else if ((cs[0] == Map.class || cs[0] == Object.class) && (cs[1] == HttpResponse.class || cs[1] == FullHttpResponse.class || cs[1] == DefaultFullHttpResponse.class)) {
                    // method(param,response)
                    re = method.invoke(restHandler, param, fs);
                } else if (FILEUPLOADARRAYNAME.equals(cs[0].getName()) && (cs[1] == HttpResponse.class || cs[1] == FullHttpResponse.class || cs[1] == DefaultFullHttpResponse.class)) {
                    // method(fileUploads,response)
                    re = method.invoke(restHandler, fs, response);
                } else if (FILEUPLOADARRAYNAME.equals(cs[0].getName()) && (cs[1] == Map.class || cs[1] == Object.class)) {
                    // method(fileUploads,param)
                    re = method.invoke(restHandler, fs, param);
                }
            } else if (paramCount == 3) {
                if ((cs[0] == Map.class || cs[0] == Object.class) && FILEUPLOADARRAYNAME.equals(cs[1].getName()) && (cs[2] == HttpResponse.class || cs[2] == FullHttpResponse.class || cs[2] == DefaultFullHttpResponse.class)) {
                    // method(param,fileUploads,response)
                    re = method.invoke(restHandler, param, fs, response);
                } else if (FILEUPLOADARRAYNAME.equals(cs[0].getName()) && (cs[1] == Map.class || cs[1] == Object.class) && (cs[2] == HttpResponse.class || cs[2] == FullHttpResponse.class || cs[2] == DefaultFullHttpResponse.class)) {
                    // method(fileUploads,param,response)
                    re = method.invoke(restHandler, fs, param, response);
                }
            }
            fileUploads.clear();
        } catch (IllegalAccessException e) {
            LOGGER.error("IllegalAccessException:" + e.getMessage());
            response.setStatus(INTERNAL_SERVER_ERROR);
        } catch (InvocationTargetException e) {
            LOGGER.error("InvocationTargetException:" + e.getMessage());
            response.setStatus(INTERNAL_SERVER_ERROR);
        }
        return re;
    }

    private Map<String, String> getParam() {
        Map<String, String> param = new HashMap<>();
        QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());
        Map<String, List<String>> uriAttributes = decoderQuery.parameters();
        for (Map.Entry<String, List<String>> attr : uriAttributes.entrySet()) {
            for (String attrVal : attr.getValue()) {
                param.put(attr.getKey(), attrVal);
            }
        }
        return param;
    }

    private HandleResult handle(HttpObject msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;
            String url = RequestParser.getUrl(request.uri());

            if (!HttpMethod.POST.equals(request.method())) {
                return new HandleResult(HandleEnum.NEXT);
            }
            if (request.headers().get("Content-Type") == null || !request.headers().get("Content-Type").contains("multipart/")) {
                return new HandleResult(HandleEnum.NEXT);
            }

            RestMethodKey restMethodKey = new RestMethodKey(url,
                    MethodTool.getMethod(request.method()),
                    MethodTool.getParamTypeFromHeader(request.headers().get("Content-Type"))
            );

            restMethodValue = MethodData.getRestAndPreProxyMethod(restMethodKey);

            if (restMethodValue == null) {
                return new HandleResult(HandleEnum.NEXT);
            }

            try {
                decoder = new HttpPostRequestDecoder(FACTORY, request);
            } catch (ErrorDataDecoderException e1) {
                e1.printStackTrace();
                FullHttpResponse res = HttpTools.getFullHttpResponse(e1.getMessage(), new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR), restMethodValue.getHttpMap().returnType());
                return new HandleResult(HandleEnum.EXCEPTION, e1, res);
            }
        }

        // check if the decoder was constructed before
        // if not it handles the form get
        if (decoder != null) {
            if (msg instanceof HttpContent) {
                // New chunk is received
                HttpContent chunk = (HttpContent) msg;
                try {
                    decoder.offer(chunk);
                } catch (ErrorDataDecoderException e1) {
                    e1.printStackTrace();
                    FullHttpResponse res = HttpTools.getFullHttpResponse(e1.getMessage(), new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR), restMethodValue.getHttpMap().returnType());
                    return new HandleResult(HandleEnum.EXCEPTION, e1, res);
                }
                // example of reading chunk by chunk (minimize memory usage due to
                // Factory)
                readHttpDataChunkByChunk();
                // example of reading only if at the end
                if (chunk instanceof LastHttpContent) {
                    // reset();
                    FullHttpResponse res = HttpTools.getFullHttpResponse("success", new DefaultFullHttpResponse(HTTP_1_1, OK), restMethodValue.getHttpMap().returnType());
                    return new HandleResult(HandleEnum.END, res);
                }
            }
        } else {
            // writeResponse(ctx.channel());
            return new HandleResult(HandleEnum.NEXT);
        }
        return new HandleResult(HandleEnum.CONTINUE);
    }

    private enum HandleEnum {
        // 往下传递
        NEXT,
        // 继续
        CONTINUE,
        // 结束
        END,
        // 异常
        EXCEPTION
    }

    private static class HandleResult {
        private HandleEnum result;
        private Exception exception;
        private FullHttpResponse response;

        public HandleResult(HandleEnum result) {
            this.result = result;
        }

        public HandleResult(HandleEnum result, FullHttpResponse response) {
            this.result = result;
            this.response = response;
        }

        public HandleResult(HandleEnum result, Exception exception, FullHttpResponse response) {
            this.result = result;
            this.exception = exception;
            this.response = response;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }

        public HandleEnum getResult() {
            return result;
        }

        public void setResult(HandleEnum result) {
            this.result = result;
        }

        public FullHttpResponse getResponse() {
            return response;
        }

        public void setResponse(FullHttpResponse response) {
            this.response = response;
        }

        @Override
        public String toString() {
            return "HandleResult{" +
                    "result=" + result +
                    ", exception=" + exception +
                    ", response=" + response +
                    '}';
        }
    }

    private void reset() {
        request = null;

        // destroy the decoder to release all resources
        decoder.destroy();
        decoder = null;
    }

    /**
     * Example of reading request by chunk and getting values from chunk to chunk
     */
    private void readHttpDataChunkByChunk() {
        try {
            while (decoder.hasNext()) {
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    // check if current HttpData is a FileUpload and previously set as partial
                    if (partialContent == data) {
                        LOGGER.info(" 100% (FinalSize: " + partialContent.length() + ")");
                        partialContent = null;
                    }
                    // new value
                    writeHttpData(data);
                }
            }
            // Check partial decoding for a FileUpload
            InterfaceHttpData data = decoder.currentPartialHttpData();
            if (data != null) {
                StringBuilder builder = new StringBuilder();
                if (partialContent == null) {
                    partialContent = (HttpData) data;
                    if (partialContent instanceof FileUpload) {
                        builder.append("Start FileUpload: ")
                                .append(((FileUpload) partialContent).getFilename()).append(" ");
                    } else {
                        builder.append("Start Attribute: ")
                                .append(partialContent.getName()).append(" ");
                    }
                    builder.append("(DefinedSize: ").append(partialContent.definedLength()).append(")");
                }
                if (partialContent.definedLength() > 0) {
                    builder.append(" ").append(partialContent.length() * 100 / partialContent.definedLength())
                            .append("% ");
                    LOGGER.info(builder.toString());
                } else {
                    builder.append(" ").append(partialContent.length()).append(" ");
                    LOGGER.info(builder.toString());
                }
            }
        } catch (EndOfDataDecoderException e1) {
            // end
        }
    }

    private void writeHttpData(InterfaceHttpData data) {
        if (data.getHttpDataType() == HttpDataType.Attribute) {
            // Attribute
        } else {
            if (data.getHttpDataType() == HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    // fileUpload.isInMemory();// tells if the file is in Memory
                    // or on File
                    // fileUpload.renameTo(dest); // enable to move into another
                    // File dest
                    // decoder.removeFileUploadFromClean(fileUpload); //remove
                    // the File of to delete file
                    fileUploads.add(fileUpload);
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}