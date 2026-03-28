package com.example.greenlens.repository;

import com.example.greenlens.R;
import com.example.greenlens.model.Coupon;
import java.util.ArrayList;
import java.util.List;

public class CouponRepository {
    private List<Coupon> shopCoupons;

    public CouponRepository() {
        shopCoupons = new ArrayList<>();
        initShopCoupons();
    }

    private void initShopCoupons() {
        // 더미 데이터 초기화
        shopCoupons.add(new Coupon("스타벅스", "아메리카노", 5000, "카페", "2025-12-31", R.drawable.ic_cafe));
        shopCoupons.add(new Coupon("투썸플레이스", "카페라떼", 5500, "카페", "2025-12-31", R.drawable.ic_cafe));
        shopCoupons.add(new Coupon("버거킹", "와퍼", 8000, "식당", "2025-12-31", R.drawable.ic_restaurant));
        // 더 많은 쿠폰 추가 가능
    }

    public List<Coupon> getAllCoupons() {
        return shopCoupons;
    }

    public List<Coupon> getCouponsByCategory(String category) {
        if (category.equals("전체")) {
            return shopCoupons;
        }

        List<Coupon> filteredCoupons = new ArrayList<>();
        for (Coupon coupon : shopCoupons) {
            if (coupon.getCategory().equals(category)) {
                filteredCoupons.add(coupon);
            }
        }
        return filteredCoupons;
    }
}