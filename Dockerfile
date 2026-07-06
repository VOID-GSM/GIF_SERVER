# ==========================================
# 1단계: 빌드 스테이지 (Build Stage)
# ==========================================
FROM gradle:9.4-jdk21 AS builder
USER root
WORKDIR /app
COPY . .
RUN gradle clean bootJar -x jar --no-daemon

# ==========================================
# 2단계: 실행 스테이지 (Run Stage)
# ==========================================
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
