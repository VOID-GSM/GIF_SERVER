# ==========================================
# 1단계: 빌드 스테이지 (Build Stage)
# ==========================================
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# 프로젝트 전체 파일을 컨테이너 내부로 복사
COPY . .

# 실행 권한 부여 단계 추가 ⭐️
RUN chmod +x gradlew

# Gradle을 사용하여 실행 가능한 JAR 파일만 빌드
RUN ./gradlew bootJar

# ==========================================
# 2단계: 실행 스테이지 (Run Stage)
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]