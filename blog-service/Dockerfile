FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} blog-service.jar

ENTRYPOINT ["java", "-jar", "/blog-service.jar"]

EXPOSE 8084
