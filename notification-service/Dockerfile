FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} notification-service.jar

ENTRYPOINT ["java", "-jar", "/notification-service.jar"]

EXPOSE 8092
