# netty-RESTful

基于netty实现的RESTful服务端与客户端

server
---

[server modules](netty-restful-server)

- RESTful 使用注解方式组织URL路由
- WebSocket
- 静态文件服务器(The static file servers)
- 支持拦截器
- 支持restful proxy
- 支持静态文件自定义处理
- 支持更改JSON序列化工具
  - 目前提供 [GSON](netty-restful-codec-gson)、[Fastjson](netty-restful-codec-fastjson) 的简单实现
  - 通过实现 [Decoder](netty-restful-core/src/main/java/com/github/zhizuqiu/nettyrestful/core/codec/Decoder.java)、[Encoder](netty-restful-core/src/main/java/com/github/zhizuqiu/nettyrestful/core/codec/Encoder.java) 接口，替换为其他JSON序列化工具
- 支持更改模板引擎
  - 目前提供 [Mustache](netty-restful-template-mustache)、[Thymeleaf](netty-restful-template-thymeleaf) 的简单实现
  - 通过实现 [Template](netty-restful-core/src/main/java/com/github/zhizuqiu/nettyrestful/core/template/Template.java) 接口，替换为其他模板引擎
- 支持文件上传

client
---

[client modules](netty-restful-client)

- RESTful 使用注解方式组织URL路由
- JSON 自定义处理 

core
---

[core modules](netty-restful-core)

- Annotation
- Exception
- Util
- Codec Interface
- Template Interface

gson
---

[gson modules](netty-restful-codec-gson)

- Gson Codec Implement

fastjson
---

[fastjson modules](netty-restful-codec-fastjson)

- Fastjson Codec Implement

mustache
---

[mustache modules](netty-restful-template-mustache)

- Mustache Template Implement

thymeleaf
---

[thymeleaf modules](netty-restful-template-thymeleaf)

- Thymeleaf Template Implement

安装
---

```
./install.sh
```

JetBrains OS licenses
---
`netty-RESTful` had been developed with Java under the **free JetBrains Open Source license(s)** granted by JetBrains s.r.o., hence I would like to express my thanks here.
![jetbrains logo](jetbrains.png)
