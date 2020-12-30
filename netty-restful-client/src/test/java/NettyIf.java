import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.core.annotation.Param;

public interface NettyIf {

    @HttpMap(path = "/getData?name={name}")
    String getData(@Param("name") String name) throws Exception;

    @HttpMap(path = "/postJson",
            paramType = HttpMap.ParamType.JSON,
            method = HttpMap.Method.POST)
    TestMessage postJson(TestMessage testMessage) throws Exception;
}
