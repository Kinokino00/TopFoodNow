FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/topfoodnow-0.0.1-SNAPSHOT.jar /app/app.jar
# 將 gcs-key.json 檔案複製到容器
COPY ./src/main/resources/gcs-key.json /app/gcs-key.json
ENTRYPOINT ["java", "-jar", "app.jar"]