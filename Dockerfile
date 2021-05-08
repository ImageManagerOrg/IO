FROM gradle:6.8.3-jdk15 AS build
COPY --chown=gradle:gradle . /workspace
WORKDIR /workspace
RUN gradle build --no-daemon 

FROM openjdk:15
EXPOSE 8080 1098 1097
RUN mkdir /app
COPY --from=build /workspace/build/libs/*.jar /app/spring-boot-application.jar
ENTRYPOINT java -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.port=1098 -Dcom.sun.management.jmxremote.rmi.port=1097 -Djava.rmi.server.hostname=0.0.0.0 -jar $JAVA_ARG /app/spring-boot-application.jar
