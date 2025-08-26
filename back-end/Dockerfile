FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY src/main/resources/gcs-key.json /app/gcs-key.json
COPY target/topfoodnow-0.0.1-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]