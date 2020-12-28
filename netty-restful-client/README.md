# netty-restful-client

Run
---

```
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
```

Example
---

```
@HttpMap(path = "/config/inner")
Map config() throws Exception;
```


Install
---
1.download this lib

2.add this lib to your mvn repository.

```
mvn install:install-file -Dfile=netty-restful-client-2.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-client -Dversion=2.0-SNAPSHOT -Dpackaging=jar
```

3.and include dependencies:

```
<dependency>
    <groupId>com.github.zhizuqiu</groupId>
    <artifactId>netty-restful-client</artifactId>
    <version>2.0-SNAPSHOT</version>
</dependency>
```
