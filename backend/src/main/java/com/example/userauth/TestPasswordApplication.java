package com.example.userauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class TestPasswordApplication {
    public static void main(String[] args) {
        // Get the Spring context
        ConfigurableApplicationContext context = SpringApplication.run(UserAuthApplication.class, args);
        
        // Get the password encoder from the context
        BCryptPasswordEncoder encoder = context.getBean(BCryptPasswordEncoder.class);
        
        // Password hash from database
        String hashedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        
        // Test different passwords
        String[] testPasswords = {"admin", "123456", "password", "Admin"};
        
        System.out.println("Testing password matches:");
        for (String password : testPasswords) {
            boolean matches = encoder.matches(password, hashedPassword);
            System.out.println("Password: '" + password + "' -> " + (matches ? "Match" : "No match"));
        }
        
        // Generate new admin password hash
        String newHash = encoder.encode("admin");
        System.out.println("\nNew admin password hash: " + newHash);
        
        // Close the context
        context.close();
    }
}