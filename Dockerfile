FROM maven:3.8.8-eclipse-temurin-17-alpine as build

WORKDIR /app

COPY pom.xml pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn verify clean --fail-never

COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn package

FROM gcr.io/distroless/java17-debian12

WORKDIR /app

COPY --from=build /app/target/bot-0.0.1-SNAPSHOT.jar app.jar

CMD ["app.jar"]
