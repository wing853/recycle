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
        ApplicationContext context = SpringApplication.run(DemoApplication.class, args);

        Environment env = context.getEnvironment();
        String port = env.getProperty("PORT");
        String serverPort = env.getProperty("server.port");

        System.out.println("✅ Render PORT: " + port);
        System.out.println("✅ server.port: " + serverPort);
    }

    @Bean
    public CommandLineRunner initTestData(RecycleAnalysisResultRepository resultRepo,
                                          PointRepository pointRepo,
                                          UserRepository userRepo) {
        return args -> {
            Long testAnalysisId = 456L;
            if (resultRepo.findByAnalysisId(testAnalysisId).isEmpty()) {
                RecycleAnalysisResult result = new RecycleAnalysisResult();
                result.setAnalysisId(testAnalysisId);
                result.setCategory("플라스틱");
                result.setConfidence(0.95);
                result.setDisposalMethod("플라스틱 전용 수거함에 버려주세요.");
                result.setCreatedAt(ZonedDateTime.now());

                resultRepo.save(result);
                System.out.println("✅ 테스트 분석 결과 저장 완료");
            }

            Long testUserId = 123L;
            if (pointRepo.findByUserId(testUserId).isEmpty()) {
                User user = userRepo.findById(testUserId).orElse(null);
                if (user != null) {
                    Point point = new Point();
                    point.setUser(user);
                    point.setPoints(150);
                    point.setUpdatedAt(ZonedDateTime.now());

                    pointRepo.save(point);
                    System.out.println("✅ 테스트 포인트 저장 완료");
                } else {
                    System.out.println("⚠️ user_id=123 없음");
                }
            }
        };
    }
}