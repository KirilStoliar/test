# ============================================================
# Build stage
# ============================================================

FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# Gradle wrapper
COPY gradlew .
COPY gradle ./gradle

# Gradle configuration
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY gradle.properties* ./

# Build logic
COPY build-logic ./build-logic

# Project modules
COPY common ./common
COPY services ./services

# Make Gradle wrapper executable
RUN chmod +x ./gradlew

ARG SERVICE_NAME

RUN if [ -z "$SERVICE_NAME" ]; then \
        echo "SERVICE_NAME build argument is required"; \
        exit 1; \
    fi

RUN ./gradlew ":services:${SERVICE_NAME}:bootJar" --no-daemon

# ============================================================
# Runtime stage
# ============================================================

FROM eclipse-temurin:21-jre

ARG SERVICE_NAME

ENV SERVICE_NAME=${SERVICE_NAME}

WORKDIR /app

# Create non-root user
RUN useradd \
        --system \
        --uid 10001 \
        --create-home \
        --shell /usr/sbin/nologin \
        spring

COPY --from=builder \
    /workspace/services/${SERVICE_NAME}/build/libs/${SERVICE_NAME}.jar \
    /app/app.jar

RUN chown -R spring:spring /app

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-jar", "/app/app.jar"]