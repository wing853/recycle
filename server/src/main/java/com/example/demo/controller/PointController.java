package com.example.demo.controller;

import com.example.demo.dto.PointHistoryResponse;
import com.example.demo.dto.PointResponse;
import com.example.demo.entity.Point;
import com.example.demo.entity User;
import com.example.demo.service.PointHistoryService;
import com.example.demo.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final PointHistoryService pointHistoryService;

    /** 🔹 현재 포인트 조회 */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PointResponse> getMyPoint(@AuthenticationPrincipal User user) {
        Long userId = userDetails.getUser().getId();
        Point point = pointService.getUserPointInfo(userId);

        PointResponse response = PointResponse.builder()
                .userId(userId)
                .points(point.getPoints())
                .updatedAt(point.getUpdatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

    /** 🔹 포인트 내역 전체 조회 */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PointHistoryResponse>> getPointHistory(@AuthenticationPrincipal User user) {
        Long userId = userDetails.getUser().getId();
        List<PointHistoryResponse> history = pointHistoryService.getHistory(userId);
        return ResponseEntity.ok(history);
    }
}