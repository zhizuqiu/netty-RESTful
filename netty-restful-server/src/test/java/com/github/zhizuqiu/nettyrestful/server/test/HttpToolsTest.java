package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.tools.HttpTools;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.junit.Assert.assertEquals;
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
}
