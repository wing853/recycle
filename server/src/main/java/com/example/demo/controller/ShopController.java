package com.example.demo.controller;

import com.example.demo.dto.PurchaseRequest;
import com.example.demo.dto.PurchaseResponse;
import com.example.demo.entity.Coupon;
import com.example.demo.entity User;
import com.example.demo.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final CouponService couponService;

    /** ✅ 로그인한 사용자가 상품권 목록을 조회 */
    @GetMapping("/coupons")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Coupon>> getCoupons() {
        return ResponseEntity.ok(couponService.getCoupons());
    }

    /** ✅ 상품권 구매 (빈 body도 허용) */
    @PostMapping("/coupons/{couponId}/purchase")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PurchaseResponse> purchaseCoupon(
            @PathVariable Long couponId,
            @RequestBody(required = false) Map<String, Object> body, // ⭐️ body 없어도 허용
            @AuthenticationPrincipal User user) {

        Long userId = userDetails.getUser().getId();
        PurchaseResponse response = couponService.purchaseCoupon(userId, couponId);
        return ResponseEntity.ok(response);
    }

    /** ✅ 로그인한 사용자가 자신의 쿠폰을 사용 */
    @PostMapping("/coupons/{couponId}/use")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PurchaseResponse> useCoupon(
            @PathVariable Long couponId,
            @AuthenticationPrincipal User user) {

        PurchaseResponse response = couponService.useCoupon(userDetails.getUser(), couponId);
        return ResponseEntity.ok(response);
    }
}
