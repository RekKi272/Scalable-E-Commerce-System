FROM openjdk:17

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} user-service.jar

ENTRYPOINT ["java", "-jar", "/user-service.jar"]

EXPOSE 8080
