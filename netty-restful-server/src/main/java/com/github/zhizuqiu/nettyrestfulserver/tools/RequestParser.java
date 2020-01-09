package com.github.zhizuqiu.nettyrestfulserver.tools;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.*;

public class RequestParser {
    private RequestParser() {
    }

    /**
     * 获取cookie
     */
    public static Set<Cookie> getCookies(FullHttpRequest req) {
        Set<Cookie> cookies = new HashSet<>();
        String value = req.headers().get(HttpHeaderNames.COOKIE);
        if (value != null) {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }
        return cookies;
    }

    /**
     * 获取url
     */
    public static String getUrl(String uri) {
        if (uri == null) {
            return "";
        } else {
            if (uri.contains("?")) {
                return uri.substring(0, uri.indexOf("?"));
            } else {
                return uri;
            }
        }
    }

    /**
     * 从FullHttpRequest中提取请求参数，以键值对的方式返回
     * getRestMethod/delete等方式，从url中获取，如url?key=value
     * post/put等方式，从表单中获取
     *
     * @param req FullHttpRequest
     * @return 键值对参数
     */
    public static Map<String, String> getParam(FullHttpRequest req) {
        Map<String, String> requestParams = new HashMap<>();

        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        Map<String, List<String>> parame = decoder.parameters();
        for (String key : parame.keySet()) {
            List<String> value = parame.get(key);
            if (value != null && value.size() > 0) {
                requestParams.put(key, value.get(0));
            }
        }

        // 是POST请求
        HttpPostRequestDecoder decoderPost = new HttpPostRequestDecoder(req);
        decoderPost.offer(req);
        List<InterfaceHttpData> parmList = decoderPost.getBodyHttpDatas();
        for (InterfaceHttpData parm : parmList) {
            Attribute data = (Attribute) parm;
            try {
                requestParams.put(data.getName(), data.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return requestParams;
    }

    /**
     * 从FullHttpRequest中提取请求参数，以json的方式返回
     *
     * @param req FullHttpRequest
     * @return json
     */
    public static String getJsonParam(FullHttpRequest req) {
        ByteBuf jsonBuf = req.content();
        return jsonBuf.toString(CharsetUtil.UTF_8);
    }
}
