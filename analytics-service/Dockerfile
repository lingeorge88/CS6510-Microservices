# Runtime image for the Analytics Service.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*-SNAPSHOT.jar app.jar
EXPOSE 8084 9094
ENTRYPOINT ["java", "-jar", "app.jar"]
