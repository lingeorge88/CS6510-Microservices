# Runtime image for the Checkout Service (REST only; talks to other services via Kafka).
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
