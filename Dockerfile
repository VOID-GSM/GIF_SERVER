# ==========================================
# 1단계: 빌드 스테이지 (Build Stage) - Java 21 반영
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# 프로젝트 전체 파일을 컨테이너 내부로 복사
COPY . .

# gradlew 파일에 실행 권한 부여
RUN chmod +x gradlew

# Gradle을 사용하여 실행 가능한 JAR 파일만 빌드
RUN ./gradlew bootJar

# ==========================================
# 2단계: 실행 스테이지 (Run Stage) - Java 21 반영
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 1단계 빌드 스테이지에서 생성된 싱글 실행 JAR 파일만 추출하여 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너가 외부와 통신할 포트 지정
EXPOSE 8080

# 컨테이너 실행 시 스프링 부트 애플리케이션 구동
ENTRYPOINT ["java", "-jar", "app.jar"]