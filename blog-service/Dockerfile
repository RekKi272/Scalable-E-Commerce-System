FROM openjdk:17

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} blog-service.jar

ENTRYPOINT ["java", "-jar", "/blog-service.jar"]

EXPOSE 8084
