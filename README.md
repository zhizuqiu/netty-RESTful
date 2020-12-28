# netty-RESTful

基于netty实现的RESTful服务端与客户端

server
---

[server modules](netty-restful-server)

- RESTful 使用注解方式组织URL路由
- WebSocket
- 静态文件服务器(The static file servers)
- 支持Mustache模板引擎
- 支持拦截器
- 支持restful proxy
- 支持静态文件自定义处理

client
---

[client modules](netty-restful-client)

- RESTful 使用注解方式组织URL路由
- JSON 自定义处理 

core
---

[core modules](netty-restful-core)

- Annotation
- Codec
- Exception
- Util

gson
---

[gson modules](netty-restful-codec-gson)

- Gson Codec

fastjson
---

[fastjson modules](netty-restful-codec-fastjson)

- Fastjson Codec

安装
---

```
./install.sh
```