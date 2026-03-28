package com.example.greenlens.model.response;

import com.google.gson.annotations.SerializedName;

public class PointResponse {
    @SerializedName("user_id")
    private Long userId;

    @SerializedName("points")
    private int points;

    @SerializedName("last_updated")
    private String lastUpdated;

    @SerializedName("message")
    private String message;

    @SerializedName("remaining_points")
    private int remainingPoints;

    @SerializedName("updated_at")
    private String updatedAt;

    // Getters
    public Long getUserId() {
        return userId;
    }

    public int getPoints() {
        return points;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public String getMessage() {
        return message;
    }

    public int getRemainingPoints() {
        return remainingPoints;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRemainingPoints(int remainingPoints) {
        this.remainingPoints = remainingPoints;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}