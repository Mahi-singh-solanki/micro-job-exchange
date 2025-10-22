# Stage 1: Build the runnable JAR
FROM maven:3.9.4-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Copy the entire project directory
COPY . .
# Run a full Maven clean install to create the final JAR
RUN mvn clean package -DskipTests

# Stage 2: Create the final, lightweight runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the compiled JAR from the build stage
COPY --from=build /app/target/*.jar app.jar
# Expose the port (Render handles mapping this to 80/443)
EXPOSE 8080
# Define the entry point to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]