FROM eclipse-temurin:17-jdk

# Build jar name
ARG FILE_JAR=target/*.jar

# Add jar file
ADD ${FILE_JAR} file-service.jar

# Set entrypoint
ENTRYPOINT ["java", "-jar", "/file-service.jar"]

# Expose service port
EXPOSE 8090
