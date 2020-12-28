import com.github.zhizuqiu.nettyrestfulcommon.annotation.HttpMap;

import java.util.Map;

public interface NettyIf {

    @HttpMap(path = "/config/inner")
    Map config() throws Exception;

}
