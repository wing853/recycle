package com.example.greenlens.model;

import com.google.gson.annotations.SerializedName;

public class AppSettings {
    @SerializedName("theme")
    private String theme;

    @SerializedName("notifications")
    private boolean notifications;

    @SerializedName("language")
    private String language;

    // 기본 생성자
    public AppSettings() {
        this.theme = "light";
        this.notifications = true;
        this.language = "ko";
    }

    // 파라미터가 있는 생성자
    public AppSettings(String theme, boolean notifications, String language) {
        this.theme = theme;
        this.notifications = notifications;
        this.language = language;
    }

    // Getters
    public String getTheme() {
        return theme;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public String getLanguage() {
        return language;
    }

    // Setters
    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}