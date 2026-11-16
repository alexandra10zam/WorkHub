# ---- build stage: compile and run unit tests are done in CI, here we only package
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- extract Spring Boot layers so dependency layers are cached between builds
FROM eclipse-temurin:21-jre-alpine AS layers
WORKDIR /layers
COPY --from=build /src/target/workhub.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ---- runtime: small JRE image, non-root user
FROM eclipse-temurin:21-jre-alpine
# Numeric UID so Kubernetes can enforce runAsNonRoot
RUN addgroup -S -g 10001 workhub && adduser -S -u 10001 -G workhub workhub
USER 10001
WORKDIR /app
COPY --from=layers /layers/dependencies/ ./
COPY --from=layers /layers/spring-boot-loader/ ./
COPY --from=layers /layers/snapshot-dependencies/ ./
COPY --from=layers /layers/application/ ./
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s \
  CMD wget -qO- http://localhost:8080/actuator/health/readiness || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
