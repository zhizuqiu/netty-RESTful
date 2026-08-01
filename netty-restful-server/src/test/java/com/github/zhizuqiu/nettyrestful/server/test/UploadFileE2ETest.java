package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * multipart 文件上传端到端测试：上传一个文本文件，验证 HttpUploadServerHandler
 * 将 FileUpload[] 正确传递给 REST 方法，且返回内容包含文件名与内容。
 */
public class UploadFileE2ETest {

    @Before
    public void setUp() throws Exception {
        MethodData.setConfig(new Config());
        Method m = UploadFileTestHandler.class.getMethod("uploadFile",
                io.netty.handler.codec.http.multipart.FileUpload[].class);
        HttpMap map = m.getAnnotation(HttpMap.class);
        Object inst = UploadFileTestHandler.class.newInstance();
        MethodData.putRestMethod(
                new RestMethodKey("/upload/file", HttpMap.Method.POST, HttpMap.ParamType.MULTIPART_FORM_DATA),
                new RestMethodValue(map, m, inst));
    }

    @Test
    public void multipartUpload_passesFileUploadsToHandler() throws Exception {
        TestServerSupport.Lifecycle server = TestServerSupport.start(true);
        try {
            String boundary = "----TestBoundary12345";
            String fileContent = "hello-upload-content";
            byte[] body = buildMultipartBody(boundary, "file", "test.txt", "text/plain", fileContent);

            URL url = new URL("http://127.0.0.1:" + server.port() + "/upload/file");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write(body);
            }

            int code = con.getResponseCode();
            String resp = readAll(con.getInputStream());
            assertEquals("响应码应为 200, 实际 body=" + resp, 200, code);
            assertTrue("应包含文件名, 实际: " + resp, resp.contains("test.txt"));
            assertTrue("应包含文件内容, 实际: " + resp, resp.contains(fileContent));
        } finally {
            server.stop();
        }
    }

    private static byte[] buildMultipartBody(String boundary, String fieldName, String filename,
                                             String contentType, String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append("\r\n");
        sb.append("Content-Disposition: form-data; name=\"").append(fieldName)
                .append("\"; filename=\"").append(filename).append("\"\r\n");
        sb.append("Content-Type: ").append(contentType).append("\r\n");
        sb.append("\r\n");
        sb.append(content).append("\r\n");
        sb.append("--").append(boundary).append("--\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String readAll(InputStream is) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }
}
