package com.example.greenlens.repository;

import com.example.greenlens.model.Coupon;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserCouponRepository {
    private final String userId;
    private List<Coupon> userCoupons;

    public UserCouponRepository(String userId) {
        this.userId = userId;
        this.userCoupons = new ArrayList<>();
        loadUserCoupons();
    }

    // 사용 가능한 쿠폰 조회
    public List<Coupon> getAvailableCoupons() {
        return userCoupons.stream()
                .filter(Coupon::isAvailable)
                .collect(Collectors.toList());
    }

    // 사용한 쿠폰 조회
    public List<Coupon> getUsedCoupons() {
        return userCoupons.stream()
                .filter(Coupon::isUsed)
                .collect(Collectors.toList());
    }

    // 만료된 쿠폰 조회
    public List<Coupon> getExpiredCoupons() {
        return userCoupons.stream()
                .filter(Coupon::isExpired)
                .collect(Collectors.toList());
    }

    // 카테고리별 쿠폰 조회
    public List<Coupon> getCouponsByCategory(String category) {
        return userCoupons.stream()
                .filter(coupon -> coupon.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    // 쿠폰 추가
    public void addCoupon(Coupon coupon) {
        userCoupons.add(coupon);
        saveUserCoupons();
    }

    // 쿠폰 사용
    public boolean useCoupon(Coupon coupon) {
        if (coupon.isAvailable()) {
            coupon.use();
            saveUserCoupons();
            return true;
        }
        return false;
    }

    private void loadUserCoupons() {
        // TODO: DB나 서버에서 사용자의 쿠폰 목록 로드
    }

    private void saveUserCoupons() {
        // TODO: DB나 서버에 사용자의 쿠폰 목록 저장
    }
}