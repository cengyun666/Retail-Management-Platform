package com.example.userauth.dto;

/**
 * 登录响应DTO
 */
public class LoginResponse {
    private UserInfo userInfo;
    private String token;
    private Long userId;
    private String username;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    // Getters and Setters
    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}