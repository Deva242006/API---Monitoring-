# Multi-stage build for Sentinel Backend
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY pom.xml .
# Pre-download dependencies
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add unprivileged user for security
RUN addgroup -S sentinel && adduser -S sentinel -G sentinel

COPY --from=builder /app/target/sentinel-api-monitoring-1.0.0.jar app.jar

RUN chown -R sentinel:sentinel /app
USER sentinel

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/v1/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

