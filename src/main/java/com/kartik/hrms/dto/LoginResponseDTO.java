package com.kartik.hrms.dto;

public class LoginResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String role;

    public LoginResponseDTO(String accessToken, Long userId, String username, String role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
