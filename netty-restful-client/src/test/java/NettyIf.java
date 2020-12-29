import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;

import java.util.Map;

public interface NettyIf {

    @HttpMap(path = "/config/inner")
    Map config() throws Exception;

}
