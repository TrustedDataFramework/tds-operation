FROM frolvlad/alpine-oraclejdk8:slim
MAINTAINER LY
RUN echo "https://mirror.tuna.tsinghua.edu.cn/alpine/v3.4/main/" > /etc/apk/repositories
RUN apk update \
        && apk upgrade \
        && apk add --no-cache bash \
        bash-doc \
        bash-completion \
        && rm -rf /var/cache/apk/* \
        && /bin/bash
VOLUME /tmp
ADD build/monitor.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
EXPOSE 8090