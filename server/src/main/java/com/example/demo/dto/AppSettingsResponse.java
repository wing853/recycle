package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AppSettingsResponse {
    private String theme;
    private Boolean notifications;
    private String language;
}
