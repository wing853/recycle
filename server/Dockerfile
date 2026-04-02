# OpenJDK 17 사용More actions
FROM eclipse-temurin:17-jdk AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 프로젝트 파일 복사
COPY . .

# Maven Wrapper(mvnw) 실행 권한 추가
RUN chmod +x mvnw

# Maven을 사용하여 프로젝트 빌드
RUN ./mvnw clean package -DskipTests

# 최종 실행 환경 설정
FROM eclipse-temurin:17-jdk
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Python 설치
RUN apt-get update && apt-get install -y python3 python3-pip

# Flask 코드 복사
COPY server/ server/
WORKDIR /app/server/scripts

# Flask 의존성 설치
RUN pip3 install flask

# 컨테이너에서 실행할 포트 노출
EXPOSE 8080 5000

# 실행 명령어
CMD ["sh", "-c", "java -jar /app/app.jar & python3 /app/server/scripts/recycle.py"]