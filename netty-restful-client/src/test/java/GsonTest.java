import com.github.zhizuqiu.nettyrestful.client.NettyRestClient;
import com.github.zhizuqiu.nettyrestful.codec.gson.GsonDecoder;
import com.github.zhizuqiu.nettyrestful.codec.gson.GsonEncoder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;

public class GsonTest {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static NettyIf nettyIf;

    @Before
    public void b() {
        nettyIf = NettyRestClient.builder()
                .host("localhost")
                .port(80)
                // .preProxy("/test")
                // .timeout(5)
                .maxFrameSize(1024 * 100)
                .encoder(new GsonEncoder(GSON))
                .decoder(new GsonDecoder(GSON))
                .target(NettyIf.class);
    }

    @Test
    public void TestGson() throws Exception {
        System.out.println(nettyIf.getData("name_1"));
    }

    @Test
    public void postJson() throws Exception {
        System.out.println(nettyIf.postJson(new TestMessage("post", "group_1", "mess_1", "exception_1")));
    }
}
