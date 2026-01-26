FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} auth-service.jar

ENTRYPOINT ["java", "-jar", "/auth-service.jar"]

EXPOSE 8090
