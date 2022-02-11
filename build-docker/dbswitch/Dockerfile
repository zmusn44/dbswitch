FROM inrgihc/maven-aliyun:3.6.3-jdk-8

ADD dbswitch-release.tar.gz  /

EXPOSE      9088

USER        root

WORKDIR /dbswitch-release

CMD  [ "/dbswitch-release/bin/startup.sh"]

