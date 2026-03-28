package com.example.demo.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupResponse {
    private String message;
    private Long userId;
    private String username;  
    private String email; 
    private String createdAt;
}
