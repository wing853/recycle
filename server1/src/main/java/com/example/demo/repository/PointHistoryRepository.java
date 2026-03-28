package com.example.demo.repository;

import com.example.demo.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    // ✅ 일반 리스트용
    List<PointHistory> findByUserIdOrderByDateDesc(Long userId);

    // ✅ 페이징 지원용
    Page<PointHistory> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);

    // ✅ 오늘 지급된 포인트 총합
    @Query("SELECT SUM(p.points) FROM PointHistory p WHERE p.user.id = :userId AND DATE(p.date) = CURRENT_DATE")
    Long getTodayTotalEarnedPoints(@Param("userId") Long userId);

    // ✅ 오늘 AI 분석 리워드 횟수
    @Query("SELECT COUNT(p) FROM PointHistory p WHERE p.user.id = :userId AND DATE(p.date) = CURRENT_DATE AND p.reason = 'AI 분석 리워드'")
    Long getTodayAiRewardCount(@Param("userId") Long userId);
}