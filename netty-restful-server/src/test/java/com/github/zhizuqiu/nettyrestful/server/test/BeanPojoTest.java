package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpHandler;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.ResourceValue;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.bean.TemplateMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.Config;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * 基础 POJO/Bean 的 getter/setter/构造契约测试，补全覆盖空白。
 * 涵盖 RestMethodValue、ResourceValue、TemplateMethodValue、Config。
 */
public class BeanPojoTest {

    @HttpHandler
    public static class HelperHolder {
        @HttpMap(path = "/bean/echo", method = HttpMap.Method.GET,
                returnType = HttpMap.ReturnType.TEXT_PLAIN)
        public String echo() { return "ok"; }
    }

    private static HttpMap httpMapOf(String name) throws Exception {
        Method m = HelperHolder.class.getMethod(name);
        return m.getAnnotation(HttpMap.class);
    }

    // ===== RestMethodValue =====

    @Test
    public void restMethodValue_noArgConstructor_fieldsNull() {
        RestMethodValue v = new RestMethodValue();
        assertNull(v.getHttpMap());
        assertNull(v.getMethod());
        assertNull(v.getInstance());
    }

    @Test
    public void restMethodValue_fullConstructor_storesValues() throws Exception {
        HttpMap map = httpMapOf("echo");
        Method m = HelperHolder.class.getMethod("echo");
        Object inst = new HelperHolder();
        RestMethodValue v = new RestMethodValue(map, m, inst);

        assertSame(map, v.getHttpMap());
        assertSame(m, v.getMethod());
        assertSame(inst, v.getInstance());
    }

    @Test
    public void restMethodValue_setters_updateFields() throws Exception {
        RestMethodValue v = new RestMethodValue();
        HttpMap map = httpMapOf("echo");
        Method m = HelperHolder.class.getMethod("echo");
        Object inst = new HelperHolder();
        v.setHttpMap(map);
        v.setMethod(m);
        v.setInstance(inst);
        assertSame(map, v.getHttpMap());
        assertSame(m, v.getMethod());
        assertSame(inst, v.getInstance());
    }

    // ===== ResourceValue =====

    @Test
    public void resourceValue_constructor_storesValues() {
        ResourceValue rv = new ResourceValue("text/html", "<html/>");
        assertEquals("text/html", rv.getContentType());
        assertEquals("<html/>", rv.getValue());
    }

    @Test
    public void resourceValue_setters_updateFields() {
        ResourceValue rv = new ResourceValue("a", "b");
        rv.setContentType("application/json");
        rv.setValue("{}");
        assertEquals("application/json", rv.getContentType());
        assertEquals("{}", rv.getValue());
    }

    // ===== TemplateMethodValue =====

    @Test
    public void templateMethodValue_defaultsNull_thenSetters() throws Exception {
        TemplateMethodValue tv = new TemplateMethodValue();
        assertNull(tv.getMethod());
        assertNull(tv.getInstance());

        Method m = HelperHolder.class.getMethod("echo");
        Object inst = new HelperHolder();
        tv.setMethod(m);
        tv.setInstance(inst);
        assertSame(m, tv.getMethod());
        assertSame(inst, tv.getInstance());
    }

    // ===== Config =====

    @Test
    public void config_defaults_staticFilePathNull_andEmptyPreProxy() {
        Config cfg = new Config();
        assertNull(cfg.getStaticFilePath());
        assertNotNull("restfulPreProxy 默认应非 null", cfg.getRestfulPreProxy());
        assertTrue("restfulPreProxy 默认应为空", cfg.getRestfulPreProxy().isEmpty());
    }

    @Test
    public void config_setters_updateFields() {
        Config cfg = new Config();
        cfg.setStaticFilePath("/var/www");
        List<String> proxies = new ArrayList<>(Arrays.asList("/api", "/v2"));
        cfg.setRestfulPreProxy(proxies);

        assertEquals("/var/www", cfg.getStaticFilePath());
        assertEquals(proxies, cfg.getRestfulPreProxy());
    }

    @Test
    public void config_toString_containsFields() {
        Config cfg = new Config();
        cfg.setStaticFilePath("/www");
        cfg.setRestfulPreProxy(new ArrayList<>(Arrays.asList("/api")));
        String s = cfg.toString();
        assertTrue("toString 应包含 staticFilePath: " + s, s.contains("/www"));
        assertTrue("toString 应包含 restfulPreProxy: " + s, s.contains("/api"));
    }
}
