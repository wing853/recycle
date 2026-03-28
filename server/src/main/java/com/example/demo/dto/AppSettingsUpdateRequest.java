package com.example.demo.dto;

import lombok.Data;

@Data
public class AppSettingsUpdateRequest {
    private String theme;
    private Boolean notifications;
    private String language;
}
