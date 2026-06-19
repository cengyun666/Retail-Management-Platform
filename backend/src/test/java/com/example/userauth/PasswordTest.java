package com.example.userauth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
public class PasswordTest {

    @Test
    public void testNewPasswordEncoding() {
        // 测试新密码的加密和匹配
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("New encoding for '" + rawPassword + "': " + encodedPassword);
        System.out.println("New encoding matches: " + encoder.matches(rawPassword, encodedPassword));
        
        // 测试已知匹配的密码和加密值
        String knownPassword = "123456";
        String knownEncodedPassword = "$2a$10$X.nEN9li1gQQ0eaFJloS/OlwNRZ89fpGqnWtIGa3RBYglfmUtQ63q";
        
        System.out.println("Known password '" + knownPassword + "' matches known encoding: " + 
                          encoder.matches(knownPassword, knownEncodedPassword));
    }

    @Test
    public void testPasswordMatching() {
        // 测试数据库中的密码是否与常见密码匹配
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String dbPassword = "$2a$10$X.nEN9li1gQQ0eaFJloS/OlwNRZ89fpGqnWtIGa3RBYglfmUtQ63q"; // 更新后的密码
        
        System.out.println("Testing password '123456': " + encoder.matches("123456", dbPassword));
        System.out.println("Testing password 'admin': " + encoder.matches("admin", dbPassword));
        System.out.println("Testing password 'password': " + encoder.matches("password", dbPassword));
        System.out.println("Testing password 'admin123': " + encoder.matches("admin123", dbPassword));
    }
}