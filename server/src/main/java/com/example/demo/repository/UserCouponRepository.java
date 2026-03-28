package com.example.demo.repository;

import com.example.demo.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    // ❌ 기존 메서드: 중복 데이터 존재 시 오류 가능
    Optional<UserCoupon> findByUserIdAndCouponId(Long userId, Long couponId);

    // ✅ 변경 메서드: 미사용 쿠폰 중 첫 번째 항목만 안전하게 조회
    Optional<UserCoupon> findFirstByUserIdAndCouponIdAndUsedFalse(Long userId, Long couponId);

    // ✅ 미사용 쿠폰 목록 조회
    List<UserCoupon> findByUserIdAndUsedFalse(Long userId);

    // ✅ 사용한 쿠폰 목록 조회
    List<UserCoupon> findByUserIdAndUsedTrue(Long userId);
}
