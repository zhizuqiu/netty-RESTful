#!/bin/sh
set -e
VERSION="3.0-SNAPSHOT"
mvn clean package
mvn install:install-file -Dfile=netty-restful-core/target/netty-restful-core-${VERSION}.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-core -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-codec-gson/target/netty-restful-codec-gson-${VERSION}.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-codec-gson -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-codec-fastjson/target/netty-restful-codec-fastjson-${VERSION}.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-codec-fastjson -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-template-mustache/target/netty-restful-template-mustache-${VERSION}.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-template-mustache -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-template-thymeleaf/target/netty-restful-template-thymeleaf-${VERSION}.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-template-thymeleaf -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-server/target/netty-restful-server-${VERSION}.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-server -Dversion=${VERSION} -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-client/target/netty-restful-client-${VERSION}.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-client -Dversion=${VERSION} -Dpackaging=jar
mkdir target
cp netty-restful-core/target/netty-restful-core-${VERSION}.jar target/
cp netty-restful-codec-gson/target/netty-restful-codec-gson-${VERSION}.jar target/
cp netty-restful-codec-fastjson/target/netty-restful-codec-fastjson-${VERSION}.jar target/
cp netty-restful-template-mustache/target/netty-restful-template-mustache-${VERSION}.jar target/
cp netty-restful-template-thymeleaf/target/netty-restful-template-thymeleaf-${VERSION}.jar target/
cp netty-restful-server/target/netty-restful-server-${VERSION}.jar target/
cp netty-restful-client/target/netty-restful-client-${VERSION}.jar target/
echo "install success!"