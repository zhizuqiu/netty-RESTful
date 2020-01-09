import com.github.zhizuqiu.nettyrestfulclient.NettyRestClient;
import org.junit.Before;
import org.junit.Test;

public class TestClient {
    private NettyIf nettyIf;

    @Before
    public void b() {
        nettyIf = NettyRestClient.builder()
                .host("10.124.142.41")
                .port(8112)
                .timeout(5)
                .target(NettyIf.class);
    }

    @Test
    public void configTest() throws Exception {
        System.out.println(nettyIf.config());
    }
}
