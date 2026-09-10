# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
# Retry flags make dependency downloads resilient to transient network errors.
ENV MAVEN_OPTS="-Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.httpconnectionManager.ttlSeconds=60"
# Cache dependencies first
COPY pom.xml .
RUN mvn -B -q -Dmaven.wagon.http.retryHandler.count=5 dependency:go-offline
# Build
COPY src ./src
RUN mvn -B -q -Dmaven.wagon.http.retryHandler.count=5 clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# OCR is optional and disabled by default. To enable Tesseract OCR, uncomment:
# RUN apt-get update && apt-get install -y --no-install-recommends tesseract-ocr \
#     && rm -rf /var/lib/apt/lists/*

# Local storage directory (used when STORAGE_PROVIDER=local)
RUN mkdir -p /app/data/evidence-store
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
