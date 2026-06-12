# ==========================================
# 1단계: 빌드 스테이지 (Build Stage) - 호환성 높은 기본 JDK 사용
# ==========================================
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# 프로젝트 전체 파일을 컨테이너 내부로 복사
COPY . .

# gradlew 파일에 실행 권한 부여
RUN chmod +x gradlew

# Gradle을 사용하여 실행 가능한 JAR 파일만 빌드
RUN ./gradlew bootJar

# ==========================================
# 2단계: 실행 스테이지 (Run Stage) - 안정적인 런타임 JRE 사용
# ==========================================
FROM eclipse-temurin:21-jre
WORKDIR /app

# 1단계 빌드 스테이지에서 생성된 싱글 실행 JAR 파일만 추출하여 복사
# (plain.jar 등이 같이 복사되어 꼬이는 것을 방지하기 위해 구체적인 이름 지정 권장)
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar || COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너가 외부와 통신할 포트 지정
EXPOSE 8080

# 컨테이너 실행 시 스프링 부트 애플리케이션 구동
ENTRYPOINT ["java", "-jar", "app.jar"]