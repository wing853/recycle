package com.example.demo.controller;

import com.example.demo.dto.LeaderboardResponse;
import com.example.demo.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/leaderboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaderboardResponse> getLeaderboard() {
        LeaderboardResponse response = rankingService.getLeaderboard();
        return ResponseEntity.ok(response);
    }
}
