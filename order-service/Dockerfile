FROM openjdk:17

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} order-service.jar

ENTRYPOINT ["java", "-jar", "/order-service.jar"]

EXPOSE 8082
