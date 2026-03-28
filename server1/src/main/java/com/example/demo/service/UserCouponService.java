package com.example.demo.service;

import com.example.demo.dto.UserCouponBoxResponse;
import com.example.demo.dto.UserCouponResponseDto;
import com.example.demo.entity.UserCoupon;
import com.example.demo.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCouponService {

    private final UserCouponRepository userCouponRepository;

    @Transactional(readOnly = true)
    public UserCouponBoxResponse getUserCoupons(Long userId) {
        // ✅ 미사용 쿠폰
        List<UserCoupon> unused = userCouponRepository.findByUserIdAndUsedFalse(userId);
        // ✅ 사용한 쿠폰
        List<UserCoupon> used = userCouponRepository.findByUserIdAndUsedTrue(userId);

        // ✅ 미사용 쿠폰 DTO 변환
        List<UserCouponResponseDto> unusedDtos = unused.stream().map(uc -> {
            long remainingDays = ChronoUnit.DAYS.between(LocalDate.now(), uc.getExpireDate());

            return new UserCouponResponseDto(
                uc.getId(),
                uc.getCoupon().getBrandName(),
                uc.getCoupon().getProductName(),
                uc.getCoupon().getImageUrl(),
                uc.getPurchasedAt().toLocalDate(),
                uc.getExpireDate(),
                remainingDays,
                uc.getBarcode(),
                null,
                null
            );
        }).collect(Collectors.toList());

        // ✅ 사용 쿠폰 DTO 변환
        List<UserCouponResponseDto> usedDtos = used.stream().map(uc -> new UserCouponResponseDto(
                uc.getId(),
                uc.getCoupon().getBrandName(),
                uc.getCoupon().getProductName(),
                uc.getCoupon().getImageUrl(),
                uc.getPurchasedAt().toLocalDate(),
                null,
                null,
                null,
                uc.getPurchasedAt(),  // 사용일로 대체
                uc.getCoupon().getPoints()  // 사용 포인트
        )).collect(Collectors.toList());

        return new UserCouponBoxResponse(unusedDtos, usedDtos);
    }
}
