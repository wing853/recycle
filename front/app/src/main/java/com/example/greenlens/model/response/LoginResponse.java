package com.example.greenlens.model.response;

public class LoginResponse {
    private String message;
    private String email;
    private String token;
    private int expiresIn;

    public LoginResponse(String message, String email, String token, int expiresIn) {
        this.message = message;
        this.email = email;
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}