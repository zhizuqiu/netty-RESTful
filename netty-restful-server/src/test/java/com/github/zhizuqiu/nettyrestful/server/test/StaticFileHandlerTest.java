package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StaticFileHandlerTest {

    private static final String STATIC_DIR = "target/static-test";

    @Before
    public void setUp() throws Exception {
        Path dir = Paths.get(STATIC_DIR);
        Files.createDirectories(dir);
        Files.write(dir.resolve("hello.txt"), "hello-static".getBytes(StandardCharsets.UTF_8));
        Files.createDirectories(dir.resolve("subdir"));

        Config cfg = new Config();
        cfg.setStaticFilePath(STATIC_DIR);
        MethodData.setConfig(cfg);
    }

    @After
    public void tearDown() {
        // 重置 Config，避免 staticFilePath 残留影响其它测试
        MethodData.setConfig(new Config());
    }

    /**
     * Problem 1: 静态文件应被正确服务（内容与状态码正确）。
     * 作为 RandomAccessFile 关闭修复的正确性回归保护：多次请求都应得到完整内容，
     * 修复在传输完成回调中关闭 raf 不应破坏传输。
     */
    @Test
    public void servesStaticFile_multipleRequests() throws Exception {
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

    /**
     * 文本文件应带正确的 Content-Type。
     */
    @Test
    public void textFile_hasTextPlainContentType() throws Exception {
        TestServerSupport.Lifecycle server = TestServerSupport.start(false);
        try {
            HttpURLConnection con = openGet(server.port(), "/hello.txt");
            assertEquals(200, con.getResponseCode());
            assertTrue("Content-Type 应为 text/plain, 实际: " + con.getContentType(),
                    con.getContentType().contains("text/plain"));
        } finally {
            server.stop();
        }
    }

    /**
     * 请求目录（带末尾 /）应返回 HTML 目录列表，包含目录内文件名。
     */
    @Test
    public void directoryListing_returnsHtmlWithFilename() throws Exception {
        TestServerSupport.Lifecycle server = TestServerSupport.start(false);
        try {
            HttpURLConnection con = openGet(server.port(), "/");
            assertEquals(200, con.getResponseCode());
            String body = readAll(con.getInputStream());
            assertTrue("目录列表应包含标题, 实际: " + body, body.contains("Listing of:"));
            assertTrue("目录列表应包含 hello.txt, 实际: " + body, body.contains("hello.txt"));
        } finally {
            server.stop();
        }
    }

    /**
     * 请求目录（不带末尾 /）应 302 重定向到带 / 的路径。
     */
    @Test
    public void directoryWithoutTrailingSlash_redirectsWithLocation() throws Exception {
        TestServerSupport.Lifecycle server = TestServerSupport.start(false);
        try {
            HttpURLConnection con = openGet(server.port(), "/subdir");
            con.setInstanceFollowRedirects(false);
            assertEquals(302, con.getResponseCode());
            assertEquals("/subdir/", con.getHeaderField("Location"));
        } finally {
            server.stop();
        }
    }

    /**
     * 对静态文件使用非 GET 方法应返回 405 Method Not Allowed。
     */
    @Test
    public void postToStaticFile_returnsMethodNotAllowed() throws Exception {
        TestServerSupport.Lifecycle server = TestServerSupport.start(false);
        try {
            URL url = new URL("http://127.0.0.1:" + server.port() + "/hello.txt");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write("x".getBytes(StandardCharsets.UTF_8));
            }
            assertEquals(405, con.getResponseCode());
        } finally {
            server.stop();
        }
    }

    /**
     * 请求不存在的文件应最终由 HttpNotFoundHandler 返回 404。
     */
    @Test
    public void getMissingFile_returns404() throws Exception {
        TestServerSupport.Lifecycle server = TestServerSupport.start(false);
        try {
            HttpURLConnection con = openGet(server.port(), "/no-such-file.txt");
            assertEquals(404, con.getResponseCode());
        } finally {
            server.stop();
        }
    }

    private static HttpURLConnection openGet(int port, String path) throws Exception {
        URL url = new URL("http://127.0.0.1:" + port + path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        return con;
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
