package com.github.zhizuqiu.nettyrestfulserver;


import com.github.zhizuqiu.nettyrestfulserver.bean.ResourceValue;
import com.github.zhizuqiu.nettyrestfulserver.bean.RestMethodKey;
import com.github.zhizuqiu.nettyrestfulserver.bean.RestMethodValue;
import com.github.zhizuqiu.nettyrestfulserver.bean.TemplateMethodValue;
import com.github.zhizuqiu.nettyrestfulserver.handler.RestCallback;
import com.github.zhizuqiu.nettyrestfulserver.handler.StaticFileHandler;
import com.github.zhizuqiu.nettyrestfulserver.initializer.RestfulServerInitializer;
import com.github.zhizuqiu.nettyrestfulserver.interceptor.InterceptorBuilder;
import com.github.zhizuqiu.nettyrestfulserver.store.Config;
import com.github.zhizuqiu.nettyrestfulserver.store.MethodData;
import com.github.zhizuqiu.nettyrestfulserver.tools.MethodTool;
import com.github.zhizuqiu.nettyrestfulcommon.annotation.HttpHandler;
import com.github.zhizuqiu.nettyrestfulcommon.annotation.HttpMap;
import com.github.zhizuqiu.nettyrestfulcommon.annotation.TemplateMap;
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
    private Boolean ssl;
    private Integer port;
    private List<String> packages;
    private Integer bossThreadCount;
    private Integer workThreadCount;
    private static InternalLogger logger = InternalLoggerFactory.getInstance(NettyRestServer.class);
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private RestCallback restCallback;
    private StaticFileHandler staticFileHandler;
    private Config config = new Config();
    private String websocketPath;
    private Class<? extends ChannelHandler> websocketHandlerClass;
    private Class<? extends InterceptorBuilder> interceptorBuilderClass;
    private List<String> restfulPreProxy;

    public NettyRestServer() {
        ssl = false;
        port = 80;
        packages = new ArrayList<>();
        bossThreadCount = 2;
        workThreadCount = 4;
        restfulPreProxy = new ArrayList<>();
    }

    public void run() throws Exception {

        initConfig();
        if (config.getStaticFilePath() != null) {
            initResource();
        }
        initMethodData(packages);
        initInterceptor(interceptorBuilderClass);

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
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new RestfulServerInitializer(sslCtx, websocketPath, websocketHandlerClass, this.restfulPreProxy, this.staticFileHandler));

            Channel ch = b.bind(port).sync().channel();

            logger.info("listen to " + (ssl ? "https" : "http") + "://127.0.0.1:" + port + '/');
            logger.info("bossThreadCount:" + bossThreadCount);
            logger.info("workThreadCount:" + workThreadCount);

            if (restCallback != null) {
                restCallback.call(bossGroup, workerGroup);
            }

            ch.closeFuture().sync();
        } catch (Exception e) {
            logger.error(e.getMessage());
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

        logger.info("load resources:");
        for (RestMethodKey restMethodKey : MethodData.resourcesKeySet()) {
            logger.info(restMethodKey.getUrl());
        }
    }

    private void initMethodData(List<String> paths) {

        if (paths == null) {
            return;
        }

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
                logger.error("class " + httpHandlerClass.getName() + " no default constructor");
                continue;
            }

            // @HttpMap
            for (Method method : httpHandlerClass.getMethods()) {
                String methodName = method.getName();
                HttpMap todoAnnotation = method.getAnnotation(HttpMap.class);

                if (todoAnnotation != null) {

                    // 检查使用HttpMap注解的方法的参数是否合法
                    Class[] paramTypeClasses = method.getParameterTypes();
                    if (paramTypeClasses.length == 1) {
                        if (!paramTypeClasses[0].getName().contains("HttpResponse")) {
                            logger.error("illegal method [" + methodName + "]:" + "must be HttpResponse, when param size=1");
                        }
                    } else if (paramTypeClasses.length == 2) {
                        Class param1 = paramTypeClasses[0];
                        Class param2 = paramTypeClasses[1];
                        if (todoAnnotation.paramType() == HttpMap.ParamType.FORM_DATA || todoAnnotation.paramType() == HttpMap.ParamType.URL_DATA) {
                            if (!param1.getName().contains("Map")) {
                                logger.error("illegal method [" + methodName + "]:" + "param[0] must be Map, when param size=2");
                            }
                        } else {
                            if (!param1.getName().contains("String")) {
                                logger.error("illegal method [" + methodName + "]:" + "param[0] must be String, when param size=2");
                            }
                        }
                        if (!param2.getName().contains("HttpResponse")) {
                            logger.error("illegal method [" + methodName + "]:" + "param[1] must be HttpResponse, when param size=2");
                        }
                    } else if (paramTypeClasses.length == 3) {
                        Class param1 = paramTypeClasses[0];
                        Class param2 = paramTypeClasses[1];
                        Class param3 = paramTypeClasses[2];
                        if (todoAnnotation.paramType() == HttpMap.ParamType.FORM_DATA || todoAnnotation.paramType() == HttpMap.ParamType.URL_DATA) {
                            if (!param1.getName().contains("Map")) {
                                logger.error("illegal method [" + methodName + "]:" + "param[0] must be Map, when param size=3");
                            }
                        } else {
                            if (!param1.getName().contains("String")) {
                                logger.error("illegal method [" + methodName + "]:" + "param[0] must be String, when param size=3");
                            }
                        }
                        if (!param2.getName().contains("HttpRequest")) {
                            logger.error("illegal method [" + methodName + "]:" + "param[1] must be HttpRequest, when param size=3");
                        }
                        if (!param3.getName().contains("HttpResponse")) {
                            logger.error("illegal method [" + methodName + "]:" + "param[2] must be HttpResponse, when param size=3");
                        }
                    } else if (paramTypeClasses.length > 3) {
                        logger.error("illegal method [" + methodName + "]:" + "param size>2");
                    }

                    RestMethodValue restMethodValue = new RestMethodValue();
                    restMethodValue.setHttpMap(todoAnnotation);
                    restMethodValue.setMethod(method);
                    restMethodValue.setInstance(instance);

                    String path = todoAnnotation.path();
                    HttpMap.Method methodType = todoAnnotation.method();
                    HttpMap.ParamType paramType = todoAnnotation.paramType();
                    RestMethodKey restMethodKey = new RestMethodKey(path, methodType, paramType);

                    MethodData.putRestMethod(restMethodKey, restMethodValue);
                    logger.info("load methon:[" + methodType + "][" + path + "][" + paramType + "]");
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
                    logger.info("load methon:[TemplateMap][" + path + "]");
                }
            }
        }
    }

    private void initConfig() {
        MethodData.setConfig(config);
    }

    /**
     * 初始化拦截器
     */
    private void initInterceptor(Class<? extends InterceptorBuilder> interceptorBuilderClass) {
        if (interceptorBuilderClass == null) {
            return;
        }
        InterceptorBuilder builder = null;
        try {
            builder = interceptorBuilderClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (builder != null) {
            MethodData.setInterceptorList(builder.build());
        }
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

        public NettyRestServerBuilder setStaticFileHandler(StaticFileHandler staticFileHandler) {
            nettyRestServer.setStaticFileHandler(staticFileHandler);
            return this;
        }

        public NettyRestServerBuilder setWebsocketHandler(String websocketPath, Class<? extends ChannelHandler> websocketHandlerClass) {
            nettyRestServer.setWebsocketPath(websocketPath);
            nettyRestServer.setWebsocketHandlerClass(websocketHandlerClass);
            return this;
        }

        public NettyRestServerBuilder setInterceptorBuilder(Class<? extends InterceptorBuilder> interceptorBuilderClass) {
            nettyRestServer.setInterceptorBuilderClass(interceptorBuilderClass);
            return this;
        }

        public NettyRestServerBuilder setRestfulPreProxy(String... proxy) {
            List<String> proxyTemp = new ArrayList<>(Arrays.asList(proxy));
            nettyRestServer.setRestfulPreProxy(proxyTemp);
            return this;
        }

        public NettyRestServer build() {
            return nettyRestServer;
        }
    }

    public Boolean getSsl() {
        return ssl;
    }

    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
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

    public List<String> getRestfulPreProxy() {
        return restfulPreProxy;
    }

    public void setRestfulPreProxy(List<String> restfulPreProxy) {
        this.restfulPreProxy = restfulPreProxy;
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

    public StaticFileHandler getStaticFileHandler() {
        return staticFileHandler;
    }

    public void setStaticFileHandler(StaticFileHandler staticFileHandler) {
        this.staticFileHandler = staticFileHandler;
    }

    public String getWebsocketPath() {
        return websocketPath;
    }

    public void setWebsocketPath(String websocketPath) {
        this.websocketPath = websocketPath;
    }

    public Class<? extends ChannelHandler> getWebsocketHandlerClass() {
        return websocketHandlerClass;
    }

    public void setWebsocketHandlerClass(Class<? extends ChannelHandler> websocketHandlerClass) {
        this.websocketHandlerClass = websocketHandlerClass;
    }

    public Class<? extends InterceptorBuilder> getInterceptorBuilderClass() {
        return interceptorBuilderClass;
    }

    public void setInterceptorBuilderClass(Class<? extends InterceptorBuilder> interceptorBuilderClass) {
        this.interceptorBuilderClass = interceptorBuilderClass;
    }
}
