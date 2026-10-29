# ─────────────────────────────────────────────────────────────────────────────
# Dockerfile – Smart Student Management System
#
# Build sequence (MUST run `mvn package` first):
#   mvn clean package
#   docker build -t smart-student-management:1.0.0 .
#   docker run -it --rm smart-student-management:1.0.0
#
# CONCEPT:
#   Image  = read-only template (blueprint) baked from this file.
#   Container = running instance of that image (ephemeral by default).
# ─────────────────────────────────────────────────────────────────────────────

# Use the official Eclipse Temurin Java 17 JRE (slim variant for smaller image)
FROM eclipse-temurin:17-jre-jammy

# Metadata labels
LABEL maintainer="vaishnavi"
LABEL version="1.0.0"
LABEL description="Smart Student Management System – console application"

# Create a non-root user for security best practice
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory inside the container
WORKDIR /app

# Copy the packaged JAR from the Maven target directory into the image.
# Make sure you have run `mvn clean package` before building this image.
COPY target/smart-student-management-1.0.0.jar app.jar

# Change ownership to non-root user
RUN chown appuser:appuser /app/app.jar

# Switch to non-root user
USER appuser

# Run the application
# -it flag is required at `docker run` time for interactive console input
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
