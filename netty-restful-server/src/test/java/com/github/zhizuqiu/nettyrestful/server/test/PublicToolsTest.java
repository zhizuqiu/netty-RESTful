package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import com.github.zhizuqiu.nettyrestful.server.tools.PublicTools;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * PublicTools 综合测试：覆盖 getValue/各 header 委托方法、cookie 边界场景、
 * getHtmlFromResources 资源读取。其中部分用例用于 TDD 驱动修复。
 */
public class PublicToolsTest {

    @Before
    public void setUp() {
        // 重置为干净 Config，避免受其他测试用例静态状态污染
        MethodData.setConfig(new Config());
    }

    // ---------------- getValue 与 header 委托方法 ----------------

    @Test
    public void getValue_absentKey_returnsEmptyString() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");

        assertEquals("", PublicTools.getValue(req, "X-Not-Exist"));
    }

    @Test
    public void getValue_presentKey_returnsValue() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().set("X-Token", "abc123");

        assertEquals("abc123", PublicTools.getValue(req, "X-Token"));
    }

    /**
     * getValue 遍历所有 entry 并覆盖赋值，存在多个同名 header 时返回最后一个。
     * 此用例固化该既有行为。
     */
    @Test
    public void getValue_multipleSameKey_returnsLastValue() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().add("X-Seq", "1");
        req.headers().add("X-Seq", "2");
        req.headers().add("X-Seq", "3");

        assertEquals("3", PublicTools.getValue(req, "X-Seq"));
    }

    /**
     * HTTP header 名称大小写不敏感（RFC 7230），客户端通常发送首字母大写形式
     * （如 "Content-Length"）。getValue 应能匹配到，否则 getContentLength 等
     * 使用小写 key 的方法在真实请求下会失效。
     */
    @Test
    public void getValue_caseInsensitiveHeaderName_returnsValue() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        // 标准首字母大写形式
        req.headers().set("Content-Length", "42");

        // 用小写 key 查询（getContentLength 内部即用 "content-length"）
        assertEquals("42", PublicTools.getValue(req, "content-length"));
    }

    @Test
    public void getUserAgent_returnsValue() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().set("User-Agent", "Mozilla/5.0");

        assertEquals("Mozilla/5.0", PublicTools.getUserAgent(req));
    }

    @Test
    public void getConnection_returnsValue() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().set("Connection", "keep-alive");

        assertEquals("keep-alive", PublicTools.getConnection(req));
    }

    @Test
    public void getHost_returnsValue() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().set("Host", "example.com");

        assertEquals("example.com", PublicTools.getHost(req));
    }

    @Test
    public void getAccept_returnsValue() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().set("Accept", "application/json");

        assertEquals("application/json", PublicTools.getAccept(req));
    }

    @Test
    public void getContentLength_lowercaseHeader_returnsValue() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().set("content-length", "99");

        assertEquals("99", PublicTools.getContentLength(req));
    }

    @Test
    public void getContentLength_standardCapitalizedHeader_returnsValue() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        // 模拟真实客户端发送的标准大小写
        req.headers().set("Content-Length", "128");

        assertEquals("128", PublicTools.getContentLength(req));
    }

    @Test
    public void getCacheControl_returnsValue() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().set("cache-control", "no-cache");

        assertEquals("no-cache", PublicTools.getCacheControl(req));
    }

    // ---------------- getCookie 边界场景 ----------------

    @Test
    public void getCookie_noCookieHeader_returnsEmptyMap() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");

        assertTrue(PublicTools.getCookie(req).isEmpty());
    }

    @Test
    public void getCookie_multipleCookieHeaders_merged() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().add("Cookie", "a=1");
        req.headers().add("Cookie", "b=2");

        Map<String, String> cookies = PublicTools.getCookie(req);

        assertEquals("1", cookies.get("a"));
        assertEquals("2", cookies.get("b"));
    }

    @Test
    public void getCookie_cookieWithoutEquals_skipped() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().set("Cookie", "flagless; valid=yes");

        Map<String, String> cookies = PublicTools.getCookie(req);

        assertFalse("无 '=' 的片段应被跳过", cookies.containsKey("flagless"));
        assertEquals("yes", cookies.get("valid"));
    }

    @Test
    public void getCookie_emptyCookieValue_returnsEmptyMap() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().set("Cookie", "");

        assertTrue(PublicTools.getCookie(req).isEmpty());
    }

    // ---------------- getHtmlFromResources ----------------

    @Test
    public void getHtmlFromResources_nullStaticFilePath_returnsEmpty() {
        // Config 默认 staticFilePath=null
        assertEquals("", PublicTools.getHtmlFromResources("/index.html"));
    }

    @Test
    public void getHtmlFromResources_nullPath_returnsEmpty() throws Exception {
        Path dir = Paths.get("target/public-tools-test");
        Files.createDirectories(dir);
        Config cfg = new Config();
        cfg.setStaticFilePath(dir.toString());
        MethodData.setConfig(cfg);

        assertEquals("", PublicTools.getHtmlFromResources(null));
    }

    @Test
    public void getHtmlFromResources_validFile_returnsContent() throws Exception {
        Path dir = Paths.get("target/public-tools-test");
        Files.createDirectories(dir);
        Files.write(dir.resolve("page.html"), "<h1>hi</h1>".getBytes(StandardCharsets.UTF_8));
        Config cfg = new Config();
        cfg.setStaticFilePath(dir.toString());
        MethodData.setConfig(cfg);

        String html = PublicTools.getHtmlFromResources("page.html");

        assertTrue("应包含文件内容", html.contains("<h1>hi</h1>"));
    }

    @Test
    public void getHtmlFromResources_leadingSlashStripped_returnsContent() throws Exception {
        Path dir = Paths.get("target/public-tools-test");
        Files.createDirectories(dir);
        Files.write(dir.resolve("slash.html"), "slash-content".getBytes(StandardCharsets.UTF_8));
        Config cfg = new Config();
        cfg.setStaticFilePath(dir.toString());
        MethodData.setConfig(cfg);

        String html = PublicTools.getHtmlFromResources("/slash.html");

        assertTrue("前导 '/' 应被剥离以正确定位文件", html.contains("slash-content"));
    }

    @Test
    public void getHtmlFromResources_missingFile_returnsEmpty() throws Exception {
        Path dir = Paths.get("target/public-tools-test");
        Files.createDirectories(dir);
        Config cfg = new Config();
        cfg.setStaticFilePath(dir.toString());
        MethodData.setConfig(cfg);

        assertEquals("", PublicTools.getHtmlFromResources("no-such-file.html"));
    }
}
