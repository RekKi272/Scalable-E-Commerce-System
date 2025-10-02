FROM openjdk:17

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} service-registry.jar

ENTRYPOINT ["java", "-jar", "/service-registry.jar"]

EXPOSE 8761
