package com.example.greenlens.model.response;

import com.google.gson.annotations.SerializedName;

public class AnalyzeResponse {
    @SerializedName("analysis_id")
    private Long analysisId;

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("remaining_reward_count")
    private int remainingRewardCount;

    @SerializedName("disposal_method")
    private String disposalMethod;

    @SerializedName("confidence")
    private double confidence;

    @SerializedName("category")
    private String category;

    @SerializedName("points_rewarded")
    private int pointsRewarded;

    public Long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getRemainingRewardCount() {
        return remainingRewardCount;
    }

    public void setRemainingRewardCount(int remainingRewardCount) {
        this.remainingRewardCount = remainingRewardCount;
    }

    public String getDisposalMethod() {
        return disposalMethod;
    }

    public void setDisposalMethod(String disposalMethod) {
        this.disposalMethod = disposalMethod;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getPointsRewarded() {
        return pointsRewarded;
    }

    public void setPointsRewarded(int pointsRewarded) {
        this.pointsRewarded = pointsRewarded;
    }
}