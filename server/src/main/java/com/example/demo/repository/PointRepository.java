package com.example.demo.repository;

import com.example.demo.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByUserId(Long userId);

    // ✅ 회원 탈퇴 시 사용자 데이터 일괄 삭제
    void deleteByUserId(Long userId);
}
