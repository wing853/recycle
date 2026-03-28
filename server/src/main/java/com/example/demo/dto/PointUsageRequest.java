package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointUsageRequest {
    private int pointsToUse;
    private String reason;
}
