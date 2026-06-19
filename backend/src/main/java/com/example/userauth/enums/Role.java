package com.example.userauth.enums;

/**
 * 用户角色枚举
 */
public enum Role {
    ADMIN("admin", "管理员"),
    MERCHANT("merchant", "商家"),
    USER("user", "普通用户");

    private final String code;
    private final String description;

    Role(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Role fromCode(String code) {
        for (Role role : Role.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return USER; // 默认返回普通用户
    }
}