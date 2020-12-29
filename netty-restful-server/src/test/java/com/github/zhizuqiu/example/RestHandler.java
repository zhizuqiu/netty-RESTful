package com.github.zhizuqiu.example;

import com.github.zhizuqiu.example.template.TemplateBody;
import com.github.zhizuqiu.example.template.TemplateObject;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpHandler;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.core.annotation.TemplateMap;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import com.github.zhizuqiu.nettyrestful.server.tools.PublicTools;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.netty.handler.codec.http.*;

import java.lang.reflect.Modifier;
import java.util.Map;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

@HttpHandler
public class RestHandler {
    /**
     * debug模式访问静态网页，改变页面元素无需刷新，生产环境请勿使用，而使用例如/index.html路由静态资源；
     */
    @HttpMap(path = "/index", returnType = HttpMap.ReturnType.TEXT_HTML)
    public String debugIndex() {
        String html = PublicTools.getHtmlFromResources("/netty-restful-test.html");
        System.out.println(html);
        return html;
    }

    /**
     * 路由
     */
    @HttpMap(path = "/", returnType = HttpMap.ReturnType.TEXT_HTML)
    public String getRoot() {
        RestMethodKey methodKey = new RestMethodKey("/netty-restful-test.html", HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
        return MethodData.getResourceAndTemplate(methodKey);
    }

    /**
     * 重定向
     */
    @HttpMap(path = "/redirectUrl", returnType = HttpMap.ReturnType.TEXT_HTML)
    public void redirect(DefaultFullHttpResponse response) {
        response.setStatus(HttpResponseStatus.MOVED_PERMANENTLY);
        response.headers().add(HttpHeaderNames.LOCATION, "/getRestMethod");
    }

    /**
     * 简单的GET请求
     */
    @HttpMap(path = "/getRestMethod")
    public Object get() {
        return "getRestMethod success 中文";
    }

    /**
     * 接受url?param1={value1}&param2={value2}
     */
    @HttpMap(path = "/getData")
    public String getData(Map<String, String> mapParam, DefaultFullHttpResponse response) {
        return mapParam.get("name");
    }

    /**
     * 接受Json串，response可以设置http状态码
     */
    @HttpMap(path = "/postJson",
            paramType = HttpMap.ParamType.JSON,
            returnType = HttpMap.ReturnType.APPLICATION_JSON,
            method = HttpMap.Method.POST)
    public TestMessage post(String jsonParam, DefaultFullHttpResponse response) {

        System.out.printf(jsonParam);
        TestMessage param = null;
        try {
            param = new Gson().fromJson(jsonParam, TestMessage.class);
        } catch (JsonSyntaxException e) {
            response.setStatus(INTERNAL_SERVER_ERROR);
        }

        return param;
    }

    /**
     * 接受表单
     */
    @HttpMap(path = "/postForm",
            paramType = HttpMap.ParamType.FORM_DATA,
            returnType = HttpMap.ReturnType.APPLICATION_JSON,
            method = HttpMap.Method.POST)
    public Object post(Map<String, String> mapParam, DefaultFullHttpResponse response) {

        System.out.printf(mapParam.toString());

        TestMessage param = new TestMessage();

        param.setType(mapParam.get("type"));
        param.setGroup(mapParam.get("group"));
        param.setMessage(mapParam.get("message"));

        return param;
    }

    /**
     * gson解析返回结果时，根据变量类型排除字段，如下：排除作用域为public的字段
     * modifierType的参数，参考java.lang.reflect.Modifier类
     */
    @HttpMap(path = "/test/Modifier",
            returnType = HttpMap.ReturnType.APPLICATION_JSON,
            gsonExcludeType = HttpMap.GsonExcludeType.Modifier,
            modifierType = Modifier.PUBLIC
    )
    public TestMessage testModifier() {

        TestMessage result = new TestMessage();
        result.setType("Modifier");
        result.setGroup("group");
        result.setMessage("message");
        result.set_exception("no exception");

        return result;
    }

    /**
     * gson解析返回结果时，根据注解@Expose排除字段，如下：排除没有@Expose的字段
     */
    @HttpMap(path = "/test/Expose",
            returnType = HttpMap.ReturnType.APPLICATION_JSON,
            gsonExcludeType = HttpMap.GsonExcludeType.Expose
    )
    public TestMessage testExpose() {

        TestMessage result = new TestMessage();
        result.setType("Expose");
        result.setGroup("group");
        result.setMessage("message");
        result.set_exception("no exception");

        return result;
    }

    /**
     * gson解析返回结果时，排除以某字符串开头的字段，如下：排除以_开头的字段
     */
    @HttpMap(path = "/test/SkipFieldStartWith",
            returnType = HttpMap.ReturnType.APPLICATION_JSON,
            gsonExcludeType = HttpMap.GsonExcludeType.SkipFieldStartWith,
            skipFieldStartWith = "_"
    )
    public TestMessage testSkipFieldStartWith() {

        TestMessage result = new TestMessage();
        result.setType("StartWith");
        result.setGroup("group");
        result.setMessage("message");
        result.set_exception("no exception");

        return result;
    }

    /**
     * 简单的GET请求
     */
    @HttpMap(path = "/cookie")
    public Map get(Map<String, String> mapParam, HttpRequest req, HttpResponse rep) {
        Map<String, String> cookies = PublicTools.getCookie(req);
        System.out.println(cookies);
        System.out.println(PublicTools.getHost(req));
        return cookies;
    }

    @TemplateMap(path = "/netty-restful-test.html")
    public TemplateObject test() {
        return new TemplateObject(
                "netty-restful-test",
                new TemplateBody("hello, click me to hide!")
        );
    }

}
