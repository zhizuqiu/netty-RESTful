package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class StaticFileHandlerTest {

    /**
     * Problem 1: 静态文件应被正确服务（内容与状态码正确）。
     * 作为 RandomAccessFile 关闭修复的正确性回归保护：多次请求都应得到完整内容，
     * 修复在传输完成回调中关闭 raf 不应破坏传输。
     */
    @Test
    public void servesStaticFile_multipleRequests() throws Exception {
        Path dir = Paths.get("target/static-test");
        Files.createDirectories(dir);
        Path file = dir.resolve("hello.txt");
        Files.write(file, "hello-static".getBytes(StandardCharsets.UTF_8));

        Config cfg = new Config();
        cfg.setStaticFilePath("target/static-test");
        MethodData.setConfig(cfg);

        TestServerSupport.Lifecycle server = TestServerSupport.start(false);
        try {
            for (int i = 0; i < 5; i++) {
                URL url = new URL("http://127.0.0.1:" + server.port() + "/hello.txt");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                assertEquals(200, con.getResponseCode());
                String body = readAll(con.getInputStream());
                assertEquals("第 " + i + " 次请求内容不一致", "hello-static", body);
            }
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
