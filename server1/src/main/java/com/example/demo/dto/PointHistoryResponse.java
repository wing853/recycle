package com.example.demo.dto;

import com.example.demo.entity.PointHistory;
import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
@Builder
public class PointHistoryResponse {
    private Long id;
    private ZonedDateTime date;
    private String type;
    private String reason;
    private String brandName;
    private int points;
    private int balance;

    public static PointHistoryResponse from(PointHistory entity) {
        return PointHistoryResponse.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .type(entity.getType())
                .reason(entity.getReason())
                .brandName(entity.getBrandName() != null ? entity.getBrandName() : entity.getWasteTypeKorean())
                .points(entity.getPoints())
                .balance(entity.getBalance())
                .build();
    }
}

