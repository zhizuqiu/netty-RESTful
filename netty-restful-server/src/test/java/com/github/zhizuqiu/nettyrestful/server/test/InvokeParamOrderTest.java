package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * TDD RED 测试：验证 invoke 参数顺序 bug
 */
public class InvokeParamOrderTest {

    /**
     * Issue #2: HttpRestfulHandler.invoke() 4参数版
     * method(String jsonParam, Map param, FullHttpRequest req, FullHttpResponse response)
     * bug: 传成了 (param, jsonParam, req, response)，导致 IllegalArgumentException
     */
    @Test
    public void restHandler_4Params_stringFirst_mapSecond() throws Exception {
        Method m = InvokeParamTestHandler.class.getMethod("handle4Params",
                String.class, Map.class,
                FullHttpRequest.class, FullHttpResponse.class);
        HttpMap map = m.getAnnotation(HttpMap.class);
        Object inst = InvokeParamTestHandler.class.newInstance();
        MethodData.setConfig(new Config());
        MethodData.putRestMethod(
                new RestMethodKey("/rest/4params", HttpMap.Method.POST, HttpMap.ParamType.JSON),
                new RestMethodValue(map, m, inst));

        TestServerSupport.Lifecycle server = TestServerSupport.start(false);
        try {
            URL url = new URL("http://127.0.0.1:" + server.port() + "/rest/4params?key=value");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setDoOutput(true);
            String payload = "{\"type\":\"t\"}";
            try (OutputStream os = con.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }
            int code = con.getResponseCode();
            String body = readAll(con.getInputStream());
            assertEquals("响应码应为 200, body=" + body, 200, code);
            assertTrue("应包含 ok-4params, 实际: " + body, body.contains("ok-4params"));
        } catch (IOException e) {
            fail("请求应成功返回200，但连接异常（可能是invoke参数顺序bug）: " + e.getMessage());
        } finally {
            server.stop();
        }
    }

    /**
     * Issue #1: HttpUploadServerHandler.invoke() 2参数版
     * method(Map param, FullHttpResponse response)
     * bug: 传成了 (param, fs)，fs 是 FileUpload[] 而非 response，导致 IllegalArgumentException
     */
    @Test
    public void uploadHandler_2Params_mapFirst_responseSecond() throws Exception {
        Method m = InvokeParamTestHandler.class.getMethod("handleParamResponse",
                Map.class, FullHttpResponse.class);
        HttpMap map = m.getAnnotation(HttpMap.class);
        Object inst = InvokeParamTestHandler.class.newInstance();
        MethodData.setConfig(new Config());
        MethodData.putRestMethod(
                new RestMethodKey("/upload/param-response", HttpMap.Method.POST, HttpMap.ParamType.MULTIPART_FORM_DATA),
                new RestMethodValue(map, m, inst));

        TestServerSupport.Lifecycle server = TestServerSupport.start(true);
        try {
            String boundary = "TestBoundary123";
            String body = "--" + boundary + "\r\n" +
                    "Content-Disposition: form-data; name=\"field1\"\r\n" +
                    "\r\n" +
                    "value1\r\n" +
                    "--" + boundary + "--\r\n";

            URL url = new URL("http://127.0.0.1:" + server.port() + "/upload/param-response");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
            int code = con.getResponseCode();
            String responseBody = readAll(con.getInputStream());
            assertEquals("响应码应为 200, body=" + responseBody, 200, code);
            assertTrue("应包含 ok-param-response, 实际: " + responseBody,
                    responseBody.contains("ok-param-response"));
        } catch (IOException e) {
            fail("请求应成功返回200，但连接异常（可能是invoke传参bug）: " + e.getMessage());
        } finally {
            server.stop();
        }
    }

    private static String readAll(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }
}
