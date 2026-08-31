# -------------------------------------------------------------
# 1. Build Stage: 소스 코드를 빌드하여 실행 파일(JAR) 생성
# -------------------------------------------------------------
FROM eclipse-temurin:25-jdk-noble AS builder
WORKDIR /app
COPY . .
# gradlew 실행 권한 부여 (Permission denied 방지)
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# -------------------------------------------------------------
# 2. Run Stage: 경량화된 JRE 환경에서 실행만 담당
# -------------------------------------------------------------
FROM eclipse-temurin:25-jre-noble
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]