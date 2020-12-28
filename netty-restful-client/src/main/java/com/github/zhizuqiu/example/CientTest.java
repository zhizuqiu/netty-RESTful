package com.github.zhizuqiu.example;

import com.github.zhizuqiu.nettyrestfulclient.NettyRestClient;
import com.github.zhizuqiu.nettyrestfulgson.GsonDecoder;
import com.github.zhizuqiu.nettyrestfulgson.GsonEncoder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CientTest {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static NettyIf nettyIf;

    private static void initNettyIf() {
        nettyIf = NettyRestClient.builder()
                .host("localhost")
                .port(8083)
                .preProxy("/test")
                .timeout(5)
                .maxFrameSize(1024 * 100)
                .encoder(new GsonEncoder(GSON))
                .decoder(new GsonDecoder(GSON))
                .target(NettyIf.class);
    }

    public static void main(String[] args) throws Exception {
        initNettyIf();
        System.out.println(nettyIf.config());
    }
}
