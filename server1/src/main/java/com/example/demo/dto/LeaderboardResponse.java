package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponse {
    private ZonedDateTime lastUpdated;
    private Long totalUsers;
    private Long totalRecycleCount;
    private List<UserRankingDto> rankings;
}
