# Dockerfile for Gym CRM Main Microservice

FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /app/target/gym-crm-spring-1.0.0.jar app.jar

RUN chown -R spring:spring /app

USER spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Default to docker profile (can be overridden via environment variable)
ENTRYPOINT ["java", "-jar", "app.jar"]
