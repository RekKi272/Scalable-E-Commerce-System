FROM openjdk:17

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} product-service.jar

ENTRYPOINT ["java", "-jar", "/product-service.jar"]

EXPOSE 8081
