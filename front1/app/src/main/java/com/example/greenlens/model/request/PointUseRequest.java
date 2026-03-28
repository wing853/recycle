package com.example.greenlens.model.request;

import com.google.gson.annotations.SerializedName;

public class PointUseRequest {
    @SerializedName("points_to_use")
    private int pointsToUse;

    @SerializedName("reason")
    private String reason;

    public PointUseRequest(int pointsToUse, String reason) {
        this.pointsToUse = pointsToUse;
        this.reason = reason;
    }

    // Getters
    public int getPointsToUse() {
        return pointsToUse;
    }

    public String getReason() {
        return reason;
    }

    // Setters
    public void setPointsToUse(int pointsToUse) {
        this.pointsToUse = pointsToUse;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}