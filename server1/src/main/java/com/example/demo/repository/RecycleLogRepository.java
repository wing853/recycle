package com.example.demo.repository;

import com.example.demo.entity.RecycleLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecycleLogRepository extends JpaRepository<RecycleLog, Long> {

    // ✅ 사용자별 분리수거 횟수 집계 (user_id, count) - null 유저 제거
    @Query("SELECT r.user.id, r.user.username, COUNT(r) FROM RecycleLog r WHERE r.user IS NOT NULL GROUP BY r.user.id, r.user.username ORDER BY COUNT(r) DESC")
    List<Object[]> countRecycleLogsByUser();

    // ✅ 전체 유저 수 (분리수거 기록이 있는 유저 수)
    @Query("SELECT COUNT(DISTINCT r.user.id) FROM RecycleLog r")
    Long countDistinctUsers();

    // ✅ 전체 분리수거 횟수
    @Query("SELECT COUNT(r) FROM RecycleLog r")
    Long countTotalRecycles();

    // ✅ 단일 사용자 분리수거 횟수 (추가 필요)
    long countByUserId(Long userId);

    // 단일 사용자 모든 분리수거 조회
    List<RecycleLog> findByUserId(Long userId);
}
