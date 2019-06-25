FROM openjdk:8-alpine
WORKDIR /usr/local

ARG SCALA_VERSION=2.12.6
ARG SBT_VERSION=0.13.16
ARG PLAY_VERSION=2.6.15

ENV SCALA_VERSION ${SCALA_VERSION}
ENV SBT_VERSION ${SBT_VERSION}
ENV PLAY_VERSION ${PLAY_VERSION}

RUN apk update
RUN apk upgrade
RUN apk add bash
RUN apk add --update \
    curl \
    && rm -rf /var/cache/apk/*

RUN set -x \
    && curl -fsL https://piccolo.link/sbt-${SBT_VERSION}.tgz | tar xfz - \
    && ls -al

RUN set -x \
    && curl -fsL http://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz | tar xfz - \
    && mv scala-${SCALA_VERSION} scala \
    && ls -al

ENV PATH /usr/local/sbt/bin:/usr/local/scala/bin:$PATH

COPY sbtopts /etc/sbt
COPY system.properties /app/
COPY build.sbt /app/build.sbt
COPY project /app/project/
COPY start.sh /app/

WORKDIR /app

RUN sbt update

## If you copy the source after the update, then you can actually cache artifacts
COPY . /app/

RUN sbt compile stage
RUN chmod +x /app/start.sh
ENTRYPOINT ["/app/start.sh"]
