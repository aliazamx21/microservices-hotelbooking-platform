# Use a lightweight Java runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar file into the container
COPY target/*.jar app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]