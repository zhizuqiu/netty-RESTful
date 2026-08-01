package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.server.tools.PublicTools;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import org.junit.Test;

import java.util.Map;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TDD RED 测试：验证 Cookie 解析 bug
 */
public class PublicToolsCookieTest {

    /**
     * Cookie 值中包含 = 号（如 base64 token）时，
     * split("=") 会产生超过2个元素，导致 cookie 被静默丢弃。
     */
    @Test
    public void getCookie_valueContainsEquals_parsedCorrectly() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().set("Cookie", "token=abc==def");

        Map<String, String> cookies = PublicTools.getCookie(req);

        assertTrue("cookie 'token' 应被解析到", cookies.containsKey("token"));
        assertEquals("abc==def", cookies.get("token"));
    }

    /**
     * 多个 cookie 之间用 "; " 分隔（标准格式带空格），
     * split(";") 后第二个 cookie 名带前导空格，导致 key 不正确。
     */
    @Test
    public void getCookie_multipleCookies_whitespaceTrimmed() {
        FullHttpRequest req = new DefaultFullHttpRequest(HTTP_1_1, POST, "/");
        req.headers().set("Cookie", "name=val; foo=bar");

        Map<String, String> cookies = PublicTools.getCookie(req);

        assertEquals("val", cookies.get("name"));
        assertEquals("bar", cookies.get("foo"));  // bug: 实际 key 是 " foo"
    }
}
