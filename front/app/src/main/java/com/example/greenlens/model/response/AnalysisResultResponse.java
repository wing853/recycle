package com.example.greenlens.model.response;

import com.google.gson.annotations.SerializedName;

public class AnalysisResultResponse {
    @SerializedName("analysis_id")
    private Long analysisId;

    @SerializedName("category")
    private String category;

    @SerializedName("confidence")
    private double confidence;

    @SerializedName("disposal_method")
    private String disposalMethod;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("message")
    private String message;

    public Long getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Long analysisId) {
        this.analysisId = analysisId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getDisposalMethod() {
        return disposalMethod;
    }

    public void setDisposalMethod(String disposalMethod) {
        this.disposalMethod = disposalMethod;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    // 서버 응답의 category를 앱에서 사용하는 type으로 변환
    public String getTypeForApp() {
        return mapCategoryToType(category);
    }

    public static String mapCategoryToType(String category) {
        if (category == null) return "plastic";

        switch (category.toLowerCase().trim()) {
            case "페트병":
            case "pet":
            case "pet_bottle":
            case "pet bottle":
            case "bottle":
            case "plastic_bottle":
            case "음료병":
            case "음료수병":
                return "pet";
            case "플라스틱":
            case "plastic":
            case "플라스틱용기":
            case "plastic_container":
                return "plastic";
            case "종이":
            case "paper":
            case "cardboard":
            case "종이류":
                return "paper";
            case "유리":
            case "glass":
            case "유리병":
            case "glass_bottle":
                return "glass";
            case "캔":
            case "금속":
            case "metal":
            case "can":
            case "aluminum":
            case "알루미늄":
            case "알루미늄캔":
            case "철캔":
            case "금속캔":
                return "metal";
            case "비닐":
            case "vinyl":
            case "비닐봉지":
            case "vinyl_bag":
                return "vinyl";
            case "스티로폼":
            case "styrofoam":
            case "foam":
            case "eps":
                return "styrofoam";
            default:
                return "plastic";
        }
    }
}