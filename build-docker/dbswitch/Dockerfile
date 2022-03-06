FROM inrgihc/maven-aliyun:3.6.3-jdk-8

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ADD dbswitch-release.tar.gz  /

EXPOSE      9088

USER        root

WORKDIR /dbswitch-release

CMD  [ "/dbswitch-release/bin/startup.sh"]

