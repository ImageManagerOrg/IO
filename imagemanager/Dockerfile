FROM gradle:6.8.3-jdk15 AS build
COPY --chown=gradle:gradle . /workspace
WORKDIR /workspace
RUN gradle build --no-daemon 

FROM openjdk:15
EXPOSE 8080 1098
RUN mkdir /app
COPY --from=build /workspace/build/libs/*.jar /app/spring-boot-application.jar
ENTRYPOINT java -Xms250m -Xmx1g -jar $JAVA_ARG /app/spring-boot-application.jar
