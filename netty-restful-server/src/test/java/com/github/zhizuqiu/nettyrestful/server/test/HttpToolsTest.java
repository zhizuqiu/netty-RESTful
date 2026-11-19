package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.tools.HttpTools;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HttpToolsTest {

    /**
     * Problem 4: OK 路径应保留用户在模板 response 上设置的 header，
     * 而不是丢弃（原实现在 OK 路径 new DefaultFullHttpResponse 时未携带 headers）。
     */
    @Test
    public void getFullHttpResponse_withReturnType_preservesCustomHeadersOnOkPath() {
        DefaultFullHttpResponse tmpl = new DefaultFullHttpResponse(HTTP_1_1, OK);
        tmpl.headers().set("X-Custom", "v1");

        FullHttpResponse res = HttpTools.getFullHttpResponse("hello", tmpl, HttpMap.ReturnType.APPLICATION_JSON);

        assertEquals("v1", res.headers().get("X-Custom"));
        assertEquals("application/json; charset=UTF-8", res.headers().get(HttpHeaderNames.CONTENT_TYPE));
        assertEquals("hello", res.content().toString(CharsetUtil.UTF_8));
        assertTrue(HttpUtil.isContentLengthSet(res));
        res.release();
        tmpl.release();
    }

    /**
     * Problem 4: 第二个重载同样应在 OK 路径保留用户 header。
     */
    @Test
    public void getFullHttpResponse_withContentType_preservesCustomHeadersOnOkPath() {
        DefaultFullHttpResponse tmpl = new DefaultFullHttpResponse(HTTP_1_1, OK);
        tmpl.headers().set("X-Custom", "v2");

        FullHttpResponse res = HttpTools.getFullHttpResponse("hello", tmpl, "application/json");

        assertEquals("v2", res.headers().get("X-Custom"));
        assertEquals("application/json; charset=UTF-8", res.headers().get(HttpHeaderNames.CONTENT_TYPE));
        assertEquals("hello", res.content().toString(CharsetUtil.UTF_8));
        res.release();
        tmpl.release();
    }

    // ---------------- getFullHttpResponse returnType 各分支 ----------------

    @Test
    public void getFullHttpResponse_htmlReturnType_setsHtmlContentType() {
        DefaultFullHttpResponse tmpl = new DefaultFullHttpResponse(HTTP_1_1, OK);

        FullHttpResponse res = HttpTools.getFullHttpResponse("<p/>", tmpl, HttpMap.ReturnType.TEXT_HTML);

        assertEquals("text/html; charset=UTF-8", res.headers().get(HttpHeaderNames.CONTENT_TYPE));
        res.release();
        tmpl.release();
    }

    @Test
    public void getFullHttpResponse_plainReturnType_setsPlainContentType() {
        DefaultFullHttpResponse tmpl = new DefaultFullHttpResponse(HTTP_1_1, OK);

        FullHttpResponse res = HttpTools.getFullHttpResponse("plain", tmpl, HttpMap.ReturnType.TEXT_PLAIN);

        assertEquals("text/plain; charset=UTF-8", res.headers().get(HttpHeaderNames.CONTENT_TYPE));
        res.release();
        tmpl.release();
    }

    @Test
    public void getFullHttpResponse_multibyteBody_setsCorrectByteLength() {
        DefaultFullHttpResponse tmpl = new DefaultFullHttpResponse(HTTP_1_1, OK);
        // “中文” UTF-8 编码占 6 字节
        String body = "中文";

        FullHttpResponse res = HttpTools.getFullHttpResponse(body, tmpl, HttpMap.ReturnType.TEXT_PLAIN);

        assertEquals(6, res.content().readableBytes());
        assertEquals("6", res.headers().get(HttpHeaderNames.CONTENT_LENGTH));
        res.release();
        tmpl.release();
    }

    // ---------------- getFullHttpResponse contentType 重载 ----------------

    @Test
    public void getFullHttpResponse_nullContentType_setsPlainContentType() {
        DefaultFullHttpResponse tmpl = new DefaultFullHttpResponse(HTTP_1_1, OK);

        FullHttpResponse res = HttpTools.getFullHttpResponse("hi", tmpl, (String) null);

        assertEquals("text/plain; charset=UTF-8", res.headers().get(HttpHeaderNames.CONTENT_TYPE));
        res.release();
        tmpl.release();
    }

    @Test
    public void getFullHttpResponse_customContentType_appendsCharset() {
        DefaultFullHttpResponse tmpl = new DefaultFullHttpResponse(HTTP_1_1, OK);

        FullHttpResponse res = HttpTools.getFullHttpResponse("hi", tmpl, "application/xml");

        assertEquals("application/xml; charset=UTF-8", res.headers().get(HttpHeaderNames.CONTENT_TYPE));
        res.release();
        tmpl.release();
    }

    // ---------------- getFullHttpResponse 非 OK 路径 ----------------

    @Test
    public void getFullHttpResponse_nonOkStatus_returnTypeOverload_returnsEmptyBodyAndPreservesStatus() {
        DefaultFullHttpResponse tmpl = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
        tmpl.headers().set("X-Custom", "keep");

        FullHttpResponse res = HttpTools.getFullHttpResponse("ignored", tmpl, HttpMap.ReturnType.APPLICATION_JSON);

        assertEquals(BAD_REQUEST, res.status());
        assertEquals("", res.content().toString(CharsetUtil.UTF_8));
        assertEquals("keep", res.headers().get("X-Custom"));
        res.release();
        tmpl.release();
    }

    @Test
    public void getFullHttpResponse_nonOkStatus_contentTypeOverload_returnsEmptyBody() {
        DefaultFullHttpResponse tmpl = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);

        FullHttpResponse res = HttpTools.getFullHttpResponse("ignored", tmpl, "application/json");

        assertEquals(BAD_REQUEST, res.status());
        assertEquals("", res.content().toString(CharsetUtil.UTF_8));
        res.release();
        tmpl.release();
    }

    // ---------------- getHttpResponse ----------------

    @Test
    public void getHttpResponse_non200Status_appendsStatusTextAndSetsContentLength() {
        HttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);

        FullHttpResponse out = HttpTools.getHttpResponse(req, res);

        assertEquals(BAD_REQUEST, out.status());
        assertTrue("应将 status 文本写入响应体", out.content().toString(CharsetUtil.UTF_8).contains("400"));
        assertTrue(HttpUtil.isContentLengthSet(out));
        out.release();
    }

    @Test
    public void getHttpResponse_http11_keepAlive_doesNotSetConnectionHeader() {
        // HTTP/1.1 默认 keep-alive，无需显式设置 Connection 头
        HttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);

        FullHttpResponse out = HttpTools.getHttpResponse(req, res);

        assertFalse("HTTP/1.1 keep-alive 默认不应显式设置 Connection",
                out.headers().contains(HttpHeaderNames.CONNECTION));
        out.release();
    }

    @Test
    public void getHttpResponse_http10_keepAlive_setsKeepAliveHeader() {
        // HTTP/1.0 默认关闭，keep-alive 需显式声明
        HttpRequest req = new DefaultFullHttpRequest(HTTP_1_0, GET, "/");
        req.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_0, OK);

        FullHttpResponse out = HttpTools.getHttpResponse(req, res);

        assertEquals(HttpHeaderValues.KEEP_ALIVE.toString(),
                out.headers().get(HttpHeaderNames.CONNECTION));
        out.release();
    }

    @Test
    public void getHttpResponse_http10_default_setsCloseHeader() {
        // HTTP/1.0 无 Connection 头 → 默认关闭 → 响应应声明 close
        HttpRequest req = new DefaultFullHttpRequest(HTTP_1_0, GET, "/");
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_0, OK);

        FullHttpResponse out = HttpTools.getHttpResponse(req, res);

        assertEquals(HttpHeaderValues.CLOSE.toString(),
                out.headers().get(HttpHeaderNames.CONNECTION));
        out.release();
    }

    @Test
    public void getHttpResponse_http11_forceClose_setsCloseHeader() {
        HttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);

        FullHttpResponse out = HttpTools.getHttpResponse(req, res, true);

        assertEquals(HttpHeaderValues.CLOSE.toString(),
                out.headers().get(HttpHeaderNames.CONNECTION));
        out.release();
    }

    @Test
    public void getHttpResponse_withCookieHeader_addsSetCookie() {
        HttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);
        res.headers().set(HttpHeaderNames.COOKIE, "session=abc123");

        FullHttpResponse out = HttpTools.getHttpResponse(req, res);

        assertNotNull(out.headers().get(HttpHeaderNames.SET_COOKIE));
        out.release();
    }

    @Test
    public void getHttpResponse_noCookieHeader_doesNotAddSetCookie() {
        HttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/");
        DefaultFullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK);

        FullHttpResponse out = HttpTools.getHttpResponse(req, res);

        assertFalse(out.headers().contains(HttpHeaderNames.SET_COOKIE));
        out.release();
    }
}
