package com.github.zhizuqiu.nettyrestful.server;


import com.github.zhizuqiu.nettyrestful.core.annotation.HttpHandler;
import com.github.zhizuqiu.nettyrestful.core.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestful.core.annotation.TemplateMap;
import com.github.zhizuqiu.nettyrestful.core.template.Template;
import com.github.zhizuqiu.nettyrestful.server.bean.ResourceValue;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestful.server.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestful.server.bean.TemplateMethodValue;
import com.github.zhizuqiu.nettyrestful.server.handler.CustomStaticFileHandler;
import com.github.zhizuqiu.nettyrestful.server.handler.RestCallback;
import com.github.zhizuqiu.nettyrestful.server.initializer.RestfulServerInitializer;
import com.github.zhizuqiu.nettyrestful.server.interceptor.InterceptorBuilder;
import com.github.zhizuqiu.nettyrestful.server.store.Config;
import com.github.zhizuqiu.nettyrestful.server.store.MethodData;
import com.github.zhizuqiu.nettyrestful.server.tools.MethodTool;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NettyRestServer {
    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(NettyRestServer.class);

    private Boolean ssl;
    private String host;
    private Integer port;
    private List<String> packages;
    private Integer bossThreadCount;
    private Integer workThreadCount;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private RestCallback restCallback;
    private CustomStaticFileHandler customStaticFileHandler;
    private Config config = new Config();
    private String websocketPath;
    private ChannelHandler websocketHandler;
    private InterceptorBuilder interceptorBuilder;
    private List<Template> templateList;
    private Boolean enableUpload;

    public NettyRestServer() {
        ssl = false;
        host = "0.0.0.0";
        port = 80;
        packages = new ArrayList<>();
        bossThreadCount = 2;
        workThreadCount = 4;
        templateList = new ArrayList<>();
        enableUpload = false;
    }

    public void run() throws Exception {

        initConfig();
        initTemplate();
        if (config.getStaticFilePath() != null) {
            initResource();
        }
        initMethodData(packages);
        initInterceptor(interceptorBuilder);

        final SslContext sslCtx;
        if (ssl) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
        } else {
            sslCtx = null;
        }

        bossGroup = new NioEventLoopGroup(bossThreadCount);
        workerGroup = new NioEventLoopGroup(workThreadCount);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new RestfulServerInitializer(
                            sslCtx,
                            websocketPath,
                            websocketHandler,
                            this.customStaticFileHandler,
                            enableUpload
                    ));

            Channel ch = b.bind(host, port).sync().channel();

            LOGGER.info("listen to " + (ssl ? "https" : "http") + "://" + host + ":" + port + '/');
            LOGGER.info("bossThreadCount:" + bossThreadCount);
            LOGGER.info("workThreadCount:" + workThreadCount);

            if (restCallback != null) {
                restCallback.call(bossGroup, workerGroup);
            }

            ch.closeFuture().sync();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    private void initResource() {
        Map<String, String> stringStringMap = new MethodTool().getResources();
        if (stringStringMap != null) {
            for (String path : stringStringMap.keySet()) {
                String re = stringStringMap.get(path);
                RestMethodKey restMethodKey = new RestMethodKey(path, HttpMap.Method.GET, HttpMap.ParamType.URL_DATA);
                String suffix = MethodTool.getSuffix(path);
                String contentType = MethodData.suffixMapContentType.get(suffix);
                ResourceValue resourceValue = new ResourceValue(contentType, re);
                MethodData.putResource(restMethodKey, resourceValue);
            }
        }

        LOGGER.info("load resources:");
        for (RestMethodKey restMethodKey : MethodData.resourcesKeySet()) {
            LOGGER.info(restMethodKey.getUrl());
        }
    }

    private void initMethodData(List<String> paths) {

        if (paths == null) {
            return;
        }

        // 扫描
        List<Class> classes = MethodTool.getClasses(paths);

        for (Class httpHandlerClass : classes) {

            Annotation[] annotations = httpHandlerClass.getAnnotations();

            if (annotations == null || annotations.length < 1) {
                continue;
            }

            HttpHandler httpHandler = null;
            for (Annotation annotation : annotations) {
                if (annotation instanceof HttpHandler) {
                    httpHandler = (HttpHandler) annotation;
                }
            }

            if (httpHandler == null) {
                continue;
            }

            Object instance;
            try {
                instance = httpHandlerClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                LOGGER.error("class " + httpHandlerClass.getName() + " no default constructor");
                continue;
            }

            // 扫描 @HttpMap 注解，分析出请求的元数据，并缓存到内存中
            for (Method method : httpHandlerClass.getMethods()) {
                HttpMap todoAnnotation = method.getAnnotation(HttpMap.class);
                if (todoAnnotation != null) {
                    RestMethodValue restMethodValue = new RestMethodValue(todoAnnotation, method, instance);
                    RestMethodKey restMethodKey = new RestMethodKey(todoAnnotation.path(), todoAnnotation.method(), todoAnnotation.paramType());
                    MethodData.putRestMethod(restMethodKey, restMethodValue);
                    LOGGER.info("load method:[" + todoAnnotation.method() + "][" + todoAnnotation.path() + "][" + todoAnnotation.paramType() + "]");
                }
            }

            // @TemplateMap
            for (Method method : httpHandlerClass.getMethods()) {
                TemplateMap todoAnnotation = method.getAnnotation(TemplateMap.class);

                if (todoAnnotation != null) {
                    TemplateMethodValue templateMethodValue = new TemplateMethodValue();
                    templateMethodValue.setMethod(method);
                    templateMethodValue.setInstance(instance);

                    String path = todoAnnotation.path();
                    MethodData.putTemplateMethod(path, templateMethodValue);
                    LOGGER.info("load methon:[TemplateMap][" + path + "]");
                }
            }
        }
    }

    private void initConfig() {
        MethodData.setConfig(config);
    }

    private void initTemplate() {
        MethodData.setTemplateList(templateList);
    }

    /**
     * 初始化拦截器
     */
    private void initInterceptor(InterceptorBuilder interceptorBuilder) {
        if (interceptorBuilder == null) {
            return;
        }
        MethodData.setInterceptorList(interceptorBuilder.build());
    }

    public static class NettyRestServerBuilder {
        NettyRestServer nettyRestServer;

        public NettyRestServerBuilder() {
            nettyRestServer = new NettyRestServer();
        }

        public NettyRestServerBuilder setSsl(Boolean ssl) {
            nettyRestServer.setSsl(ssl);
            return this;
        }

        public NettyRestServerBuilder setHost(String host) {
            nettyRestServer.setHost(host);
            return this;
        }

        public NettyRestServerBuilder setPort(Integer port) {
            nettyRestServer.setPort(port);
            return this;
        }

        public NettyRestServerBuilder setPackages(List<String> packages) {
            nettyRestServer.setPackages(packages);
            return this;
        }

        public NettyRestServerBuilder setPackages(String... packages) {
            List<String> packagesTemp = new ArrayList<>(Arrays.asList(packages));
            nettyRestServer.setPackages(packagesTemp);
            return this;
        }

        public NettyRestServerBuilder setStaticFilePath(String path) {
            if (nettyRestServer.config == null) {
                nettyRestServer.config = new Config();
            }
            nettyRestServer.config.setStaticFilePath(path);
            return this;
        }

        public NettyRestServerBuilder setBossThreadCount(Integer bossThreadCount) {
            nettyRestServer.setBossThreadCount(bossThreadCount);
            return this;
        }

        public NettyRestServerBuilder setWorkThreadCount(Integer workThreadCount) {
            nettyRestServer.setWorkThreadCount(workThreadCount);
            return this;
        }

        public NettyRestServerBuilder setRestCallback(RestCallback restCallback) {
            nettyRestServer.setRestCallback(restCallback);
            return this;
        }

        public NettyRestServerBuilder setStaticFileHandler(CustomStaticFileHandler customStaticFileHandler) {
            nettyRestServer.setStaticFileHandler(customStaticFileHandler);
            return this;
        }

        public NettyRestServerBuilder setWebsocketHandler(String websocketPath, ChannelHandler websocketHandler) {
            nettyRestServer.setWebsocketPath(websocketPath);
            nettyRestServer.setWebsocketHandler(websocketHandler);
            return this;
        }

        public NettyRestServerBuilder setInterceptorBuilder(InterceptorBuilder interceptorBuilder) {
            nettyRestServer.setInterceptorBuilder(interceptorBuilder);
            return this;
        }

        public NettyRestServerBuilder setRestfulPreProxy(String... proxy) {
            List<String> proxyTemp = new ArrayList<>(Arrays.asList(proxy));
            nettyRestServer.config.setRestfulPreProxy(proxyTemp);
            return this;
        }

        public NettyRestServerBuilder setTemplateList(Template... templateList) {
            List<Template> proxyTemp = new ArrayList<>(Arrays.asList(templateList));
            nettyRestServer.setTemplateList(proxyTemp);
            return this;
        }

        public NettyRestServerBuilder setEnableUpload(Boolean enableUpload) {
            nettyRestServer.setEnableUpload(enableUpload);
            return this;
        }

        public NettyRestServer build() {
            return nettyRestServer;
        }
    }

    public List<Template> getTemplateList() {
        return templateList;
    }

    public void setTemplateList(List<Template> templateList) {
        this.templateList = templateList;
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public Integer getBossThreadCount() {
        return bossThreadCount;
    }

    public void setBossThreadCount(Integer bossThreadCount) {
        this.bossThreadCount = bossThreadCount;
    }

    public Integer getWorkThreadCount() {
        return workThreadCount;
    }

    public void setWorkThreadCount(Integer workThreadCount) {
        this.workThreadCount = workThreadCount;
    }

    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public RestCallback getRestCallback() {
        return restCallback;
    }

    public void setRestCallback(RestCallback restCallback) {
        this.restCallback = restCallback;
    }

    public CustomStaticFileHandler getStaticFileHandler() {
        return customStaticFileHandler;
    }

    public void setStaticFileHandler(CustomStaticFileHandler customStaticFileHandler) {
        this.customStaticFileHandler = customStaticFileHandler;
    }

    public String getWebsocketPath() {
        return websocketPath;
    }

    public void setWebsocketPath(String websocketPath) {
        this.websocketPath = websocketPath;
    }

    public ChannelHandler getWebsocketHandler() {
        return websocketHandler;
    }

    public void setWebsocketHandler(ChannelHandler websocketHandler) {
        this.websocketHandler = websocketHandler;
    }

    public InterceptorBuilder getInterceptorBuilder() {
        return interceptorBuilder;
    }

    public void setInterceptorBuilder(InterceptorBuilder interceptorBuilder) {
        this.interceptorBuilder = interceptorBuilder;
    }

    public Boolean getEnableUpload() {
        return enableUpload;
    }

    public void setEnableUpload(Boolean enableUpload) {
        this.enableUpload = enableUpload;
    }
}
