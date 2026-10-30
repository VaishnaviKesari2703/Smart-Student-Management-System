# Runtime image for the Smart Student Management System.
# Build the JAR first:  mvn clean package   (the Jenkins pipeline does this automatically)
FROM eclipse-temurin:17-jre

LABEL org.opencontainers.image.title="smart-student-management" \
      org.opencontainers.image.version="1.0.0"

RUN groupadd --system sms && useradd --system --gid sms --home /app sms
WORKDIR /app

COPY target/smart-student-management-1.0.0.jar app.jar

# JSON data directory - mounted as a persistent volume by docker-compose
RUN mkdir -p /app/data && chown -R sms:sms /app
VOLUME ["/app/data"]

ENV APP_DATA_DIR=/app/data \
    PORT=8080 \
    JAVA_OPTS="-Xmx256m"

USER sms
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
