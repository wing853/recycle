package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Entity
@Table(name = "point_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자와의 연관관계 (userId 대신) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private ZonedDateTime date;

    @Column(nullable = false)
    private String type; // "적립" 또는 "사용"

    @Column(nullable = false)
    private String reason; // 예: "AI 분석 리워드"

    private String brandName;        // 사용 시 브랜드명
    private String wasteTypeKorean;  // 적립 시 폐기물 종류

    @Column(nullable = false)
    private int points; // 증감량

    @Column(nullable = false)
    private int balance; // 변화 후 총 포인트
}
