package com.example.greenlens.model.response;

import java.util.List;
import java.util.Map;

public class LeaderboardResponse {
    private String lastUpdated;
    private int totalUsers;
    private int totalRecycleCount;
    private List<Map<String, Object>> rankings;

    public String getLastUpdated() {
        return lastUpdated;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public int getTotalRecycleCount() {
        return totalRecycleCount;
    }

    public List<Map<String, Object>> getRankings() {
        return rankings;
    }
}