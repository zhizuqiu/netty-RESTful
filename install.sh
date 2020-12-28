#!/bin/sh
set -e
mvn package
mvn install:install-file -Dfile=netty-restful-common/target/netty-restful-common-2.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-common -Dversion=2.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-gson/target/netty-restful-gson-2.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-gson -Dversion=2.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-fastjson/target/netty-restful-fastjson-2.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-fastjson -Dversion=2.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-server/target/netty-restful-server-2.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-server -Dversion=2.0-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=netty-restful-client/target/netty-restful-client-2.0-SNAPSHOT.jar -DgroupId=com.github.zhizuqiu -DartifactId=netty-restful-client -Dversion=2.0-SNAPSHOT -Dpackaging=jar
echo "success"