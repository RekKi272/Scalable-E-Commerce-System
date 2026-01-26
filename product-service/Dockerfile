FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} product-service.jar

ENTRYPOINT ["java", "-jar", "/product-service.jar"]

EXPOSE 8081
