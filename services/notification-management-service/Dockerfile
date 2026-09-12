FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,id=oefms-maven-repository,target=/root/.m2,sharing=locked mvn -B -ntp -pl services/notification-management-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/notification-management-service/target/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
