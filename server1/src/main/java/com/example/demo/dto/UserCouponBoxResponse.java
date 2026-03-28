package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCouponBoxResponse {
    private List<UserCouponResponseDto> unusedCoupons;
    private List<UserCouponResponseDto> usedCoupons;
}
