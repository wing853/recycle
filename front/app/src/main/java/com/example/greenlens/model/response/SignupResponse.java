package com.example.greenlens.model.response;

public class SignupResponse {
    private String message;
    private Long userId;
    private String createdAt;

    public SignupResponse(String message, Long userId, String createdAt) {
        this.message = message;
        this.userId = userId;
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}