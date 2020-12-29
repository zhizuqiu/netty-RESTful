#!/bin/sh
set -e
mvn clean package
mvn install:install-file -Dfile=netty-restful-core/target/netty-restful-core-3.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-core -Dversion=3.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-codec-gson/target/netty-restful-codec-gson-3.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-codec-gson -Dversion=3.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-codec-fastjson/target/netty-restful-codec-fastjson-3.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-codec-fastjson -Dversion=3.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-template-mustache/target/netty-restful-template-mustache-3.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-template-mustache -Dversion=3.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-template-thymeleaf/target/netty-restful-template-thymeleaf-3.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-template-thymeleaf -Dversion=3.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-server/target/netty-restful-server-3.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-server -Dversion=3.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-client/target/netty-restful-client-3.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-client -Dversion=3.0-SNAPSHOT -Dpackaging=jar
echo "install success!"