# ==========================================
# 1단계: 빌드 스테이지 (Build Stage)
# ==========================================
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY . .
RUN chmod +x gradlew

# 이전 빌드 캐시가 꼬이는 것을 방지하기 위해 clean 후 bootJar 실행
RUN ./gradlew clean bootJar

# ==========================================
# 2단계: 실행 스테이지 (Run Stage)
# ==========================================
FROM eclipse-temurin:21-jdk
WORKDIR /app

# 빌드된 메인 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]