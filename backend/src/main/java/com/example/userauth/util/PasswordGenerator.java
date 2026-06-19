package com.example.userauth.util;

public class PasswordGenerator {
    public static void main(String[] args) {
        String password = "123456";
        String encodedPassword = PasswordUtil.encode(password);
        System.out.println("原始密码: " + password);
        System.out.println("加密后的密码: " + encodedPassword);
    }
}