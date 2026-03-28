package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppSettingsUpdateResponse {
    private String message;
    private String updatedAt;
}
