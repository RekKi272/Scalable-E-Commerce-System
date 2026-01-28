FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} service-registry.jar

ENTRYPOINT ["java", "-jar", "/service-registry.jar"]

EXPOSE 8761
