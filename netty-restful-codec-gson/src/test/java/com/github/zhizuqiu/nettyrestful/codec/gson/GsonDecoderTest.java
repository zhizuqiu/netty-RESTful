package com.github.zhizuqiu.nettyrestful.codec.gson;

import com.github.zhizuqiu.nettyrestful.core.codec.DecodeException;
import com.github.zhizuqiu.nettyrestful.core.codec.Decoder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GsonDecoderTest {

    private final Decoder decoder = new GsonDecoder(new Gson());

    @Test
    public void decode_validJson_returnsObject() throws Exception {
        String json = "{\"name\":\"alice\",\"age\":30}";

        Object result = decoder.decode(json, SampleMessage.class);

        assertTrue(result instanceof SampleMessage);
        assertEquals("alice", ((SampleMessage) result).getName());
        assertEquals(30, ((SampleMessage) result).getAge());
    }

    @Test
    public void decode_genericTypeList_returnsCorrectList() throws Exception {
        String json = "[\"a\",\"b\",\"c\"]";
        Type listType = new TypeToken<List<String>>() {}.getType();

        Object result = decoder.decode(json, listType);

        assertTrue(result instanceof List);
        assertEquals(Arrays.asList("a", "b", "c"), result);
    }

    @Test
    public void decode_nullInput_returnsNull() throws Exception {
        assertNull(decoder.decode(null, SampleMessage.class));
    }

    @Test
    public void decode_emptyString_returnsNull() throws Exception {
        // Gson 对空串返回 null（不抛异常）
        assertNull(decoder.decode("", SampleMessage.class));
    }

    /**
     * 解析非法 JSON 时应抛出 {@link DecodeException}，而不是把 Gson 的
     * 运行时异常（JsonSyntaxException）直接泄漏给调用方。
     */
    @Test
    public void decode_invalidJson_throwsDecodeException() {
        String badJson = "{not a valid json";

        try {
            decoder.decode(badJson, SampleMessage.class);
            fail("非法 JSON 应抛出 DecodeException");
        } catch (DecodeException e) {
            assertNotNull(e.getCause());
        } catch (Exception e) {
            fail("应抛出 DecodeException 而非 " + e.getClass().getName()
                    + "，说明实现未按接口契约包装异常");
        }
    }

    /**
     * 类型不匹配（如把对象 JSON 解析为数组类型）属于解析失败，应包装为
     * {@link DecodeException} 而非泄漏 Gson 的运行时异常。
     */
    @Test
    public void decode_typeMismatch_throwsDecodeException() {
        String json = "{\"name\":\"alice\"}";

        try {
            decoder.decode(json, new TypeToken<List<String>>() {}.getType());
            fail("类型不匹配应抛出 DecodeException");
        } catch (DecodeException e) {
            assertNotNull(e.getCause());
        } catch (Exception e) {
            fail("应抛出 DecodeException 而非 " + e.getClass().getName());
        }
    }
}
