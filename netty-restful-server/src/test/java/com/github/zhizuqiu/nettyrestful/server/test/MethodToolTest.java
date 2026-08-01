package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpHandler;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.tools.MethodTool;
import com.github.zhizuqiu.example.TestMessage;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class MethodToolTest {

    // ===== getMethod =====

    @Test
    public void getMethod_get_returnsGET() {
        assertEquals(HttpMap.Method.GET, MethodTool.getMethod(HttpMethod.GET));
    }

    @Test
    public void getMethod_post_returnsPOST() {
        assertEquals(HttpMap.Method.POST, MethodTool.getMethod(HttpMethod.POST));
    }

    @Test
    public void getMethod_put_returnsPUT() {
        assertEquals(HttpMap.Method.PUT, MethodTool.getMethod(HttpMethod.PUT));
    }

    @Test
    public void getMethod_delete_returnsDELETE() {
        assertEquals(HttpMap.Method.DELETE, MethodTool.getMethod(HttpMethod.DELETE));
    }

    @Test
    public void getMethod_other_returnsOTHER() {
        assertEquals(HttpMap.Method.OTHER, MethodTool.getMethod(HttpMethod.HEAD));
    }

    // ===== getParamTypeFromHeader =====

    @Test
    public void getParamTypeFromHeader_null_returnsURL_DATA() {
        assertEquals(HttpMap.ParamType.URL_DATA, MethodTool.getParamTypeFromHeader(null));
    }

    @Test
    public void getParamTypeFromHeader_json_returnsJSON() {
        assertEquals(HttpMap.ParamType.JSON, MethodTool.getParamTypeFromHeader("application/json; charset=UTF-8"));
    }

    @Test
    public void getParamTypeFromHeader_form_returnsFORM_DATA() {
        assertEquals(HttpMap.ParamType.FORM_DATA, MethodTool.getParamTypeFromHeader("application/x-www-form-urlencoded"));
    }

    @Test
    public void getParamTypeFromHeader_multipart_returnsMULTIPART_FORM_DATA() {
        assertEquals(HttpMap.ParamType.MULTIPART_FORM_DATA, MethodTool.getParamTypeFromHeader("multipart/form-data; boundary=----WebKitFormBoundary"));
    }

    @Test
    public void getParamTypeFromHeader_other_returnsURL_DATA() {
        assertEquals(HttpMap.ParamType.URL_DATA, MethodTool.getParamTypeFromHeader("text/plain"));
    }

    // ===== getSuffix =====

    @Test
    public void getSuffix_withExtension_returnsExtension() {
        assertEquals(".html", MethodTool.getSuffix("/path/to/file.html"));
    }

    @Test
    public void getSuffix_withoutExtension_returnsNull() {
        assertNull(MethodTool.getSuffix("/path/to/file"));
    }

    @Test
    public void getSuffix_null_returnsNull() {
        assertNull(MethodTool.getSuffix(null));
    }

    // ===== serializeString =====

    @HttpHandler
    public static class SerializeTestHelper {
        @HttpMap(path = "/json", method = HttpMap.Method.GET, returnType = HttpMap.ReturnType.APPLICATION_JSON)
        public TestMessage getJson() { return new TestMessage("test", "grp", "msg", null); }

        @HttpMap(path = "/text", method = HttpMap.Method.GET, returnType = HttpMap.ReturnType.TEXT_HTML)
        public String getText() { return "hello"; }
    }

    @Test
    public void serializeString_jsonReturnType_serializesAsJson() throws Exception {
        Method m = SerializeTestHelper.class.getMethod("getJson");
        HttpMap map = m.getAnnotation(HttpMap.class);
        String result = MethodTool.serializeString(map, new TestMessage("test", "grp", "msg", null));
        assertNotNull(result);
        assertTrue(result.contains("\"type\":\"test\""));
        assertTrue(result.contains("\"group\":\"grp\""));
    }

    @Test
    public void serializeString_textReturnType_usesToString() throws Exception {
        Method m = SerializeTestHelper.class.getMethod("getText");
        HttpMap map = m.getAnnotation(HttpMap.class);
        String result = MethodTool.serializeString(map, "hello");
        assertEquals("hello", result);
    }

    @Test
    public void serializeString_nullObject_textReturnType_returnsNull() throws Exception {
        Method m = SerializeTestHelper.class.getMethod("getText");
        HttpMap map = m.getAnnotation(HttpMap.class);
        assertNull(MethodTool.serializeString(map, null));
    }

    @Test
    public void serializeString_nullObject_jsonReturnType_returnsJsonNull() throws Exception {
        Method m = SerializeTestHelper.class.getMethod("getJson");
        HttpMap map = m.getAnnotation(HttpMap.class);
        assertEquals("null", MethodTool.serializeString(map, null));
    }

    // ===== newGsonExcludeStartsWithStr =====

    @Test
    public void newGsonExcludeStartsWithStr_excludesMatchingFields() {
        Object obj = new TestMessage("test", "grp", "msg", null);
        String json = MethodTool.newGsonExcludeStartsWithStr("type").toJson(obj);
        assertFalse(json.contains("\"type\""));
        assertTrue(json.contains("\"group\""));
    }

    // ===== getSuffix (补充边界场景) =====

    @Test
    public void getSuffix_simpleExtension_returnsWithDot() {
        assertEquals(".txt", MethodTool.getSuffix("a.txt"));
    }

    @Test
    public void getSuffix_multipleDots_returnsLastSuffix() {
        assertEquals(".gz", MethodTool.getSuffix("archive.tar.gz"));
    }

    @Test
    public void getSuffix_trailingDot_returnsDot() {
        assertEquals(".", MethodTool.getSuffix("a."));
    }
}
