import com.github.zhizuqiu.nettyrestful.client.NettyRestClient;
import com.github.zhizuqiu.nettyrestful.codec.fastjson.FastjsonDecoder;
import com.github.zhizuqiu.nettyrestful.codec.fastjson.FastjsonEncoder;
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
        System.out.println(nettyIf.getData("name_1"));
    }

}
