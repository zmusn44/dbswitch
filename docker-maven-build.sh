#!/bin/sh

docker run -it --rm \
	--name my-maven-project \
	-v ~/.m2:/root/.m2 \
	-v "$PWD":/usr/src/mymaven \
	-w /usr/src/mymaven \
 inrgihc/aliyun-maven:3.5.0-jdk-8-alpine mvn clean package

