# 1. Build Spring Boot
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Spring Boot 소스만 복사
COPY springboot/ springboot/
WORKDIR /app/springboot

# Maven 빌드
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# 2. 최종 실행 환경
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Spring Boot 빌드 JAR 복사
COPY --from=builder /app/springboot/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Python 환경 설치
RUN apt-get update && apt-get install -y python3 python3-pip

# Flask 코드 복사
COPY server/ server/
WORKDIR /app/server/scripts

# Flask 의존성 설치
RUN pip3 install flask

# 포트 노출
EXPOSE 8080 5000

# 두 서버 동시에 실행 (개발용)
CMD java -jar /app/app.jar & python3 recycle.py