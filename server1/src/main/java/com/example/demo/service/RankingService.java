package com.example.demo.service;

import com.example.demo.dto.LeaderboardResponse;
import com.example.demo.dto.UserRankingDto;
import com.example.demo.repository.RecycleLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RecycleLogRepository recycleLogRepository;

    @Transactional(readOnly = true)
    public LeaderboardResponse getLeaderboard() {
        List<Object[]> rawData = recycleLogRepository.countRecycleLogsByUser();

        List<UserRankingDto> rankings = rawData.stream()
                .map(data -> {
                    Long userId = (Long) data[0];
                    String username = (String) data[1];
                    Long count = (Long) data[2];

                    return new UserRankingDto(0, userId, username, count);
                })
                .collect(Collectors.toList());

        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return new LeaderboardResponse(
                ZonedDateTime.now(),
                recycleLogRepository.countDistinctUsers(),
                recycleLogRepository.countTotalRecycles(),
                rankings
        );
    }
}