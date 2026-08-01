package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * MethodData.getRestAndPreProxyMethod 路由测试。
 *
 * 该方法实现两段查找：
 * 1. 直接按 key 命中 REST_METHOD_MAP；
 * 2. 未命中时遍历 config.restfulPreProxy，若 url 以某个 proxy 前缀开头，
 *    则剥离前缀后再查一次。
 *
 * 使用 UUID 路径避免 MethodData 静态 Map 在测试间互相污染。
 */
public class MethodDataPreProxyTest {

    @After
    public void tearDown() {
        // 重置 preProxy，避免残留影响其它走 MethodData 的测试
        MethodData.setConfig(new Config());
    }

    @Test
    public void directMatch_returnsMethod() {
        String base = uniqueBase();
        RestMethodKey key = key(base + "/list");
        RestMethodValue value = new RestMethodValue();
        MethodData.putRestMethod(key, value);

        assertTrue(same(value, MethodData.getRestAndPreProxyMethod(key)));
    }

    @Test
    public void noMatch_returnsNull() {
        assertNull(MethodData.getRestAndPreProxyMethod(key(uniqueBase() + "/missing")));
    }

    @Test
    public void preProxy_stripsPrefixAndMatches() {
        String base = uniqueBase();
        // 真实路由注册在 /<base>/list
        RestMethodKey realKey = key(base + "/list");
        RestMethodValue value = new RestMethodValue();
        MethodData.putRestMethod(realKey, value);

        setPreProxy("/api");
        // 请求 /api/<base>/list → 剥离 /api → /<base>/list → 命中
        RestMethodKey requestKey = key("/api" + base + "/list");
        assertTrue(same(value, MethodData.getRestAndPreProxyMethod(requestKey)));
    }

    @Test
    public void preProxy_noMatchingPrefix_returnsNull() {
        String base = uniqueBase();
        MethodData.putRestMethod(key(base + "/list"), new RestMethodValue());

        setPreProxy("/api");
        // 请求 /other/<base>/list → 不以 /api 开头 → null
        assertNull(MethodData.getRestAndPreProxyMethod(key("/other" + base + "/list")));
    }

    @Test
    public void preProxy_multipleProxies_secondMatches() {
        String base = uniqueBase();
        RestMethodKey realKey = key(base + "/list");
        RestMethodValue value = new RestMethodValue();
        MethodData.putRestMethod(realKey, value);

        setPreProxy("/v1", "/v2");
        // 请求 /v2/<base>/list → 剥离 /v2 → /<base>/list → 命中
        assertTrue(same(value, MethodData.getRestAndPreProxyMethod(key("/v2" + base + "/list"))));
    }

    @Test
    public void preProxy_prefixMatchesButStrippedNotFound_returnsNull() {
        String base = uniqueBase();
        // 仅注册 /<base>（不是 /<base>/list）
        MethodData.putRestMethod(key(base), new RestMethodValue());

        setPreProxy("/api");
        // 请求 /api/<base>/list → 剥离 /api → /<base>/list → 未注册 → null
        assertNull(MethodData.getRestAndPreProxyMethod(key("/api" + base + "/list")));
    }

    @Test
    public void preProxy_emptyProxyList_returnsNullForUnregistered() {
        // 默认 Config（无 preProxy），未注册的 key 应返回 null
        assertNull(MethodData.getRestAndPreProxyMethod(key(uniqueBase() + "/x")));
    }

    // ---------------- helpers ----------------

    private static String uniqueBase() {
        return "/pp-" + UUID.randomUUID();
    }

    private static RestMethodKey key(String url) {
        return new RestMethodKey(url, HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
    }

    private static void setPreProxy(String... proxies) {
        Config cfg = new Config();
        cfg.setRestfulPreProxy(new ArrayList<>(Arrays.asList(proxies)));
        MethodData.setConfig(cfg);
    }

    private static boolean same(Object expected, Object actual) {
        return expected == actual;
    }
}
