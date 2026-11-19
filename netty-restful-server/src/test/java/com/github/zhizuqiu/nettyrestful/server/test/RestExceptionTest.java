package com.github.zhizuqiu.nettyrestful.server.test;

import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import org.junit.Before;
import org.junit.Test;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * REST 处理器异常路径测试：当业务方法抛出异常时，HttpRestfulHandler.invoke 应捕获
 * InvocationTargetException 并将响应状态置为 500，而不应使连接异常关闭。
 */
public class RestExceptionTest {

    @Before
    public void setUp() throws Exception {
        MethodData.setConfig(new Config());
        Method m = ExceptionTestHandler.class.getMethod("boom", String.class);
        HttpMap map = m.getAnnotation(HttpMap.class);
        Object inst = ExceptionTestHandler.class.newInstance();
        MethodData.putRestMethod(
                new RestMethodKey("/error/boom", HttpMap.Method.POST, HttpMap.ParamType.JSON),
                new RestMethodValue(map, m, inst));
    }

    @Test
    public void handlerThrows_returns500() throws Exception {
        TestServerSupport.Lifecycle server = TestServerSupport.start(false);
        try {
            URL url = new URL("http://127.0.0.1:" + server.port() + "/error/boom");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write("{\"x\":1}".getBytes(StandardCharsets.UTF_8));
            }

            int code = con.getResponseCode();
            assertEquals("业务方法抛异常时应返回 500", 500, code);
        } finally {
            server.stop();
        }
    }
}
