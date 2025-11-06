FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} store-service.jar

ENTRYPOINT ["java", "-jar", "/store-service.jar"]

EXPOSE 8087
