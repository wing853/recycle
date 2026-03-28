// model/Point.java
package com.example.greenlens.model;

import com.google.gson.annotations.SerializedName;

public class Point {
    @SerializedName("logId")
    private Long logId;

    @SerializedName("analysisId")
    private Long analysisId;

    @SerializedName("category")
    private String category;

    @SerializedName("disposalCategory")
    private String disposalCategory;

    @SerializedName("disposalMethod")
    private String disposalMethod;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("totalPoints")
    private int totalPoints;

    @SerializedName("recycleCount")
    private int recycleCount;

    @SerializedName("message")
    private String message;

    public Long getLogId() {
        return logId;
    }

    public Long getAnalysisId() {
        return analysisId;
    }

    public String getCategory() {
        return category;
    }

    public String getDisposalCategory() {
        return disposalCategory;
    }

    public String getDisposalMethod() {
        return disposalMethod;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public int getRecycleCount() {
        return recycleCount;
    }

    public String getMessage() {
        return message;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDisposalCategory(String disposalCategory) {
        this.disposalCategory = disposalCategory;
    }

    public void setDisposalMethod(String disposalMethod) {
        this.disposalMethod = disposalMethod;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public void setRecycleCount(int recycleCount) {
        this.recycleCount = recycleCount;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
