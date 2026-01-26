FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} order-service.jar

ENTRYPOINT ["java", "-jar", "/order-service.jar"]

EXPOSE 8082
