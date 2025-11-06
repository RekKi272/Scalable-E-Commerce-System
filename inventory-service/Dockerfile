FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} inventory-service.jar

ENTRYPOINT ["java", "-jar", "/inventory-service.jar"]

EXPOSE 8088
