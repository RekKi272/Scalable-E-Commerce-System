FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} api-gateway.jar

ENTRYPOINT ["java", "-jar", "/api-gateway.jar"]

EXPOSE 8080
