package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
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

public class UploadPipelineTest {

    /**
     * Problem 3: enableUpload=true 时，非 multipart 的 JSON POST 应能正确穿过
     * HttpUploadServerHandler（位于 HttpObjectAggregator 之前）到达 REST 处理器，
     * 而不因 SimpleChannelInboundHandler 透传消息未 retain 导致引用计数错误。
     */
    @Test
    public void nonMultipartJsonPost_reachesRestHandler() throws Exception {
        Method m = UploadTestHandler.class.getMethod("echoJson", String.class);
        HttpMap map = m.getAnnotation(HttpMap.class);
        Object inst = UploadTestHandler.class.newInstance();
        MethodData.setConfig(new Config());
        MethodData.putRestMethod(
                new RestMethodKey("/upload/json", HttpMap.Method.POST, HttpMap.ParamType.JSON),
                new RestMethodValue(map, m, inst));

        TestServerSupport.Lifecycle server = TestServerSupport.start(true);
        try {
            URL url = new URL("http://127.0.0.1:" + server.port() + "/upload/json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            String payload = "{\"type\":\"t\",\"group\":\"g\",\"message\":\"m\"}";
            try (OutputStream os = con.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }
            int code = con.getResponseCode();
            String body = readAll(con.getInputStream());
            assertEquals("响应码应为 200, 实际 body=" + body, 200, code);
            assertTrue("应包含返回的 type 字段, 实际: " + body, body.contains("\"type\":\"t\""));
        } finally {
            server.stop();
        }
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
