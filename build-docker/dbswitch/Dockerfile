FROM openjdk:8-jre-alpine

ADD dbswitch-release.tar.gz  /

EXPOSE      9088

USER        root

WORKDIR /dbswitch-release

CMD  ["/dbswitch-release/bin/startup.sh"]

