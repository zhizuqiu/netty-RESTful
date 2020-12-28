import com.github.zhizuqiu.nettyrestfulclient.NettyRestClient;
import com.github.zhizuqiu.nettyrestfulfastjson.FastjsonDecoder;
import com.github.zhizuqiu.nettyrestfulfastjson.FastjsonEncoder;
import com.github.zhizuqiu.nettyrestfulgson.GsonDecoder;
import com.github.zhizuqiu.nettyrestfulgson.GsonEncoder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;

public class FastjsonTest {
    private static NettyIf nettyIf;

    @Before
    public void b() {
        nettyIf = NettyRestClient.builder()
                .host("localhost")
                .port(8083)
                .preProxy("/test")
                .timeout(5)
                .maxFrameSize(1024 * 100)
                .encoder(new FastjsonEncoder())
                .decoder(new FastjsonDecoder())
                .target(NettyIf.class);
    }

    @Test
    public void TestGson() throws Exception {
        System.out.println(nettyIf.config());
    }

}
