FROM openjdk:17

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} api-gateway.jar

ENTRYPOINT ["java", "-jar", "/api-gateway.jar"]

EXPOSE 8080
