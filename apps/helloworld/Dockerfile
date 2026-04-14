# Stage 1: Build Stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml first to leverage Docker layer caching
COPY pom.xml .

# Download dependencies (cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime Stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user for security
# RUN addgroup -S spring && adduser -S spring -G spring

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to spring user
# RUN chown spring:spring app.jar

# Switch to non-root user
# USER spring

# Expose the application port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/hello || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
