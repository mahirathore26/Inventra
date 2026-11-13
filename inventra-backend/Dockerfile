# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Copy Maven POM and pre-fetch dependencies for efficient layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Minimal Production Runtime
# ==========================================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Create a non-root system group and user for security compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built executable fat JAR from the builder stage
COPY --from=builder /build/target/inventra-backend-0.0.1-SNAPSHOT.jar app.jar

# Adjust ownership to the non-root user
RUN chown -R appuser:appgroup /app

# Switch to the non-root user
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
