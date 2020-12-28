#!/bin/sh
set -e
mvn package
mvn install:install-file -Dfile=netty-restful-core/target/netty-restful-core-2.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-core -Dversion=2.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-codec-gson/target/netty-restful-codec-gson-2.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-codec-gson -Dversion=2.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-codec-fastjson/target/netty-restful-codec-fastjson-2.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-codec-fastjson -Dversion=2.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-server/target/netty-restful-server-2.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-server -Dversion=2.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-client/target/netty-restful-client-2.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-client -Dversion=2.0-SNAPSHOT -Dpackaging=jar
echo "install success!"