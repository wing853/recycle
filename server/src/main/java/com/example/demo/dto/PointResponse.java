package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
public class PointResponse {
    private Long userId;
    private int points;
    private ZonedDateTime updatedAt;
}