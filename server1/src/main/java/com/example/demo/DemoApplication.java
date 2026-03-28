package com.example.demo;

import com.example.demo.entity.Point;
import com.example.demo.entity.RecycleAnalysisResult;
import com.example.demo.entity.User;
import com.example.demo.repository.PointRepository;
import com.example.demo.repository.RecycleAnalysisResultRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;

import java.time.ZonedDateTime;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        // ✅ Render의 자동 포트 할당을 무시하고, 8080으로 강제 설정
        System.setProperty("server.port", "8080");

        // Spring Boot 애플리케이션 실행
        ApplicationContext context = SpringApplication.run(DemoApplication.class, args);

        // 환경 변수에서 PORT 값 가져오기
        Environment env = context.getEnvironment();
        String port = env.getProperty("PORT"); // Render 환경 변수에서 PORT 가져오기
        String serverPort = env.getProperty("server.port"); // Spring 설정에서 server.port 가져오기

        // 현재 설정된 포트 출력
        System.out.println("✅ 현재 설정된 환경 변수 PORT: " + port);
        System.out.println("✅ 현재 설정된 server.port: " + serverPort);
    }

    // ✅ 테스트용 분석 결과 + 포인트 자동 삽입
    @Bean
    public CommandLineRunner initTestData(RecycleAnalysisResultRepository resultRepo,
                                          PointRepository pointRepo,
                                          UserRepository userRepo) {
        return args -> {
            // 분석 결과 삽입
            Long testAnalysisId = 456L;
            if (resultRepo.findByAnalysisId(testAnalysisId).isEmpty()) {
                RecycleAnalysisResult result = new RecycleAnalysisResult();
                result.setAnalysisId(testAnalysisId);
                result.setCategory("플라스틱");
                result.setConfidence(0.95);
                result.setDisposalMethod("플라스틱 전용 수거함에 버려주세요.");
                result.setCreatedAt(ZonedDateTime.now());

                resultRepo.save(result);
                System.out.println("✅ 테스트용 분석 결과 저장 완료 (analysis_id = 456)");
            }

            // 포인트 데이터 삽입
            Long testUserId = 123L;
            if (pointRepo.findByUserId(testUserId).isEmpty()) {
                // 테스트 사용자 조회
                User user = userRepo.findById(testUserId).orElse(null);
                if (user != null) {
                    Point point = new Point();
                    point.setUser(user); // ✅ 변경된 필드
                    point.setPoints(150);
                    point.setUpdatedAt(ZonedDateTime.now());

                    pointRepo.save(point);
                    System.out.println("✅ 테스트용 포인트 데이터 저장 완료 (user_id = 123)");
                } else {
                    System.out.println("⚠️ user_id=123 사용자가 존재하지 않아 포인트 생성 건너뜀");
                }
            }
        };
    }
}
