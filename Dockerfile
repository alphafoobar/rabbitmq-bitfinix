FROM openjdk:jre-alpine

# Install dependencies
RUN apk update
# For the healthcheck
RUN apk add curl

# Allows debug configuration
ARG guest_java_opts
ARG spring_profiles=default,production

# Install app
RUN rm -rf /var/jars/*
ADD build/libs /var/java

EXPOSE 80

ENV JAVA_OPTS "${guest_java_opts} -Dspring.profiles.active=${spring_profiles}"

CMD ["/var/java/rabbitmq-bitfinex-0.0.1-SNAPSHOT.jar", "--server.port=8080"]

HEALTHCHECK \
  CMD curl --fail http://localhost/1/ping || exit 1
