package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpHandler;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.IOException;

/**
 * 文件上传测试处理器：接收 FileUpload[]，回写第一个文件名与内容用于断言。
 */
@HttpHandler
public class UploadFileTestHandler {

    @HttpMap(path = "/upload/file",
            method = HttpMap.Method.POST,
            paramType = HttpMap.ParamType.MULTIPART_FORM_DATA,
            returnType = HttpMap.ReturnType.APPLICATION_JSON)
    public String uploadFile(FileUpload[] fileUploads) throws IOException {
        if (fileUploads == null || fileUploads.length == 0) {
            return "none";
        }
        FileUpload f = fileUploads[0];
        return f.getFilename() + "|" + f.getString();
    }
}
