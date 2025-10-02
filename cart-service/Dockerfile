FROM openjdk:17

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} cart-service.jar

ENTRYPOINT ["java", "-jar", "/cart-service.jar"]

EXPOSE 8083
