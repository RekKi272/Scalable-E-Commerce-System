FROM eclipse-temurin:17-jdk

ARG FILE_JAR=target/*.jar

ADD ${FILE_JAR} cart-service.jar

ENTRYPOINT ["java", "-jar", "/cart-service.jar"]

EXPOSE 8083
