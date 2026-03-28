package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class RecycleLogResponse {
    private Long logId;               // 로그 식별자
    private Long analysisId;          // 분석 ID
    private String category;          // AI 분류 결과
    private String disposalCategory;  // 분리수거 분류
    private String disposalMethod;    // 배출 방법
    private ZonedDateTime createdAt;  // 생성 시간

    private long totalPoints;         // 누적 포인트
    private long recycleCount;        // 분리수거 누적 횟수
    private String message;           // 사용자 메시지
}