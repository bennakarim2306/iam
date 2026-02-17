# dockerfile
FROM maven:3.9.5-eclipse-temurin-21 AS build
ARG APP_VERSION
LABEL app.version=$APP_VERSION
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy AS runtime
ARG JAR=/workspace/target/*.jar
RUN addgroup --system app && adduser --system --ingroup app app
COPY --from=build ${JAR} /app/app.jar
USER app
EXPOSE 8080
# ENTRYPOINT verwendet die Fly-Umgebungsvariable PORT (oder 8080 als Fallback)
ENTRYPOINT ["sh","-c","exec java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]