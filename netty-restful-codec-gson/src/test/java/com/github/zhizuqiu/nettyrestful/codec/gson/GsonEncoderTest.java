package com.github.zhizuqiu.nettyrestful.codec.gson;

import com.github.zhizuqiu.nettyrestful.core.codec.Encoder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GsonEncoderTest {

    private final Encoder plainEncoder = new GsonEncoder(new Gson());

    @Test
    public void encode_simpleObject_returnsValidJson() {
        SampleMessage msg = new SampleMessage("alice", 30, "topsecret");

        String json = plainEncoder.encode(msg);

        assertNotNull(json);
        assertTrue("应包含 name 字段", json.contains("\"name\":\"alice\""));
        assertTrue("应包含 age 字段", json.contains("\"age\":30"));
        assertTrue("应包含 secret 字段", json.contains("\"secret\":\"topsecret\""));
    }

    @Test
    public void encode_null_returnsJsonNullLiteral() {
        assertEquals("null", plainEncoder.encode(null));
    }

    @Test
    public void encode_emptyString_returnsEmptyQuotedString() {
        assertEquals("\"\"", plainEncoder.encode(""));
    }

    @Test
    public void encode_list_returnsJsonArray() {
        List<Integer> nums = Arrays.asList(1, 2, 3);

        String json = plainEncoder.encode(nums);

        assertEquals("[1,2,3]", json);
    }

    @Test
    public void encode_withExposeExclusion_omitsUnannotatedFields() {
        Encoder encoder = new GsonEncoder(
                new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create());

        String json = encoder.encode(new SampleMessage("alice", 30, "topsecret"));

        assertTrue("标注 @Expose 的 name 应被序列化", json.contains("\"name\":\"alice\""));
        assertTrue("标注 @Expose 的 age 应被序列化", json.contains("\"age\":30"));
        assertTrue("未标注 @Expose 的 secret 应被排除",
                !json.contains("secret"));
    }

    @Test
    public void encode_symmetricWithDecoder_roundTrip() throws Exception {
        Gson gson = new Gson();
        Encoder encoder = new GsonEncoder(gson);
        GsonDecoder decoder = new GsonDecoder(gson);

        SampleMessage original = new SampleMessage("bob", 25, "hidden");
        String json = encoder.encode(original);

        Object decoded = decoder.decode(json, SampleMessage.class);

        assertTrue("解码结果应为 SampleMessage", decoded instanceof SampleMessage);
        assertEquals("bob", ((SampleMessage) decoded).getName());
        assertEquals(25, ((SampleMessage) decoded).getAge());
    }
}
