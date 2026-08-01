package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * RestMethodKey equals/hashCode 契约测试。
 *
 * RestMethodKey 作为 REST_METHOD_MAP（ConcurrentHashMap）的 key，其 equals/hashCode
 * 必须严格基于 url + method + paramType 三个字段，否则会导致路由查找失败或内存泄漏。
 */
public class RestMethodKeyTest {

    private static RestMethodKey key(String url, HttpMap.Method m, HttpMap.ParamType p) {
        return new RestMethodKey(url, m, p);
    }

    // ===== equals =====

    @Test
    public void equals_sameInstance_returnsTrue() {
        RestMethodKey k = key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
        assertEquals(k, k);
    }

    @Test
    public void equals_sameFields_returnsTrue() {
        assertEquals(
                key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA),
                key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA));
    }

    @Test
    public void equals_differentUrl_returnsFalse() {
        assertNotEquals(
                key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA),
                key("/b", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA));
    }

    @Test
    public void equals_differentMethod_returnsFalse() {
        assertNotEquals(
                key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA),
                key("/a", HttpMap.Method.POST, HttpMap.ParamType.URL_DATA));
    }

    @Test
    public void equals_differentParamType_returnsFalse() {
        assertNotEquals(
                key("/a", HttpMap.Method.POST, HttpMap.ParamType.JSON),
                key("/a", HttpMap.Method.POST, HttpMap.ParamType.URL_DATA));
    }

    @Test
    public void equals_nullOther_returnsFalse() {
        assertFalse(key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA).equals(null));
    }

    @Test
    public void equals_differentType_returnsFalse() {
        assertFalse(key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA).equals("/a"));
    }

    @Test
    public void equals_symmetry() {
        RestMethodKey a = key("/x", HttpMap.Method.PUT, HttpMap.ParamType.JSON);
        RestMethodKey b = key("/x", HttpMap.Method.PUT, HttpMap.ParamType.JSON);
        assertTrue(a.equals(b) && b.equals(a));
    }

    @Test
    public void equals_nullUrlField_bothEqualWhenNull() {
        // url 为 null 时，两个 null-url 的 key 应相等（构造时允许 null）
        RestMethodKey a = new RestMethodKey(null, HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
        RestMethodKey b = new RestMethodKey(null, HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
        assertEquals(a, b);
    }

    @Test
    public void equals_nullUrlField_vsNonNull_returnsFalse() {
        RestMethodKey a = new RestMethodKey(null, HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
        RestMethodKey b = key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
        assertNotEquals(a, b);
    }

    // ===== hashCode =====

    @Test
    public void hashCode_equalObjects_haveSameHashCode() {
        assertEquals(
                key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA).hashCode(),
                key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA).hashCode());
    }

    @Test
    public void hashCode_consistentAcrossCalls() {
        RestMethodKey k = key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
        int h1 = k.hashCode();
        int h2 = k.hashCode();
        assertEquals(h1, h2);
    }

    @Test
    public void hashCode_nullUrlField_doesNotThrow() {
        RestMethodKey k = new RestMethodKey(null, HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
        k.hashCode(); // 不抛异常即可
    }

    // ===== 作为 Map key 的行为 =====

    @Test
    public void asMapKey_equalKeysRetrieveSameValue() {
        Map<RestMethodKey, String> map = new HashMap<>();
        map.put(key("/user", HttpMap.Method.POST, HttpMap.ParamType.JSON), "create");
        assertEquals("create", map.get(key("/user", HttpMap.Method.POST, HttpMap.ParamType.JSON)));
    }

    @Test
    public void asMapKey_differentParamType_doesNotCollide() {
        Map<RestMethodKey, String> map = new HashMap<>();
        map.put(key("/user", HttpMap.Method.POST, HttpMap.ParamType.JSON), "json");
        map.put(key("/user", HttpMap.Method.POST, HttpMap.ParamType.URL_DATA), "form");
        assertEquals("json", map.get(key("/user", HttpMap.Method.POST, HttpMap.ParamType.JSON)));
        assertEquals("form", map.get(key("/user", HttpMap.Method.POST, HttpMap.ParamType.URL_DATA)));
    }

    // ===== getter/setter =====

    @Test
    public void getters_returnConstructorValues() {
        RestMethodKey k = key("/path", HttpMap.Method.DELETE, HttpMap.ParamType.FORM_DATA);
        assertEquals("/path", k.getUrl());
        assertEquals(HttpMap.Method.DELETE, k.getMethod());
        assertEquals(HttpMap.ParamType.FORM_DATA, k.getParamType());
    }

    @Test
    public void setters_updateFields() {
        RestMethodKey k = key("/old", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
        k.setUrl("/new");
        k.setMethod(HttpMap.Method.PUT);
        k.setParamType(HttpMap.ParamType.JSON);
        assertEquals("/new", k.getUrl());
        assertEquals(HttpMap.Method.PUT, k.getMethod());
        assertEquals(HttpMap.ParamType.JSON, k.getParamType());
    }

    @Test
    public void setterNullUrl_thenGetReturnsNull() {
        RestMethodKey k = key("/a", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
        k.setUrl(null);
        assertNull(k.getUrl());
    }
}
