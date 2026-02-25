FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

COPY ${FILE_JAR} /user-service.jar

ENTRYPOINT ["java", "-jar", "/user-service.jar"]

EXPOSE 8085
