package com.example.demo.repository;

import com.example.demo.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    // 필요하면 여기서 커스텀 쿼리 메서드 정의
    // 예: List<Coupon> findByExpiryDateAfter(LocalDateTime now);
}
