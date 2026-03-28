# OpenJDK 17 사용More actions
FROM openjdk:17-jdk-slim AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 파일 복사
COPY . .

# Maven Wrapper(mvnw) 실행 권한 추가
RUN chmod +x mvnw

# Maven을 사용하여 프로젝트 빌드
RUN ./mvnw clean package -DskipTests

# 최종 실행 환경 설정
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# 컨테이너에서 실행할 포트 노출
EXPOSE 8080

# 실행 명령어
CMD ["java", "-jar", "app.jar"]