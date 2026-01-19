# ---- Build Stage ----
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

# Copy Gradle files and source code
COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle
COPY src /app/src

# Build the JAR
RUN ./gradlew build --no-daemon

# ---- Run Stage ----
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
