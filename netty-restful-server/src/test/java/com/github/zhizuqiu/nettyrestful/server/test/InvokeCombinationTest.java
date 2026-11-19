package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import org.junit.Before;
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
 * 测试 HttpRestfulHandler.invoke() 全部 19 种参数组合
 */
public class InvokeCombinationTest {

    private static final String[][] CASES = {
            // {methodName, path, expectedMarker}
            {"m0", "/invoke/m0", "ok-m0"},
            {"m1_p", "/invoke/m1_p", "ok-m1_p:value"},
            {"m1_j", "/invoke/m1_j", "ok-m1_j"},
            {"m1_r", "/invoke/m1_r", "ok-m1_r"},
            {"m1_q", "/invoke/m1_q", "ok-m1_q"},
            {"m2_pj", "/invoke/m2_pj", "ok-m2_pj"},
            {"m2_jp", "/invoke/m2_jp", "ok-m2_jp"},
            {"m2_pr", "/invoke/m2_pr", "ok-m2_pr"},
            {"m2_jr", "/invoke/m2_jr", "ok-m2_jr"},
            {"m2_pq", "/invoke/m2_pq", "ok-m2_pq"},
            {"m2_jq", "/invoke/m2_jq", "ok-m2_jq"},
            {"m3_pjr", "/invoke/m3_pjr", "ok-m3_pjr"},
            {"m3_jpr", "/invoke/m3_jpr", "ok-m3_jpr"},
            {"m3_pjq", "/invoke/m3_pjq", "ok-m3_pjq"},
            {"m3_jpq", "/invoke/m3_jpq", "ok-m3_jpq"},
            {"m3_pqr", "/invoke/m3_pqr", "ok-m3_pqr"},
            {"m3_jqr", "/invoke/m3_jqr", "ok-m3_jqr"},
            {"m4_pjqr", "/invoke/m4_pjqr", "ok-m4_pjqr"},
            {"m4_jpqr", "/invoke/m4_jpqr", "ok-m4_jpqr"},
    };

    @Before
    public void setUp() throws Exception {
        MethodData.setConfig(new Config());
        InvokeCombinationHandler inst = new InvokeCombinationHandler();
        for (Method m : InvokeCombinationHandler.class.getDeclaredMethods()) {
            HttpMap map = m.getAnnotation(HttpMap.class);
            if (map != null) {
                MethodData.putRestMethod(
                        new RestMethodKey(map.path(), HttpMap.Method.POST, HttpMap.ParamType.JSON),
                        new RestMethodValue(map, m, inst));
            }
        }
    }

    @Test
    public void allInvokeCombinations_correctParams() throws Exception {
        TestServerSupport.Lifecycle server = TestServerSupport.start(false);
        try {
            for (String[] c : CASES) {
                String result = sendPost(server.port(), c[1] + "?key=value");
                assertTrue(
                        "参数组合 " + c[0] + " 失败，期望包含 '" + c[2] + "'，实际: " + result,
                        result.contains(c[2])
                );
            }
        } finally {
            server.stop();
        }
    }

    private static String sendPost(int port, String path) throws IOException {
        URL url = new URL("http://127.0.0.1:" + port + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.setDoOutput(true);
        String payload = "{\"type\":\"test\"}";
        try (OutputStream os = con.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        try {
            int code = con.getResponseCode();
            if (code != 200) {
                return "HTTP " + code + ":" + readAll(con.getErrorStream());
            }
            return readAll(con.getInputStream());
        } catch (IOException e) {
            return "IOError:" + e.getMessage();
        }
    }

    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }
}
