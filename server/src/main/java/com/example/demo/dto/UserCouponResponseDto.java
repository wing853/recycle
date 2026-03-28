package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCouponResponseDto {
    private Long id;
    private String brandName;
    private String productName;
    private String imageUrl;
    private LocalDate purchaseDate;
    private LocalDate expireDate;
    private Long daysRemaining;
    private String barcode;
    private ZonedDateTime usedDate;
    private Integer pointsUsed;
}
