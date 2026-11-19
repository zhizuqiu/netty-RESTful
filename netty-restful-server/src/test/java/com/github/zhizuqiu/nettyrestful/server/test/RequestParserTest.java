package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.server.tools.RequestParser;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.*;

public class RequestParserTest {

    // ===== getUrl =====

    @Test
    public void getUrl_withQueryString_returnsPathOnly() {
        assertEquals("/api/users", RequestParser.getUrl("/api/users?key=value&page=1"));
    }

    @Test
    public void getUrl_withoutQueryString_returnsOriginal() {
        assertEquals("/api/users", RequestParser.getUrl("/api/users"));
    }

    @Test
    public void getUrl_null_returnsEmptyString() {
        assertEquals("", RequestParser.getUrl(null));
    }

    @Test
    public void getUrl_emptyString_returnsEmptyString() {
        assertEquals("", RequestParser.getUrl(""));
    }

    // ===== getParam =====

    @Test
    public void getParam_queryParams_parsedCorrectly() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/test?key=value&page=1");
        Map<String, String> params = RequestParser.getParam(req);
        assertEquals("value", params.get("key"));
        assertEquals("1", params.get("page"));
    }

    @Test
    public void getParam_formUrlencodedBody_parsedCorrectly() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/test",
                Unpooled.buffer(256));
        req.content().writeBytes("name=alice&age=30".getBytes());
        req.headers().set("Content-Type", "application/x-www-form-urlencoded");
        Map<String, String> params = RequestParser.getParam(req);
        assertEquals("alice", params.get("name"));
        assertEquals("30", params.get("age"));
    }

    @Test
    public void getParam_queryAndFormCombined_bothParsed() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/test?source=query",
                Unpooled.buffer(256));
        req.content().writeBytes("name=bob".getBytes());
        req.headers().set("Content-Type", "application/x-www-form-urlencoded");
        Map<String, String> params = RequestParser.getParam(req);
        assertEquals("query", params.get("source"));
        assertEquals("bob", params.get("name"));
    }

    @Test
    public void getParam_noParams_returnsEmptyMap() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/test");
        Map<String, String> params = RequestParser.getParam(req);
        assertTrue(params.isEmpty());
    }

    @Test
    public void getParam_duplicateKeys_takesFirstValue() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/test?key=first&key=second");
        Map<String, String> params = RequestParser.getParam(req);
        assertEquals("first", params.get("key"));
    }

    // ===== getJsonParam =====

    @Test
    public void getJsonParam_jsonBody_extractedCorrectly() {
        String json = "{\"name\":\"test\",\"value\":42}";
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/test",
                Unpooled.wrappedBuffer(json.getBytes()));
        assertEquals(json, RequestParser.getJsonParam(req));
    }

    @Test
    public void getJsonParam_emptyBody_returnsEmptyString() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/test");
        assertEquals("", RequestParser.getJsonParam(req));
    }

    // ===== getCookies =====

    @Test
    public void getCookies_singleCookie_parsed() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/test");
        req.headers().set("Cookie", "sessionId=abc123");
        Set<Cookie> cookies = RequestParser.getCookies(req);
        assertEquals(1, cookies.size());
        Cookie cookie = cookies.iterator().next();
        assertEquals("sessionId", cookie.name());
        assertEquals("abc123", cookie.value());
    }

    @Test
    public void getCookies_multipleCookies_allParsed() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/test");
        req.headers().set("Cookie", "sessionId=abc123; userId=42");
        Set<Cookie> cookies = RequestParser.getCookies(req);
        assertEquals(2, cookies.size());
    }

    @Test
    public void getCookies_noCookieHeader_returnsEmptySet() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, GET, "/test");
        Set<Cookie> cookies = RequestParser.getCookies(req);
        assertTrue(cookies.isEmpty());
    }
}
