FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl services/gdpr-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/gdpr-service/target/*.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
