package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointUsageResponse {
    private String message;
    private int remainingPoints;
    private String updatedAt;
}
