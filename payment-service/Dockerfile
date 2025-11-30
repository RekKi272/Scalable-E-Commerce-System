FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} payment-service.jar

ENTRYPOINT ["java", "-jar", "/payment-service.jar"]

EXPOSE 8089
