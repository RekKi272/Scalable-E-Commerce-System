FROM openjdk:17

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} store-service.jar

ENTRYPOINT ["java", "-jar", "/store-service.jar"]

EXPOSE 8087
