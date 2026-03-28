package com.example.demo.service;

import com.example.demo.dto.PurchaseResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserCouponRepository userCouponRepository;

    /** 모든 쿠폰 조회 */
    public List<Coupon> getCoupons() {
        return couponRepository.findAll();
    }

    /** 상품권 구매 */
    @Transactional
    public PurchaseResponse purchaseCoupon(Long userId, Long couponId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다."));

        Point point = pointRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("포인트 정보가 없습니다."));

        int couponPoint = coupon.getPoints();
        if (point.getPoints() < couponPoint) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }

        point.setPoints(point.getPoints() - couponPoint);
        pointRepository.save(point);

        PointHistory history = new PointHistory();
        history.setUser(user);
        history.setPoints(-couponPoint);
        history.setReason("상품권 구매: " + coupon.getProductName());
        history.setBalance(point.getPoints());
        history.setDate(ZonedDateTime.now());
        history.setType("사용");
        history.setBrandName(coupon.getBrandName());
        pointHistoryRepository.save(history);

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUser(user);
        userCoupon.setCoupon(coupon);
        userCoupon.setPurchasedAt(ZonedDateTime.now());
        userCoupon.setExpireDate(LocalDate.now().plusDays(coupon.getExpireDays()));
        userCoupon.setBarcode(UUID.randomUUID().toString().substring(0, 13));
        userCoupon.setUsed(false);
        userCouponRepository.save(userCoupon);

        PurchaseResponse.CouponDetails details = new PurchaseResponse.CouponDetails();
        details.setBrandName(coupon.getBrandName());
        details.setProductName(coupon.getProductName());
        details.setPointsUsed(couponPoint);

        PurchaseResponse response = new PurchaseResponse();
        response.setSuccess(true);
        response.setMessage("상품권 구매가 완료되었습니다.");
        response.setUserCouponId(userCoupon.getId());
        response.setRemainingPoints(point.getPoints());
        response.setExpireDate(userCoupon.getExpireDate());
        response.setBarcode(userCoupon.getBarcode());
        response.setCouponDetails(details);

        return response;
    }

    /** 쿠폰 사용 */
    @Transactional
    public PurchaseResponse useCoupon(User user, Long couponId) {
        Long userId = user.getId();

        UserCoupon userCoupon = userCouponRepository
                .findFirstByUserIdAndCouponIdAndUsedFalse(userId, couponId)
                .orElseThrow(() -> new IllegalArgumentException("사용 가능한 쿠폰이 없습니다."));

        userCoupon.setUsed(true);
        ZonedDateTime usedDate = ZonedDateTime.now();
        userCoupon.setUsedDate(usedDate);
        userCouponRepository.save(userCoupon);

        Coupon coupon = userCoupon.getCoupon();
        String imageUrl = coupon.getImageUrl(); // 예: "/images/2.png"
        String base64Image = encodeImageToBase64(imageUrl);

        PurchaseResponse.CouponDetails details = new PurchaseResponse.CouponDetails();
        details.setBrandName(coupon.getBrandName());
        details.setProductName(coupon.getProductName());
        details.setBarcode(userCoupon.getBarcode());
        details.setExpireDate(userCoupon.getExpireDate());
        details.setUsageInstructions("매장에서 이 쿠폰 이미지를 제시해주세요");

        PurchaseResponse response = new PurchaseResponse();
        response.setSuccess(true);
        response.setMessage("쿠폰이 성공적으로 발급되었습니다.");
        response.setUsedDate(userCoupon.getUsedDate());
        response.setCouponImageUrl(imageUrl);
        response.setCouponImageBase64(base64Image);
        response.setCouponDetails(details);

        return response;
    }

    /** base64 인코딩을 위해 classpath에서 이미지 로드 */
    private String encodeImageToBase64(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank() || imageUrl.equalsIgnoreCase("없음")) {
            return "";
        }

        try {
            // 외부 URL이면 URL 열기
            if (imageUrl.startsWith("http")) {
                try (InputStream in = new java.net.URL(imageUrl).openStream()) {
                    byte[] imageBytes = in.readAllBytes();
                    return Base64.getEncoder().encodeToString(imageBytes);
                }
            }

            // 내부 리소스 경로이면 classpath에서 로드
            ClassPathResource resource = new ClassPathResource("static" + imageUrl);
            try (InputStream in = resource.getInputStream()) {
                byte[] imageBytes = in.readAllBytes();
                return Base64.getEncoder().encodeToString(imageBytes);
            }

        } catch (Exception e) {
            throw new RuntimeException("이미지를 base64로 변환할 수 없습니다: " + imageUrl, e);
        }
    }
}
