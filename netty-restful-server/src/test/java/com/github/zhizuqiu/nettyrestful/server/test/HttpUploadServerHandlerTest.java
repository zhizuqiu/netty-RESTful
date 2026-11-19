package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.server.handler.HttpUploadServerHandler;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.fail;

/**
 * HttpUploadServerHandler 单元测试：聚焦 reset() 的资源清理契约。
 *
 * reset() 在 channelInactive / exceptionCaught / END 分支都会被调用，
 * 但当请求并非 multipart POST（例如普通 GET 或 JSON POST）时 decoder 从未被创建，
 * reset() 直接调用 decoder.destroy() 会抛出 NullPointerException，污染日志并
 * 导致 exceptionCaught 中后续的 ctx.channel().close() 被跳过。
 */
public class HttpUploadServerHandlerTest {

    /**
     * 当 decoder 为 null 时（未处理过 multipart 请求），reset() 必须安全返回，
     * 而不是抛出 NPE。
     */
    @Test
    public void reset_whenDecoderNull_doesNotThrow() throws Exception {
        HttpUploadServerHandler handler = new HttpUploadServerHandler();
        Method reset = HttpUploadServerHandler.class.getDeclaredMethod("reset");
        reset.setAccessible(true);
        try {
            reset.invoke(handler);
        } catch (InvocationTargetException e) {
            fail("reset() 在 decoder 为 null 时不应抛出异常, 实际: "
                    + e.getCause());
        }
    }
}
