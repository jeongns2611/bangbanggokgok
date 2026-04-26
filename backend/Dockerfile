FROM gradle:8.7-jdk17 AS builder
WORKDIR /build
COPY . .
RUN chmod +x ./gradlew && ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/build/libs/*.jar app.jar
EXPOSE 18081
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=18081"]